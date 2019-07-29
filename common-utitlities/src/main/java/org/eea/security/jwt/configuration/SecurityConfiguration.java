package org.eea.security.jwt.configuration;


import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.eea.security.jwt.expression.SecondLevelAuthorizationConfiguration;
import org.eea.security.jwt.utils.JwtAuthenticationEntryPoint;
import org.eea.security.jwt.utils.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * The type Security configuration.
 */
@Configuration
@EnableWebSecurity
@ComponentScan("org.eea.security")
@Import(SecondLevelAuthorizationConfiguration.class)
public abstract class SecurityConfiguration extends WebSecurityConfigurerAdapter {

  @Autowired
  private JwtAuthenticationEntryPoint unauthorizedHandler;

  /**
   * Jwt authentication filter jwt authentication filter.
   *
   * @return the jwt authentication filter
   */
  @Autowired
  private JwtAuthenticationFilter jwtAuthenticationFilter;


  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .csrf()
        .disable()
        .exceptionHandling()
        .authenticationEntryPoint(unauthorizedHandler)
        .and()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .authorizeRequests()
        .antMatchers("/actuator/**")
        .permitAll()
    //.antMatchers("/user/test-security").hasRole("PROVIDER")

    ;
    String[] authenticatedRequest = getAuthenticatedRequest();
    if (null != authenticatedRequest && authenticatedRequest.length > 0) {
      http.authorizeRequests().antMatchers(authenticatedRequest).authenticated();
    }
    String[] permitedRequest = getPermitedRequest();
    if (null != permitedRequest && permitedRequest.length > 0) {
      http.authorizeRequests().antMatchers(permitedRequest).permitAll();
    }

    List<Pair<String[], String>> roleProtectedRequest = getRoleProtectedRequest();
    if (null != roleProtectedRequest && roleProtectedRequest.size() > 0) {
      roleProtectedRequest.stream().forEach(pair -> {
        try {
          http.authorizeRequests().antMatchers(pair.getLeft()).hasRole(pair.getRight());
        } catch (Exception e) {
          e.printStackTrace();
        }
      });

    }
    // Add our custom JWT security filter
    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);


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
  protected abstract String[] getPermitedRequest();

  /**
   * Gets role protected request.
   *
   * @return the role protected request
   */
  protected abstract List<Pair<String[], String>> getRoleProtectedRequest();
}
