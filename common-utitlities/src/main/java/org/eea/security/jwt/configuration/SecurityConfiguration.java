package org.eea.security.jwt.configuration;


import java.util.List;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.tuple.Pair;
import org.eea.security.jwt.utils.ApiKeyAuthenticationFilter;
import org.eea.security.jwt.utils.ExternalJwtAuthenticationFilter;
import org.eea.security.jwt.utils.JwtAuthenticationEntryPoint;
import org.eea.security.jwt.utils.JwtAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * The type Security configuration.
 */
@Configuration
@EnableWebSecurity
@ComponentScan("org.eea.security")
@Import({EeaExpressionConfiguration.class})
public abstract class SecurityConfiguration extends WebSecurityConfigurerAdapter {


  /**
   * The unauthorized handler.
   */
  @Autowired
  private JwtAuthenticationEntryPoint unauthorizedHandler;

  /**
   * Jwt authentication filter jwt authentication filter.
   *
   * @return the jwt authentication filter
   */
  @Autowired
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  /**
   * The api key authentication filter.
   */
  @Autowired
  private ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

  @Autowired
  private ExternalJwtAuthenticationFilter externalJwtAuthenticationFilter;
  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * Inits the security.
   */
//  @PostConstruct
//  private void initSecurity() {
//    SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
//  }


  /**
   * Configure.
   *
   * @param http the http
   *
   * @throws Exception the exception
   */
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable().exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
        .authorizeRequests().antMatchers("/actuator/**").permitAll()
    // .antMatchers("/user/test-security").hasRole("PROVIDER")

    ;
    String[] authenticatedRequest = getAuthenticatedRequest();
    if (null != authenticatedRequest && authenticatedRequest.length > 0) {
      http.authorizeRequests().antMatchers(authenticatedRequest).authenticated();
    }
    String[] permitedRequest = getPermittedRequest();
    if (null != permitedRequest && permitedRequest.length > 0) {
      http.authorizeRequests().antMatchers(permitedRequest).permitAll();
    }

    List<Pair<String[], String>> roleProtectedRequest = getRoleProtectedRequest();
    if (null != roleProtectedRequest && !roleProtectedRequest.isEmpty()) {
      roleProtectedRequest.stream().forEach(pair -> {
        try {
          http.authorizeRequests().antMatchers(pair.getLeft()).hasRole(pair.getRight());
        } catch (Exception e) {
          LOG_ERROR.error("Exception in security configuration. Message: {}", e.getMessage(), e);
        }
      });

    }
    // Add our custom JWT security filter
    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    http.addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    http.addFilterBefore(externalJwtAuthenticationFilter,
        UsernamePasswordAuthenticationFilter.class);


  }

  /**
   * Get authenticated request string [ ].
   *
   * @return the string [ ]
   */
  protected abstract String[] getAuthenticatedRequest();

  /**
   * Get permited request string [ ].
   *
   * @return the string [ ]
   */
  protected abstract String[] getPermittedRequest();

  /**
   * Gets role protected request.
   *
   * @return the role protected request
   */
  protected abstract List<Pair<String[], String>> getRoleProtectedRequest();


}
