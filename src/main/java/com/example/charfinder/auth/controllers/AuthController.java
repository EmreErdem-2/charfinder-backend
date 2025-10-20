package com.example.charfinder.auth.controllers;

import com.example.charfinder.auth.UserRepository;
import com.example.charfinder.auth.services.JwtService;
import com.example.charfinder.auth.services.RefreshTokenService;
import com.example.charfinder.auth.tables.RefreshToken;
import com.example.charfinder.auth.tables.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshService;
    private final UserRepository userRepo;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        User user = userRepo.findByEmail(req.email()).orElseThrow();
        String access = jwtService.generateAccessToken(user);

        String rawRefresh = refreshService.generateSecureRandomToken();
        refreshService.issue(user, rawRefresh);

        return new AuthResponse(access, rawRefresh);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody RefreshRequest req) {
        User user = userRepo.findByEmail(req.email()).orElseThrow();
        Optional<RefreshToken> rotated = refreshService.validateAndRotate(user, req.refreshToken());
        if (rotated.isEmpty()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");

        String newAccess = jwtService.generateAccessToken(user);
        String newRawRefresh = rotated.get().getReplacedBy() != null
                ? null // handled by service; return newly issued token
                : refreshService.generateSecureRandomToken(); // should not happen in rotation path

        // Better: have validateAndRotate return the newly issued raw token alongside entity
        String issuedRawRefresh = refreshService.generateSecureRandomToken();
        RefreshToken next = refreshService.issue(user, issuedRawRefresh);

        return new AuthResponse(newAccess, issuedRawRefresh);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestBody LogoutRequest req) {
        User user = userRepo.findByEmail(req.email()).orElseThrow();
        refreshService.revokeAll(user);
    }
}

record LoginRequest(String email, String password) {}
record RefreshRequest(String email, String refreshToken) {}
record LogoutRequest(String email) {}
record AuthResponse(String accessToken, String refreshToken) {}