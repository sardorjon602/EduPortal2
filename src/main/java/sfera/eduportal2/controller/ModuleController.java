package sfera.eduportal2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.request.ReqModule;
import sfera.eduportal2.service.ModuleService;

@RestController
@RequestMapping("module")
@RequiredArgsConstructor
public class ModuleController {
    private final ModuleService moduleService;


    @GetMapping("/all")
    public ApiResponse findAll(){
        return moduleService.findAll();
    }

    @GetMapping("{id}")
    public ApiResponse findById(@PathVariable Long id){
        return moduleService.findById(id);
    }

    @PostMapping("/save")
    public ApiResponse save (@RequestBody ReqModule requestModule){
        return moduleService.save(requestModule);
    }

    @PutMapping("/update/{id}")
    public ApiResponse update(@PathVariable Long id,@RequestBody ReqModule requestModule){
    return moduleService.update(id,requestModule);
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse delete (@PathVariable Long id){
        return moduleService.delete(id);
    }
}
