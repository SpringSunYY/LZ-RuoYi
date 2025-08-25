package com.ruoyi.framework.aspectj;

import com.ruoyi.common.annotation.CustomSort;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义排序注解
 *
 * @Author: YY
 */
@Aspect
@Component
public class CustomSortAspect {

    /**
     * 排序SQL
     */
    public static final String SORT_SQL = "sortSql";

    /**
     * 排序字段
     */
    public static final String ORDER_BY_COLUMN = "orderByColumn";

    /**
     * 是否升序
     */
    public static final String IS_ASC = "isAsc";

    @Before("@annotation(customSort)")
    public void doBefore(JoinPoint joinPoint, CustomSort customSort) throws Throwable {
        //清除排序字段参数
        if (!clearSortJudgeParams(joinPoint)) {
            return;
        }
        //执行sql
        handleSort(joinPoint, customSort);
    }

    private void handleSort(JoinPoint joinPoint, CustomSort customSort) {
        Object params = joinPoint.getArgs()[0];
        if (StringUtils.isNotNull(params) && hasGetParamsMethod(params)) {
            try {
                //动态调用getParams方法
                Method getParamsMethod = params.getClass().getMethod("getParams");
                Object result = getParamsMethod.invoke(params);
                @SuppressWarnings("unchecked")
                Map<String, Object> paramsMap = (Map<String, Object>) result;
                if (StringUtils.isEmpty(paramsMap)) {
                    paramsMap = new HashMap<>();
                }
                //从params参数中获取排序字段，和排序方式
                if (StringUtils.isEmpty(paramsMap)) {
                    return;
                }
                String[] sortFields = customSort.sortFields();
                String[] sortMappingFields = customSort.sortMappingFields();
                //从params参数中获取排序字段，和排序方式
                String filed = paramsMap.get(ORDER_BY_COLUMN).toString();
                boolean isAsc = paramsMap.get(IS_ASC).toString().equalsIgnoreCase("true");
                //校验字段是否存在
                if (!ArrayUtils.contains(sortFields, filed)) {
                    return;
                }
                if (StringUtils.isNotEmpty(sortMappingFields)) {
                    //获取到此字段在字段的索引
                    int index = ArrayUtils.indexOf(sortFields, filed);
                    filed = sortMappingFields[index];
                }
                if (StringUtils.isEmpty(filed)) {
                    //排序字段不存在
                    return;
                }
                //拼接sql
                String sql = "order by " + filed + " " + (isAsc ? "asc" : "desc");
                paramsMap.put(SORT_SQL, sql);
                // 调用 setParams 方法设置新的 Map
                Method setParamsMethod = params.getClass().getMethod("setParams", Map.class);
                setParamsMethod.invoke(params, paramsMap);
            } catch (Exception e) {
                throw new ServiceException("排序出错:" + e.getMessage());
            }
        }
    }

    /**
     * 拼接sql之前先清除排序字段参数，防止sql注入，并查看是否有排序字段参数
     */
    private boolean clearSortJudgeParams(JoinPoint joinPoint) {
        Object params = joinPoint.getArgs()[0];
        if (StringUtils.isNotNull(params) && hasGetParamsMethod(params)) {
            try {
                //动态调用getParams方法
                Method getParamsMethod = params.getClass().getMethod("getParams");
                Object result = getParamsMethod.invoke(params);
                @SuppressWarnings("unchecked")
                Map<String, Object> paramsMap = (Map<String, Object>) result;
                if (StringUtils.isEmpty(paramsMap)) {
                    paramsMap = new HashMap<>();
                }
                paramsMap.put(SORT_SQL, "");
                //获取排序参数
                Object orderBy = paramsMap.get(ORDER_BY_COLUMN);
                Object asc = paramsMap.get(IS_ASC);
                return StringUtils.isNotNull(orderBy) && StringUtils.isNotNull(asc);
            } catch (Exception e) {
                throw new ServiceException("排序出错:" + e.getMessage());
            }
        }
        return false;
    }

    ;

    /**
     * 判断对象是否有 getParams 方法
     */
    private static boolean hasGetParamsMethod(Object obj) {
        try {
            obj.getClass().getMethod("getParams");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
