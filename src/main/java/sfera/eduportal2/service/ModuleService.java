package sfera.eduportal2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.request.RequestModule;
import sfera.eduportal2.Payload.response.ResModule;
import sfera.eduportal2.Payload.response.ResQuestions;
import sfera.eduportal2.Repository.CategoryRepository;
import sfera.eduportal2.Repository.ModuleRepository;
import sfera.eduportal2.entity.Category;
import sfera.eduportal2.entity.Module;
import sfera.eduportal2.entity.Questions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final CategoryRepository categoryRepository;

    public ApiResponse findAll() {

        List<Module> modules = moduleRepository.findAll();

        List<ResModule> result = new ArrayList<>();

        for (Module module : modules) {

            // BUG FIX: category null bo'lsa NPE chiqardi — endi xavfsiz
            String categoryName = (module.getCategory() != null)
                    ? module.getCategory().getName()
                    : "Noma'lum";

            ResModule resModule = ResModule.builder()
                    .id(module.getId())
                    .moduleName(module.getModuleName())
                    .categoryName(categoryName)
                    .build();

            result.add(resModule);
        }

        return ApiResponse.builder()
                .message("Success")
                .success(true)
                .status(HttpStatus.OK)
                .body(result)
                .build();
    }

    public ApiResponse findById(Long id) {

        Optional<Module> optionalModule = moduleRepository.findById(id);

        if (optionalModule.isEmpty()) {
            return ApiResponse.builder()
                    .message("Module topilmadi")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        Module module = optionalModule.get();

        // BUG FIX: category null bo'lsa NPE chiqardi — endi xavfsiz
        String categoryName = (module.getCategory() != null)
                ? module.getCategory().getName()
                : "Noma'lum";

        ResModule resModule = ResModule.builder()
                .id(module.getId())
                .moduleName(module.getModuleName())
                .categoryName(categoryName)
                .build();

        return ApiResponse.builder()
                .message("Success")
                .success(true)
                .status(HttpStatus.OK)
                .body(resModule)
                .build();
    }

    public ApiResponse save(RequestModule reqModule) {

        boolean exists = moduleRepository.existsByModuleNameIgnoreCase(reqModule.getTitle());

        if (exists) {
            return ApiResponse.builder()
                    .message("Module already exists")
                    .success(false)
                    .status(HttpStatus.CONFLICT)
                    .build();
        }

        Optional<Category> optionalCategory = categoryRepository.findById(reqModule.getCategoryId());

        if (optionalCategory.isEmpty()) {
            return ApiResponse.builder()
                    .message("Category topilmadi")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        Module module = Module.builder()
                .moduleName(reqModule.getTitle())
                .category(optionalCategory.get())
                .build();

        // BUG FIX: .save() chaqirilmagan edi — module saqlanmayotgan edi
        moduleRepository.save(module);

        return ApiResponse.builder()
                .message("Module saqlandi")
                .success(true)
                .status(HttpStatus.OK)
                .body(toResModule(module))
                .build();
    }

    public ApiResponse update(Long id, RequestModule reqModule) {

        Optional<Module> optionalModule = moduleRepository.findById(id);

        if (optionalModule.isEmpty()) {
            return ApiResponse.builder()
                    .message("Module topilmadi")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        // BUG FIX: existsByModuleNameIgnoreCase o'zini ham bloklayotgan edi
        // existsByModuleNameIgnoreCaseAndIdNot ishlatilishi kerak
        boolean exists = moduleRepository.existsByModuleNameIgnoreCaseAndIdNot(
                reqModule.getTitle(), id
        );

        if (exists) {
            return ApiResponse.builder()
                    .message("Module nomi band")
                    .success(false)
                    .status(HttpStatus.CONFLICT)
                    .build();
        }

        Optional<Category> optionalCategory = categoryRepository.findById(reqModule.getCategoryId());

        if (optionalCategory.isEmpty()) {
            return ApiResponse.builder()
                    .message("Category topilmadi")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        Module module = optionalModule.get();

        // BUG FIX: getContent() emas, getTitle() bo'lishi kerak
        module.setModuleName(reqModule.getTitle());
        module.setCategory(optionalCategory.get());

        // BUG FIX: .save() chaqirilmagan edi — o'zgarish saqlanmayotgan edi
        moduleRepository.save(module);

        return ApiResponse.builder()
                .message("Module yangilandi")
                .success(true)
                .status(HttpStatus.OK)
                .body(toResModule(module))
                .build();
    }

    public ApiResponse delete(Long id) {

        Optional<Module> optionalModule = moduleRepository.findById(id);

        if (optionalModule.isEmpty()) {
            return ApiResponse.builder()
                    .message("Module topilmadi")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        moduleRepository.delete(optionalModule.get());

        return ApiResponse.builder()
                .message("Module o'chirildi")
                .success(true)
                .status(HttpStatus.OK)
                .build();
    }
    private ResModule toResModule(Module module) {
        return ResModule.builder()
                .id(module.getId())
                .moduleName(module.getModuleName())
                .categoryName(module.getCategory().getName())
                .build();
    }
}