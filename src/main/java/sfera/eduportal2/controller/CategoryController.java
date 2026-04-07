package sfera.eduportal2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.request.ReqCategory;
import sfera.eduportal2.service.CategoryService;

@RestController
@RequestMapping("category")
@RequiredArgsConstructor




public class CategoryController {
    private final CategoryService CategoryService;
    @GetMapping
    public ApiResponse getAll() {
        return CategoryService.findAll();
    }
    @GetMapping("/{id}")
    public ApiResponse getById(@PathVariable Long id) {
        return CategoryService.findById(id);
    }
    @PostMapping
    public ApiResponse create(@RequestBody ReqCategory requestCategory) {
        return CategoryService.save(requestCategory);
    }

    @PutMapping("/{id}")
    public ApiResponse update(@PathVariable Long id, @RequestBody ReqCategory requestCategory) {
        return CategoryService.update(id, requestCategory);
    }

    @DeleteMapping("/{id}")
    public ApiResponse delete(@PathVariable Long id) {
        return CategoryService.delete(id);
    }
}

