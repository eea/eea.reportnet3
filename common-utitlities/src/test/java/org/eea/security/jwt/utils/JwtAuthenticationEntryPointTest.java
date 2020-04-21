package org.eea.security.jwt.utils;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.AuthenticationException;

public class JwtAuthenticationEntryPointTest {

  @InjectMocks
  JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @Mock
  HttpServletRequest httpServletRequest;

  @Mock
  HttpServletResponse httpServletResponse;

  @Mock
  AuthenticationException authenticationException;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }


  @Test
  public void testCommence() throws IOException, ServletException {
    jwtAuthenticationEntryPoint.commence(httpServletRequest, httpServletResponse,
        authenticationException);
  }

}
