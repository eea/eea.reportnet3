package org.eea.lock.configuration;

import org.eea.job.EnableJobScheduler;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configures Lock Aspect to prevent one method to be executed more than onece at a time.
 *
 * @see org.eea.lock.aspect.MethodLockAspect
 */
@Configuration
@EnableAspectJAutoProxy
@EnableJobScheduler
@ComponentScan("org.eea.lock")
public class LockConfiguration {

}
