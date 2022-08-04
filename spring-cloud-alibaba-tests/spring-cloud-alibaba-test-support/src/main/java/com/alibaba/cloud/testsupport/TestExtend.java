package com.alibaba.cloud.testsupport;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(TestTimeoutExtension.class)
public @interface TestExtend {
    
    //设置超时时间
    long time();
}
