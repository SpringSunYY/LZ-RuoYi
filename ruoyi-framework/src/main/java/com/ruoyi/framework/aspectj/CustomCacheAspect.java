package com.ruoyi.framework.aspectj;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.annotation.CustomCacheable;
import com.ruoyi.common.core.redis.RedisCache;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 自定义缓存切面
 */
@Aspect
@Component
public class CustomCacheAspect {

    @Resource
    private RedisCache redisCache;

    private static final String COMMON_SEPARATOR_CACHE = ":";

    private static final int DEFAULT_PAGE_SIZE = 30;

    private static final int DEFAULT_PAGE_NUM = 1;

    @Around("@annotation(customCacheable)")
    public Object around(ProceedingJoinPoint joinPoint, CustomCacheable customCacheable) throws Throwable {
        //获取注解参数
        String keyPrefix = customCacheable.keyPrefix();
        String keyFieldPath = customCacheable.keyField();
        boolean useQueryParamsAsKey = customCacheable.useQueryParamsAsKey();
        long expireTime = customCacheable.expireTime();
        boolean paginate = customCacheable.paginate();
        String pageNumberField = customCacheable.pageNumberField();
        String pageSizeField = customCacheable.pageSizeField();

        //获取方法参数
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();

        // 先根据 keyFieldPath 从指定参数中取值
        Object keyFieldValue = null;
        if (keyFieldPath != null && !keyFieldPath.isEmpty()) {
            keyFieldValue = getValueByParamNameAndFieldPath(paramNames, args, keyFieldPath);
        }

        // 构造基础缓存 key
        // 格式：prefix:keyFieldStr 或 prefix:keyFieldStr:json参数字符串
        String baseCacheKey = keyPrefix;
        if (keyFieldValue != null && !keyFieldValue.toString().isEmpty()) {
            baseCacheKey += COMMON_SEPARATOR_CACHE + keyFieldValue.toString();
        }

        // 如果开启了 useQueryParamsAsKey，就把整个参数数组序列化成 JSON 加到 key 后面
        if (useQueryParamsAsKey) {
            baseCacheKey = baseCacheKey + COMMON_SEPARATOR_CACHE + JSON.toJSONString(args);
        }

        // 分页缓存
        if (paginate) {
            int pageNumber = extractIntValue(paramNames, args, pageNumberField, DEFAULT_PAGE_NUM);
            int pageSize = extractIntValue(paramNames, args, pageSizeField, DEFAULT_PAGE_SIZE);
            // 构造缓存 key,不缓存所有数据，而是缓存指定页的数据，防止如果数据量过大，缓存的数据会变大，获取数据拿数据慢
            String pageCacheKey = baseCacheKey + COMMON_SEPARATOR_CACHE + pageNumber + COMMON_SEPARATOR_CACHE + pageSize;
            List<Object> cachedPage = redisCache.getCacheList(pageCacheKey, 0, pageSize - 1);
            if (cachedPage != null && !cachedPage.isEmpty()) {
                return cachedPage;
            }
            //没拿到缓存继续执行方法
            Object result = joinPoint.proceed();

            if (result instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> resultList = (List<Object>) result;
                if (!resultList.isEmpty()) {
                    redisCache.setCacheListRightPushAll(pageCacheKey, resultList, (int) expireTime, TimeUnit.SECONDS);
                }
            }

            return result;
        } else {
            Object cachedValue = redisCache.getCacheObject(baseCacheKey);
            if (cachedValue != null) {
                return cachedValue;
            }
            Object result = joinPoint.proceed();
            redisCache.setCacheObject(baseCacheKey, result, (int) expireTime, TimeUnit.SECONDS);
            return result;
        }
    }


    /**
     * 根据参数名+字段路径，获取对应值
     * keyFieldPath 示例："request.type.id"，
     * 第一部分是参数名 request，
     * 后面是递归取字段 type.id
     *
     * @param paramNames   参数名
     * @param args         参数
     * @param keyFieldPath 字段路径
     * @return 值
     */
    private Object getValueByParamNameAndFieldPath(String[] paramNames, Object[] args, String keyFieldPath) {
        if (keyFieldPath == null || keyFieldPath.isEmpty()) return null;
        //根据.分割参数
        String[] parts = keyFieldPath.split("\\.");
        if (parts.length == 0) return null;

        //获取参数名
        String paramName = parts[0];
        String nestedPath = keyFieldPath.substring(paramName.length());
        if (nestedPath.startsWith(".")) nestedPath = nestedPath.substring(1);

        //获取参数值
        Object paramValue = null;
        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equals(paramName)) {
                paramValue = args[i];
                break;
            }
        }
        if (paramValue == null) return null;

        if (nestedPath.isEmpty()) {
            return paramValue;
        } else {
            return getNestedFieldValue(paramValue, nestedPath);
        }
    }

    /**
     * 递归通过字段路径取对象字段值（支持多层嵌套）
     * 例如 "type.id"
     *
     * @param obj       对象
     * @param fieldPath 字段路径
     * @return 字段值
     */
    private Object getNestedFieldValue(Object obj, String fieldPath) {
        try {
            String[] fields = fieldPath.split("\\.");
            Object current = obj;
            //字段值
            for (String field : fields) {
                if (current == null) return null;
                Field f = current.getClass().getDeclaredField(field);
                f.setAccessible(true);
                current = f.get(current);
            }
            return current;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据字段路径提取 int 类型值（用于分页参数）
     */
    private int extractIntValue(String[] paramNames, Object[] args, String fieldPath, int defaultValue) {
        if (fieldPath == null || fieldPath.isEmpty()) {
            return defaultValue;
        }
        Object val = getValueByParamNameAndFieldPath(paramNames, args, fieldPath);
        if (val instanceof Integer) {
            return (Integer) val;
        } else if (val instanceof Number) {
            return ((Number) val).intValue();
        } else {
            return defaultValue;
        }
    }
}
