package org.eea.lock.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Lock Criteria to be used to prevent a method from being executed more than once at a time with
 * the same Criteria.
 *
 * Works together with {@link org.eea.lock.annotation.LockMethod}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface LockCriteria {

  /**
   * Name.
   *
   * @return the string
   */
  public String name();

  /**
   * Path.
   *
   * @return the string
   */
  public String path() default "";
}
