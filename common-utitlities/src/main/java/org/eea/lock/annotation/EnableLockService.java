package org.eea.lock.annotation;

import org.eea.lock.configuration.LockServiceConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({LockServiceConfiguration.class})
public @interface EnableLockService {
}
