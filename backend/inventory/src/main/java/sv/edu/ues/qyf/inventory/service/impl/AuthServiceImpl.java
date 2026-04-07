package sv.edu.ues.qyf.inventory.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import sv.edu.ues.qyf.inventory.dto.LoginRequestDto;
import sv.edu.ues.qyf.inventory.dto.LoginResponseDto;
import sv.edu.ues.qyf.inventory.security.JwtService;
import sv.edu.ues.qyf.inventory.security.UserPrincipal;
import sv.edu.ues.qyf.inventory.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public LoginResponseDto login(LoginRequestDto request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername().trim(),
                            request.getPassword()));

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            return LoginResponseDto.builder()
                    .token(jwtService.generateToken(userPrincipal))
                    .username(userPrincipal.getUsername())
                    .role(userPrincipal.getRole())
                    .fullName(userPrincipal.getFullName())
                    .accessScope(userPrincipal.getAccessScope())
                    .build();
        } catch (AuthenticationException exception) {
            throw new BadCredentialsException("Invalid username or password", exception);
        }
    }
}
