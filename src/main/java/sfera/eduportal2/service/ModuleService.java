package sfera.eduportal2.service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.request.ReqModule;
import sfera.eduportal2.Payload.response.ResModule;
import sfera.eduportal2.Repository.CategoryRepository;
import sfera.eduportal2.Repository.ModuleRepository;
import sfera.eduportal2.entity.Category;
import sfera.eduportal2.entity.Module;
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

            ResModule resModule = ResModule.builder()
                    .id(module.getId())
                    .moduleName(module.getModuleName())
                    .categoryName(module.getCategory().getName())
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

        ResModule resModule = ResModule.builder()
                .id(module.getId())
                .moduleName(module.getModuleName())
                .categoryName(module.getCategory().getName())
                .build();

        return ApiResponse.builder()
                .message("Success")
                .success(true)
                .status(HttpStatus.OK)
                .body(resModule)
                .build();
    }


    public ApiResponse save(ReqModule reqModule) {


        boolean exists =
                moduleRepository.existsByModuleNameIgnoreCase(
                        reqModule.getTitle()
                );

        if (exists) {

            return ApiResponse.builder()
                    .message("Bunday module mavjud")
                    .success(false)
                    .status(HttpStatus.CONFLICT)
                    .build();
        }

        Optional<Category> optionalCategory =
                categoryRepository.findById(
                        reqModule.getCategoryId()
                );

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

        moduleRepository.save(module);

        return ApiResponse.builder()
                .message("Module saqlandi")
                .success(true)
                .status(HttpStatus.CREATED)
                .build();
    }


    public ApiResponse update(Long id, ReqModule reqModule) {

        Optional<Module> optionalModule =
                moduleRepository.findById(id);

        if (optionalModule.isEmpty()) {

            return ApiResponse.builder()
                    .message("Module topilmadi")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        boolean exists =
                moduleRepository.existsByModuleNameIgnoreCaseAndIdNot(
                        reqModule.getTitle(),
                        id
                );

        if (exists) {

            return ApiResponse.builder()
                    .message("Module nomi band")
                    .success(false)
                    .status(HttpStatus.CONFLICT)
                    .build();
        }

        Optional<Category> optionalCategory =
                categoryRepository.findById(
                        reqModule.getCategoryId()
                );

        if (optionalCategory.isEmpty()) {

            return ApiResponse.builder()
                    .message("Category topilmadi")
                    .success(false)
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        Module module = optionalModule.get();

        module.setModuleName(reqModule.getTitle());
        module.setCategory(optionalCategory.get());

        moduleRepository.save(module);

        return ApiResponse.builder()
                .message("Module yangilandi")
                .success(true)
                .status(HttpStatus.OK)
                .build();
    }


    public ApiResponse delete(Long id) {

        Optional<Module> optionalModule =
                moduleRepository.findById(id);

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

}