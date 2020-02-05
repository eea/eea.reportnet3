package org.eea.security.jwt.configuration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * The interface Eea enable security.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({FileBasedSecurityConfiguration.class, CacheClientSecurityConfiguration.class})
public @interface EeaEnableSecurity {


}
