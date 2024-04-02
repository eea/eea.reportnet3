package org.eea.datalake.service.annotation;

import org.eea.s3configuration.PackageConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * imports common package org.eea.datalake.service.impl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) // on class level
@Documented
@Import({LakesPackageConfig.class})
public @interface ImportDataLakeCommons {
}
