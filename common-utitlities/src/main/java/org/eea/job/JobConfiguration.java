package org.eea.job;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configures Lock Aspect to prevent one method to be executed more than onece at a time.
 *
 * @see org.eea.lock.aspect.MethodLockAspect
 */
@Configuration
@ComponentScan("org.eea.job")
public class JobConfiguration {

}
