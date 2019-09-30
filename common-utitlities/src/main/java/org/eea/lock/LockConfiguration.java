package org.eea.lock;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configures Lock Aspect to prevent one method to be executed more than onece at a time.
 *
 * @see org.eea.lock.aspect.LockAspect
 */
@Configuration
@EnableAspectJAutoProxy
public class LockConfiguration {

}
