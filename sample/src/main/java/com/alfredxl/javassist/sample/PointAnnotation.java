package com.alfredxl.javassist.sample;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <br> ClassName:   ${className}
 * <br> Description:
 * <br>
 * <br> @author:      谢文良
 * <br> Date:        2018/8/9 14:21
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
public @interface PointAnnotation {
    String className();
    String methodName();
}
