package com.project.paymentgateway.payment_gateway_service.controller;

import com.project.paymentgateway.payment_gateway_service.dao.User;
import com.project.paymentgateway.payment_gateway_service.dto.AuthResponse;
import com.project.paymentgateway.payment_gateway_service.dto.LoginRequest;
import com.project.paymentgateway.payment_gateway_service.repository.UserRepo;
import com.project.paymentgateway.payment_gateway_service.service.TokenService;
import com.project.paymentgateway.payment_gateway_service.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserService userService;
    private final UserRepo userRepo;

    public AuthController(AuthenticationManager authenticationManager, TokenService tokenService, UserService userService, UserRepo userRepo) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.userService = userService;
        this.userRepo = userRepo;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            boolean isInMemoryUser = false;
            if(!userService.validateUser(loginRequest.getUsername())){
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
                isInMemoryUser = true;
            }
            String token = tokenService.generateToken(loginRequest.getUsername(), isInMemoryUser);
            if(!isInMemoryUser){
                User loginUser = userRepo.findByUserName(loginRequest.getUsername());
                loginUser.setLastSuccessfulLogin(LocalDateTime.now());
                userRepo.save(loginUser);
            }
            return ResponseEntity.ok(new AuthResponse(token));
        }
        catch (Exception e) {
            User loginUser = userRepo.findByUserName(loginRequest.getUsername());
            if(loginUser != null){
                loginUser.setFailedLoginCount(loginUser.getFailedLoginCount()+1);
                userRepo.save(loginUser);
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    @GetMapping("/validateToken")
    public String getResponse(HttpServletRequest request){
        if(tokenService.validateToken(request)){
            return "Valid Token";
        } else {
            return "InValid Token";
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> test(Authentication auth) {
        System.out.println("User: " + auth.getName());
        System.out.println("Authorities: " + auth.getAuthorities());
        return ResponseEntity.ok("Authenticated as " + auth.getName());
    }
}

