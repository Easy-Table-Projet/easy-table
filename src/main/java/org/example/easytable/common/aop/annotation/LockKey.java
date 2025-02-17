package org.example.easytable.common.aop.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER) // 파라미터에서만 사용 가능
public @interface LockKey {
}

