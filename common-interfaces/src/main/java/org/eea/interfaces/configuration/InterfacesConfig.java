package org.eea.interfaces.configuration;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients("org.eea.interfaces")
public class InterfacesConfig {

}
