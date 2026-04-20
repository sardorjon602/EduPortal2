package sfera.eduportal2.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.request.AuthLogin;
import sfera.eduportal2.Payload.request.AuthRegister;
import sfera.eduportal2.service.AuthService;
import sfera.eduportal2.service.UserService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Level -> JUNIOR, MIDDLE, SENIOR")
    public ResponseEntity<ApiResponse> register(@RequestBody AuthRegister authRegister) {
        return ResponseEntity.ok(authService.register(authRegister));
    }

    @PutMapping("/activate")
    public ResponseEntity<ApiResponse> activate(@RequestParam Long code) {
        return ResponseEntity.ok(authService.activateUser(code));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody AuthLogin authLogin) {
        return ResponseEntity.ok(authService.login(authLogin));
    }

    // 4. Forgot password — email yuboradi
    @PostMapping("/forgot-password")
    @Operation(summary = "Parolni unutdim — emailga kod yuboriladi")
    public ResponseEntity<ApiResponse> forgotPassword(@RequestParam String email) {
        return ResponseEntity.ok(userService.forgotPassword(email));
    }

    // 4. Reset password — kod va yangi parol
    @PutMapping("/reset-password")
    @Operation(summary = "Yangi parol o'rnatish — kod va yangi parol")
    public ResponseEntity<ApiResponse> resetPassword(
            @RequestParam Long code,
            @RequestParam String newPassword) {
        return ResponseEntity.ok(userService.resetPassword(code, newPassword));
    }
}