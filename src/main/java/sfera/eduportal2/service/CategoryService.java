package sfera.eduportal2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.request.ReqCategory;
import sfera.eduportal2.Payload.response.ResCategory;
import sfera.eduportal2.Repository.CategoryRepository;
import sfera.eduportal2.entity.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;


    public ApiResponse findAll() {
        List<Category> all = categoryRepository.findAll();
        List<ResCategory> resList = new ArrayList<>();

        for (Category category : all) {
            ResCategory res = ResCategory.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .questionCount(category.getQuestionCount())
                    .build();
            resList.add(res);
        }

        return ApiResponse.builder()
                .message("Success")
                .success(true)
                .status(HttpStatus.OK)
                .body(resList)
                .build();
    }


    public ApiResponse findById(Long id) {
        Optional<Category> optional = categoryRepository.findById(id);
        if (optional.isEmpty()) {
            return ApiResponse.builder()
                    .message("Category not found")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        Category category = optional.get();
        ResCategory res = ResCategory.builder()
                .id(category.getId())
                .name(category.getName())
                .questionCount(category.getQuestionCount())
                .build();

        return ApiResponse.builder()
                .message("Success")
                .success(true)
                .status(HttpStatus.OK)
                .body(res)
                .build();
    }

    public ApiResponse save(ReqCategory requestCategory) {
        boolean exists = categoryRepository.existsByNameIgnoreCase(requestCategory.getName());
        if (exists) {
            return ApiResponse.builder()
                    .message("Category already exists")
                    .success(false)
                    .status(HttpStatus.CONFLICT)
                    .build();
        }

        Category category = Category.builder()
                .name(requestCategory.getName())
                .questionCount(requestCategory.getQuestionCount())
                .build();

        categoryRepository.save(category);

        return ApiResponse.builder()
                .message("Category successfully saved")
                .success(true)
                .status(HttpStatus.CREATED)
                .build();
    }


    public ApiResponse update(Long id, ReqCategory requestCategory) {
        Optional<Category> optional = categoryRepository.findById(id);
        if (optional.isEmpty()) {
            return ApiResponse.builder()
                    .message("Category not found")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        boolean exists = categoryRepository.existsByNameIgnoreCaseAndIdNot(requestCategory.getName(), id);
        if (exists) {
            return ApiResponse.builder()
                    .message("Category with this name already exists")
                    .success(false)
                    .status(HttpStatus.CONFLICT)
                    .build();












        }

        Category category = optional.get();
        category.setName(requestCategory.getName());
        category.setQuestionCount(requestCategory.getQuestionCount());
        categoryRepository.save(category);

        return ApiResponse.builder()
                .message("Category successfully updated")
                .success(true)
                .status(HttpStatus.OK)
                .build();
    }

    public ApiResponse delete(Long id) {
        Optional<Category> optional = categoryRepository.findById(id);
        if (optional.isEmpty()) {
            return ApiResponse.builder()
                    .message("Category not found")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        categoryRepository.delete(optional.get());

        return ApiResponse.builder()
                .message("Category successfully deleted")
                .success(true)
                .status(HttpStatus.OK)
                .build();
    }
}