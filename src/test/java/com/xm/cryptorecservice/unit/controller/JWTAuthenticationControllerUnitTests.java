package com.xm.cryptorecservice.unit.controller;

import com.xm.cryptorecservice.controller.JwtAuthenticationController;
import com.xm.cryptorecservice.model.jwt.JwtRequest;
import com.xm.cryptorecservice.model.jwt.JwtResponse;
import com.xm.cryptorecservice.model.user.UserDto;
import com.xm.cryptorecservice.service.jwt.JwtAuthenticationService;
import com.xm.cryptorecservice.service.jwt.JwtUserDetailsService;
import com.xm.cryptorecservice.unit.TestUserDetailsImpl;
import com.xm.cryptorecservice.util.jwt.JwtTokenUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JWTAuthenticationControllerUnitTests {
    private static final TestUserDetailsImpl TEST_USER_DETAILS =
            new TestUserDetailsImpl("username", "password");
    private static final JwtRequest TEST_JWT_REQUEST = new JwtRequest("username", "password");
    @InjectMocks
    private JwtAuthenticationController jwtAuthenticationController;
    @Mock
    private JwtTokenUtil jwtTokenUtil;
    @Mock private JwtUserDetailsService userDetailsService;
    @Mock private JwtAuthenticationService jwtAuthenticationService;

    @Test
    public void whenUserIsAuthenticatedInDB_thenReturnNewToken(){
        doNothing().when(jwtAuthenticationService).authenticate(anyString(), anyString());
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(TEST_USER_DETAILS);
        when(jwtTokenUtil.generateToken(TEST_USER_DETAILS)).thenReturn("token");
        assertEquals(
                Objects.requireNonNull(
                        jwtAuthenticationController.authenticate(TEST_JWT_REQUEST).getBody()),
                new JwtResponse("token"));
    }

    @Test
    public void whenUserRegistersWithAUsernameWithLeadingAndTrailingWhitespace_thenReturnedUserDetailsHasTheUsernameTrimmed(){
        UserDto userDto = new UserDto(" max    ", "maxpassword");
        UserDto expectedUserDto = new UserDto("max" , "maxpassword"); // The controller does not actually ever return the password, but that's fine for this unit test.
        when(userDetailsService.save(userDto)).thenAnswer(invocationOnMock -> {
            UserDto providedUserDto = invocationOnMock.getArgument(0);
            return new UserDto(providedUserDto.getEmail().trim(), providedUserDto.getPassword());
        });
        assertEquals(new ResponseEntity<>(expectedUserDto, HttpStatus.CREATED),
                jwtAuthenticationController.registerUser(userDto));
    }
}
