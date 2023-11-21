package edu.jit.nsi.iot_ms.responseResult.result;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseResult {

    Class<? extends Result> value() default PlatformResult.class;
}
