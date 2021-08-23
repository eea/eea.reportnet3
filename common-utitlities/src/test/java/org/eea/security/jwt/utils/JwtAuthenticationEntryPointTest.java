package org.eea.security.jwt.utils;

import static org.mockito.Mockito.times;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
    MockitoAnnotations.openMocks(this);
  }


  @Test
  public void testCommence() throws IOException, ServletException {
    JwtAuthenticationEntryPoint spyClass = Mockito.spy(jwtAuthenticationEntryPoint);
    spyClass.commence(httpServletRequest, httpServletResponse, authenticationException);
    Mockito.verify(spyClass, times(1)).commence(Mockito.any(), Mockito.any(), Mockito.any());
  }

}
