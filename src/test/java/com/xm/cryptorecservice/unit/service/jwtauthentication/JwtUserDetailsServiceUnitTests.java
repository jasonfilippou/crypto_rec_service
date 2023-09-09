package com.xm.cryptorecservice.unit.service.jwtauthentication;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.xm.cryptorecservice.model.user.UserDto;
import com.xm.cryptorecservice.model.user.UserEntity;
import com.xm.cryptorecservice.persistence.UserRepository;
import com.xm.cryptorecservice.service.jwt.JwtUserDetailsService;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

/**
 * Unit tests for {@link JwtUserDetailsService}. Perform extensive use of Mockito and jUnit4 assertions.
 * 
 * @author jason 
 */
@RunWith(MockitoJUnitRunner.class)
public class JwtUserDetailsServiceUnitTests {

  public static final String EMAIL = "plainoljoe@company.com";
  public static final String PASSWORD = "plainoljoepass";
  private static final UserEntity USER_ENTITY = new UserEntity(EMAIL, PASSWORD);
  private static final UserDto USER_DTO = new UserDto(EMAIL, PASSWORD);
  @InjectMocks private JwtUserDetailsService jwtUserDetailsService;
  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;

  @Test
  public void whenUserIsInDB_thenUserDetailsReturned() {
    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(USER_ENTITY));
    UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(EMAIL);
    assertEquals(userDetails.getUsername(), EMAIL);
    assertEquals(userDetails.getPassword(), PASSWORD);
  }

  @Test(expected = UsernameNotFoundException.class)
  public void whenUserIsNotInDB_thenUsernameNotFoundExceptionIsThrown() {
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    jwtUserDetailsService.loadUserByUsername(RandomStringUtils.randomAlphanumeric(10));
  }

  @Test
  public void whenSavingNewAdminUser_thenTheirInformationIsReturned() {
    when(passwordEncoder.encode(any(CharSequence.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0)); // Encoder basically does nothing.
    when(userRepository.save(any())).thenReturn(USER_ENTITY);
    assertEquals(USER_DTO, jwtUserDetailsService.save(USER_DTO));
  }
  
  @Test
  public void whenSavingNewUserWithTrailingAndLeadingWhitespaceInUsername_thenThatUsernameIsTrimmed(){
    UserDto userDto = new UserDto(" max    ", "maxpassword");
    UserDto expectedUserDto = new UserDto("max" , "maxpassword");
    when(passwordEncoder.encode(any(CharSequence.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
    when(userRepository.save(any(UserEntity.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
    Assertions.assertEquals(expectedUserDto, jwtUserDetailsService.save(userDto));
  }
}
