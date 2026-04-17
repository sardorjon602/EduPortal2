package sfera.eduportal2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.request.ReqCategory;
import sfera.eduportal2.service.CategoryService;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor




public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/list")
    public ApiResponse getAll() {
        return categoryService.findAll();
    }


    @GetMapping("/{id}")
    public ApiResponse getById(@PathVariable Long id) {
        return categoryService.findById(id);
    }



    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse create(@RequestBody ReqCategory requestCategory) {
        return categoryService.save(requestCategory);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse update(@PathVariable Long id, @RequestBody ReqCategory requestCategory) {
        return categoryService.update(id, requestCategory);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse delete(@PathVariable Long id) {
        return categoryService.delete(id);
    }
}

