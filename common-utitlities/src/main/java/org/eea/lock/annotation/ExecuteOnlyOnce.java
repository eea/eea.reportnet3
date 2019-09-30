package org.eea.lock.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to ensure that a method is executed only once at a time Works together Works together
 * with {@link org.eea.lock.annotation.LockCriteria} to determine what input parameter are taken
 * into account to lock the method
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ExecuteOnlyOnce {

}
