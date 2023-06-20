package org.eea.datalake.service.annotation;

import org.springframework.context.annotation.ComponentScan;

import java.lang.annotation.*;

/**
 * imports common package org.eea.datalake.service.impl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) // on class level
@Documented
@ComponentScan("org.eea.datalake.service.impl")
public @interface ImportDataLakeCommons {
}
