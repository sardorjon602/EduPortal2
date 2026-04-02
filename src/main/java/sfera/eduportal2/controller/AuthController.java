package sfera.eduportal2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.request.AuthLogin;
import sfera.eduportal2.Payload.request.AuthRegister;
import sfera.eduportal2.service.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor

public class AuthController {


    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody AuthRegister authRegister){
        ApiResponse register = authService.register(authRegister);
        return ResponseEntity.ok(register);
    }

    @PutMapping("/activate")
    public ResponseEntity<ApiResponse> activate(@RequestParam Long code){
        ApiResponse apiResponse = authService.activateUser(code);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody AuthLogin authLogin){
        ApiResponse apiResponse = authService.login(authLogin);
        return ResponseEntity.ok(apiResponse);
    }


}
