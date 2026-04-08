package sfera.eduportal2.service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import sfera.eduportal2.Exception.NotFoundException;
import sfera.eduportal2.Payload.ApiResponse;
import sfera.eduportal2.Payload.response.ResFile;
import sfera.eduportal2.Repository.FileRepository;
import sfera.eduportal2.entity.File;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor

public class FileService {
    private final FileRepository fileRepository;

    @Value("${supabase.url}")
    private String url;

    @Value("${supabase.api_key}")
    private String apiKey;

    @Value("${supabase.bucket_name}")
    private String bucketName;

    private final RestTemplate restTemplate;


    private static final Path filePath = Paths.get("src/main/resources");

    public ApiResponse saveFile(MultipartFile file){
        String papka = checkingAttachmentFile(file);

        long timeMillis = System.currentTimeMillis();
        Path resolve = filePath.resolve(papka + "/" + timeMillis + "_"  + file.getOriginalFilename());
        File files;
        try{
            Files.copy(file.getInputStream(), resolve,  StandardCopyOption.REPLACE_EXISTING);
            File file1 = new File();
            file1.setFilePath(filePath.resolve(papka + "/" + timeMillis + "_"  + file.getOriginalFilename()).toString());
            file1.setFileName(file.getOriginalFilename());
            file1.setContentType(file.getContentType());
            file1.setSize(file.getSize());
            files = fileRepository.save(file1);

        }catch (IOException e){
            throw new NotFoundException(e.getMessage());
        }
        return new ApiResponse("Success", HttpStatus.OK, true, files.getId());

    }
    ////       GET FILE uchun
    public ResFile getAttachment(Long id){
        try{
            Optional<File> byId = fileRepository.findById(id);
            if(byId.isPresent()){
                File file = byId.get();
                if (file.getFileName() == null || file.getFilePath() == null || file.getContentType() == null ){
                    throw new NotFoundException("File incorrect ");
                }
                java.io.File files = new java.io.File(file.getFilePath());
                if (!files.exists()){
                    throw new NotFoundException("File not found ");
                }
                Resource resource = new UrlResource(files.toURI());
                ResFile resFile = new ResFile();
                resFile.setResource(resource);
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.setContentType(MediaType.parseMediaType(file.getContentType()));
                httpHeaders.setContentLength(file.getSize());
                resFile.setHeaders(httpHeaders);
                return resFile;
            }else {
                throw new NotFoundException("File not found ");
            }
        }catch (IOException e){
            throw new NotFoundException(e.getMessage());
        }

    }


    public String checkingAttachmentFile(MultipartFile file){
        String fileName = file.getOriginalFilename();
        if (fileName != null) {
            if (fileName.endsWith(".pdf") || fileName.endsWith(".doc")
                    || fileName.endsWith(".docx") || fileName.endsWith(".xls")
                    || fileName.endsWith(".xlsx") ||  fileName.endsWith(".txt")) {
                return "file";
            } else if (fileName.endsWith(".png") || fileName.endsWith(".img")  ||
                    fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
                    fileName.endsWith(".JPG") || fileName.endsWith(".webp")) {
                return "img";

            }else {
                throw new NotFoundException("File Not Found");
            }
        }
        return null;
    }


    public CompletableFuture<String> uploadFile(MultipartFile file, String fileName) throws IOException {
        String uniqueName = LocalDateTime.now() + "_" + fileName;
        String path = "upload/" + uniqueName;

        String uploadUrl = String.format("%s/storage/v1/object/%s/%s",  url, bucketName, path );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(Objects.requireNonNull(file.getContentType())));
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<byte[]>  entity = new HttpEntity<>(file.getBytes(), headers);

        ResponseEntity<String> response = restTemplate.exchange(
                uploadUrl, HttpMethod.POST, entity, String.class
        );

        if (response.getStatusCode().is2xxSuccessful()){
            return CompletableFuture.completedFuture(url + "storage/v1/object/" + bucketName + "/" + path);
        }else {
            throw new NotFoundException("File not found ");
        }
    }


}
