package com.example.charfinder.auth;

import com.example.charfinder.auth.token.RefreshRotationResult;
import com.example.charfinder.user.*;
import com.example.charfinder.auth.token.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshService;
    private final UserService userService;
    private final CookieService cookieService;


    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest req) {
        if (userService.findByEmail(req.email()).isPresent()) {
            return ResponseEntity.ok("Email already in use. Please log in.");
        }
        User user = userService.createUser(req.email(), req.password()); // searches for email a second time. Maybe unnecessary

        String rawRefresh = refreshService.generateSecureRandomToken();
        refreshService.issue(user, rawRefresh);

        return ResponseEntity.ok("User registered");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));

        User user = userService.findByEmail(req.email()).orElseThrow();
        String accessToken = jwtService.generateAccessToken(user);

        String rawRefreshToken = refreshService.generateSecureRandomToken();
        refreshService.issue(user, rawRefreshToken);
        ResponseCookie refreshCookie = cookieService.buildRefreshTokenCookie(rawRefreshToken);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new AuthResponse(accessToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest req, @CookieValue("refresh_token") String refreshToken) {
        User user = userService.findByEmail(req.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        String accessToken = jwtService.generateAccessToken(user);

        RefreshRotationResult resultRefreshToken = refreshService.validateAndRotate(user, refreshToken);
        ResponseCookie refreshCookie = cookieService.buildRefreshTokenCookie(resultRefreshToken.rawToken());

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new AuthResponse(accessToken));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<?> logout(@RequestBody LogoutRequest req) {
        User user = userService.findByEmail(req.email()).orElseThrow();
        refreshService.revokeAll(user);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, cookieService.clearRefreshTokenCookie().toString())
                .body("successfully logged out.");
    }
}

record SignupRequest(String email, String password) {}
record LoginRequest(String email, String password) {}
record RefreshRequest(String email) {}
record LogoutRequest(String email) {}
record AuthResponse(String accessToken) {}