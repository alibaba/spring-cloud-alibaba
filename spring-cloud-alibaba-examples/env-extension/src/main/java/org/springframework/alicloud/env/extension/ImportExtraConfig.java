package org.springframework.alicloud.env.extension;


import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ImportExtraConfig {

    String[] name() default "";
}
