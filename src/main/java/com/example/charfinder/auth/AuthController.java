package com.example.charfinder.auth;

import com.example.charfinder.token.RefreshRotationResult;
import com.example.charfinder.user.UserRepository;
import com.example.charfinder.token.RefreshTokenService;
import com.example.charfinder.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        User user = userRepo.findByEmail(req.email()).orElseThrow();
        String access = jwtService.generateAccessToken(user);

        String rawRefresh = refreshService.generateSecureRandomToken();
        refreshService.issue(user, rawRefresh);

        return new AuthResponse(access, rawRefresh);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody RefreshRequest req) {
        User user = userRepo.findByEmail(req.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        RefreshRotationResult result = refreshService.validateAndRotate(user, req.refreshToken());

        String newAccess = jwtService.generateAccessToken(user);
        return new AuthResponse(newAccess, result.rawToken());
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