package com.ruoyi.framework.aspectj;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.annotation.CustomCacheEvict;
import com.ruoyi.common.core.redis.RedisCache;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Field;

@Aspect
@Component
public class CustomCacheEvictAspect {

    @Resource
    private RedisCache redisCache;

    private static final String COMMON_SEPARATOR_CACHE = ":";

    @Around("@annotation(customCacheEvict)")
    public Object around(ProceedingJoinPoint joinPoint, CustomCacheEvict customCacheEvict) throws Throwable {
        Object result = joinPoint.proceed();

        String[] keyPrefixes = customCacheEvict.keyPrefixes();
        String[] keyFields = customCacheEvict.keyFields();
        boolean useQueryParamsAsKey = customCacheEvict.useQueryParamsAsKey();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < keyPrefixes.length; i++) {
            String prefix = keyPrefixes[i];
            String keyPattern;

            if (keyFields != null && i < keyFields.length && keyFields[i] != null && !keyFields[i].isEmpty()) {
                Object value = getValueByParamNameAndFieldPath(null, args, keyFields[i]);
                if (value != null) {
                    keyPattern = prefix + COMMON_SEPARATOR_CACHE + value.toString() + "*";
                } else {
                    // 字段路径没找到值，退化为前缀模糊删除
                    keyPattern = prefix + "*";
                }
            } else {
                // keyFields 不填，直接前缀模糊删除
                keyPattern = prefix + "*";
            }

            if (useQueryParamsAsKey) {
                String paramsJson = JSON.toJSONString(args);
                keyPattern = keyPattern.substring(0, keyPattern.length() - 1) // 去掉末尾的 '*'
                        + COMMON_SEPARATOR_CACHE + paramsJson + "*";
            }

            redisCache.deleteObjectsByPattern(keyPattern);
        }

        return result;
    }


    /**
     * 重用之前的 getNestedFieldValue 方法，从 args 里找指定字段值
     *
     * @param paramNames 参数名
     * @param args       参数
     * @param fieldPath  字段路径
     * @return 字段值
     */
    private Object getValueByParamNameAndFieldPath(String[] paramNames, Object[] args, String fieldPath) {
        try {
            if (fieldPath == null || fieldPath.isEmpty()) return null;
            String[] parts = fieldPath.split("\\.");
            String paramName = parts[0];
            String nestedField = parts.length > 1 ? fieldPath.substring(paramName.length() + 1) : "";

            Object targetArg = null;
            if (paramNames != null && paramNames.length == args.length) {
                for (int i = 0; i < paramNames.length; i++) {
                    if (paramNames[i].equals(paramName)) {
                        targetArg = args[i];
                        break;
                    }
                }
            }
            if (targetArg == null && args.length > 0) {
                // 如果找不到指定参数名，默认用第一个参数
                targetArg = args[0];
                nestedField = fieldPath;
            }

            if (nestedField.isEmpty()) {
                return targetArg;
            } else {
                return getNestedFieldValue(targetArg, nestedField);
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取嵌套字段的值
     *
     * @param obj       对象
     * @param fieldPath 字段路径
     * @return 字段值
     */
    private Object getNestedFieldValue(Object obj, String fieldPath) {
        try {
            if (obj == null || fieldPath == null || fieldPath.isEmpty()) return null;
            String[] fields = fieldPath.split("\\.");
            Object current = obj;
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
}
