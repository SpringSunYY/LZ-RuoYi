package com.ruoyi.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义缓存注解，父类字段无法拿到
 * 坚持是前行的舟
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomCacheable {
    String keyPrefix();          // 缓存 key 的前缀，你要设置的key的前缀

    String keyField() default "";           // 从方法入参中提取 key 的字段路径（支持嵌套字段）

    long expireTime() default 3600L; // 缓存时间（秒）

    boolean paginate() default false; // 是否启用分页缓存

    String pageNumberField() default ""; // 页码字段（如果 paginate 为 true，则使用该字段）

    String pageSizeField() default "";  // 每页大小字段（如果 paginate 为 true，则使用该字段）

    boolean useQueryParamsAsKey() default false; // 是否将整个查询对象转换为 JSON 字符串作为缓存 key 的一部分
}

