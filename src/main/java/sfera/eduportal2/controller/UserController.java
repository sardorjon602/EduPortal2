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

    // 2. Filtr qo'shildi — name, phone, email optional
    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get all users - Admin only",
            description = "name, email, phone bo'yicha filtr qo'llanilishi mumkin")
    public ResponseEntity<ApiResponse> getAllUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone) {
        ApiResponse apiResponse = userService.getAllUsers(name, email, phone);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getMe(@CurrentUser Users user) {
        return ResponseEntity.ok(userService.getMe(user));
    }

    @PostMapping("/teacher-save")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Admin uchun — Teacher qo'shish")
    public ResponseEntity<ApiResponse> teacherSave(@RequestBody AuthRegister authRegister) {
        return ResponseEntity.ok(userService.teacherSave(authRegister));
    }

    // 3. Admin userni update qilishi — mavjud
    @PutMapping("/update")
    public ResponseEntity<ApiResponse> update(
            @CurrentUser Users user,
            @RequestBody ReqUser reqUser) {
        return ResponseEntity.ok(userService.updateUser(user, reqUser));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deleteUser(id));
    }

    }
