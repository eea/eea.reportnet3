package org.eea.job;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * Enables Lock Aspect to prevent one method to be executed more than onece at a time.
 *
 * @see org.eea.lock.aspect.MethodLockAspect
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({JobConfiguration.class})
public @interface EnableJobScheduler {

}
