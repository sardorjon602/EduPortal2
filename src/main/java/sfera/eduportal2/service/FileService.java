package sfera.eduportal2.service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import sfera.eduportal2.Exception.NotFoundException;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Repository.FileRepository;
import sfera.eduportal2.entity.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final RestTemplate restTemplate;

    @Value("${supabase.url}")
    private String url;

    @Value("${supabase.api_key}")
    private String apiKey;

    @Value("${supanase.bucket_name}")
    private String bucketName;


    private static final Path filePath = Paths.get("/src/main/resources");
//
//    public ApiResponse saveFile(MultipartFile file) {
//        String papka = checkingAttachment(file);
//
//        long timeMillis = System.currentTimeMillis();
//        Path resolve = filePath.resolve(papka + "/" + timeMillis + "_" + file.getOriginalFilename());
//        File files;
//        try{
//            Files.copy(file.getInputStream(), resolve, StandardCopyOption.REPLACE_EXISTING);
//            File file1 = new File();
//            file1.setFilePath(filePath.resolve(papka + "/" + timeMillis + "_" + file.getOriginalFilename()).toString());
//            file1.setFileName(file.getOriginalFilename());
//            file1.setSize(file.getSize());
//            file1.setContentType(file.getContentType());
//            files = fileRepository.save(file1);
//        }catch(IOException e){
//            throw  new NotFoundException(new ApiResponse("File not found", HttpStatus.NOT_FOUND,false,null));
//        }
//        return new ApiResponse("File saved successfully", HttpStatus.OK,false, files.getId());
//    }

//    public ResFile getAttachment(Long id) {
//        try{
//            Optional<File> byId = fileRepository.findById(id);
//            if(byId.isPresent()){
//                File file = byId.get();
//                if (file.getFileName()==null || file.getContentType()==null||file.getFilePath()==null){
//                    throw  new NotFoundException(new ApiResponse("Wrong file informations", HttpStatus.NOT_FOUND,false,null));
//                }
//                java.io.File files = new java.io.File(file.getFilePath());
//                if(!files.exists()){
//                    throw  new NotFoundException(new ApiResponse("File not found", HttpStatus.NOT_FOUND,false,null));
//                }
//                Resource resource = new UrlResource(files.toURI());
//                ResFile resFile = new ResFile();
//                resFile.setResource(resource);
//                HttpHeaders httpHeaders = new HttpHeaders();
//                httpHeaders.setContentType(MediaType.parseMediaType(file.getContentType()));
//                httpHeaders.setContentLength(file.getSize());
//                resFile.setHeaders(httpHeaders);
//                return resFile;
//            }else {
//                throw  new NotFoundException(new ApiResponse("File not found", HttpStatus.NOT_FOUND,false,null));
//            }
//        }catch(IOException e){
//            throw  new NotFoundException(new ApiResponse("Something went wrong", HttpStatus.BAD_REQUEST,false,null + e.getMessage()));
//        }
//    }
//    public String checkingAttachment(MultipartFile file) {
//        String fileName = file.getOriginalFilename();
//        if (fileName != null) {
//            if(fileName.endsWith(".pdf")||fileName.endsWith(".doc")||fileName.endsWith(".docx")||fileName.endsWith(".xlsx")||fileName.endsWith(".xls")||fileName.endsWith(".txt")) {
//                return "file";
//            }else if(fileName.endsWith(".png")||fileName.endsWith(".jpg")||fileName.endsWith(".jpeg")||fileName.endsWith(".webp")||fileName.endsWith(".PNG")||fileName.endsWith("JPG")) {
//                return "img";
//            }else {
//                throw  new NotFoundException(new ApiResponse("User not found", HttpStatus.NOT_FOUND,false,null));
//            }
//        }
//        return null;
//    }

    public CompletableFuture<String> uploadFile(MultipartFile file, String fileName) throws IOException {
        String uniqueName = LocalDateTime.now() + "_" + fileName;
        String path =  "uploads/" + uniqueName;

        String uploadUrl = url + "/storage/v1/object/" + bucketName + "/" + path;

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.parseMediaType(Objects.requireNonNull(file.getContentType())));
        httpHeaders.set("Authorization", "Bearer " + apiKey);

        HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(), httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(
                uploadUrl, HttpMethod.POST, entity, String.class
        );
        if (response.getStatusCode().is2xxSuccessful()) {
            return CompletableFuture.completedFuture(uploadUrl);
        }else {
            throw new NotFoundException(new ApiResponse("File not found", HttpStatus.NOT_FOUND,false,null));
        }
    }
}
