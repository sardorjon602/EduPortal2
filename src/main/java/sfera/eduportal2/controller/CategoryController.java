package sfera.eduportal2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.request.RequestCategory;
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
    @PostMapping("/save")
    public ApiResponse create(@RequestBody RequestCategory requestCategory) {
        return categoryService.save(requestCategory);
    }

    @PutMapping("/update/{id}")
    public ApiResponse update(@PathVariable Long id, @RequestBody RequestCategory requestCategory) {
        return categoryService.update(id, requestCategory);
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse delete(@PathVariable Long id) {
        return categoryService.delete(id);
    }

}

