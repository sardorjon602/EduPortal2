package sfera.eduportal2.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.request.AuthRegister;
import sfera.eduportal2.Payload.request.ReqUser;
import sfera.eduportal2.Security.CurrentUser;
import sfera.eduportal2.entity.Users;
import sfera.eduportal2.service.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor

public class UserController {


    private final UserService userService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get all users - Admin only",
            description = "Returns a list of all registered users")
    public ResponseEntity<ApiResponse> getAllUsers() {
        ApiResponse apiResponse = userService.getAllUsers();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/by-email")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Find user by email - Admin only",
            description = "Returns user details by email address")
    public ResponseEntity<ApiResponse> getUserByEmail(@RequestParam String email) {
        ApiResponse apiResponse = userService.getUserByEmail(email);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getMe(@CurrentUser Users user) {
        ApiResponse me = userService.getMe(user);
        return ResponseEntity.ok(me);
    }


    @PostMapping("/teacher-save")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "This API is for Admin only. ",
            description = "Teacher data is stored through this API")
    public ResponseEntity<ApiResponse> teacherSave(@RequestBody AuthRegister authRegister) {
        ApiResponse teacher = userService.teacherSave(authRegister);
        return ResponseEntity.ok(teacher);
    }


    @PutMapping("/update")
    public ResponseEntity<ApiResponse> update(@CurrentUser Users user, @RequestBody ReqUser reqUser){
        ApiResponse apiResponse = userService.updateUser(user, reqUser);
        return ResponseEntity.ok(apiResponse);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long id) {
        ApiResponse apiResponse = userService.deleteUser(id);
        return ResponseEntity.ok(apiResponse);

    }


}
