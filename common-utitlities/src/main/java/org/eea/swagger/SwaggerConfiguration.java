package org.eea.swagger;

import com.google.common.collect.Lists;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * The type Swagger configuration.
 */
@Configuration
@EnableSwagger2
@EnableWebMvc
public class SwaggerConfiguration implements WebMvcConfigurer {

  /**
   * Api docket.
   *
   * @return the docket
   */
  @Bean
  @Profile("!production")
  public Docket nonProdApi() {
    return new Docket(DocumentationType.SWAGGER_2)
        .securityContexts(Lists.newArrayList(securityContext()))
        .securitySchemes(Lists.newArrayList(apiKey())).select()
        .apis(RequestHandlerSelectors.basePackage("org.eea")).paths(PathSelectors.any()).build();
  }

  /**
   * Prod api docket.
   *
   * @return the docket
   */
  @Bean
  @Profile("production")
  public Docket prodApi() {
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.basePackage("org.eea")).paths(PathSelectors.any()).build();
  }

  /**
   * Adds the resource handlers.
   *
   * @param registry the registry
   */
  @Override
  public void addResourceHandlers(final ResourceHandlerRegistry registry) {
    registry.addResourceHandler("swagger-ui.html")
        .addResourceLocations("classpath:/META-INF/resources/");

    registry.addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/");
  }

  /**
   * Cors filter cors filter for Swagger
   *
   * @return the cors filter
   */
  @Bean
  public CorsFilter corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOrigin("*");
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");

    source.registerCorsConfiguration("/v2/api-docs", config);
    return new CorsFilter(source);
  }

  /**
   * Api key.
   *
   * @return the api key
   */
  private static ApiKey apiKey() {
    return new ApiKey("JWT", "Authorization", "header");
  }

  /**
   * Security context.
   *
   * @return the security context
   */
  private SecurityContext securityContext() {
    return SecurityContext.builder().securityReferences(defaultAuth())
        .forPaths(PathSelectors.regex("/.*")).build();
  }

  /**
   * Default auth list.
   *
   * @return the list
   */
  private List<SecurityReference> defaultAuth() {
    final AuthorizationScope authorizationScope =
        new AuthorizationScope("global", "accessEverything");
    final AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
    authorizationScopes[0] = authorizationScope;
    return Lists.newArrayList(new SecurityReference("JWT", authorizationScopes));
  }
}
