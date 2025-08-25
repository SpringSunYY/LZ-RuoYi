package com.ruoyi.common.annotation;

import java.lang.annotation.*;

/**
 * 自定义排序注解
 * 首先我希望我变好，也希望你
 *
 * @Author: YY
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CustomSort {
    /*
       createTime-create_time,这样一一对应，对应数据库字段
     */
    String[] sortFields() default {};   //排序字段，就是你前端传过来的字段，一一对应

    String[] sortMappingFields() default {}; //映射字段，前端只需要传过来字段名，如果多表查询设置映射字段，映射字段为查询时所需字段
}
