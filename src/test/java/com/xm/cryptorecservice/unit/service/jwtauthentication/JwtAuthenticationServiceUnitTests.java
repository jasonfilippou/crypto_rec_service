package com.xm.cryptorecservice.unit.service.jwtauthentication;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.xm.cryptorecservice.service.jwt.JwtAuthenticationService;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * Unit tests for {@link JwtAuthenticationService}. Perform extensive use of Mockito and jUnit4 assertions.
 * 
 * @author jason 
 */
@RunWith(MockitoJUnitRunner.class)
public class JwtAuthenticationServiceUnitTests {
  private static final String USERNAME = RandomStringUtils.randomAlphanumeric(10);
  private static final String ACCEPTABLE_PASSWORD = RandomStringUtils.randomAlphanumeric(20);
  private static final UsernamePasswordAuthenticationToken OK_TOKEN =
      new UsernamePasswordAuthenticationToken(new Object(), new Object());
  @InjectMocks private JwtAuthenticationService jwtAuthenticationService;
  @Mock private AuthenticationManager authenticationManager;

  @Test
  public void whenAuthenticationManagerAuthenticates_thenAllOk() {
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(OK_TOKEN);
    Exception exc = null;
    try {
      jwtAuthenticationService.authenticate(USERNAME, ACCEPTABLE_PASSWORD);
    } catch (Exception thrown) {
      exc = thrown;
    }
    assertNull(exc, "Did not expect authentication method to throw.");
  }

  @Test(expected = Exception.class)
  public void whenAuthenticationManagerThrowsDisabledException_thenThrowException() {
    doThrow(new DisabledException("some message"))
        .when(authenticationManager)
        .authenticate(any(UsernamePasswordAuthenticationToken.class));
    jwtAuthenticationService.authenticate(USERNAME, ACCEPTABLE_PASSWORD);
  }

  @Test(expected = BadCredentialsException.class)
  public void whenAuthenticationManagerThrowsBadCredentialsException_thenThrowTheException(){
    doThrow(new BadCredentialsException("some message"))
        .when(authenticationManager)
        .authenticate(any(UsernamePasswordAuthenticationToken.class));
    jwtAuthenticationService.authenticate(USERNAME, ACCEPTABLE_PASSWORD);
  }
}
