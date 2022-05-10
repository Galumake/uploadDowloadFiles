package com.example.uploadanddownload.web;

import com.example.uploadanddownload.service.FileStorageService;
import com.example.uploadanddownload.service.dto.FileUploadResponse;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;




// Para descargar y subir archivos en carpeta local en proyecto
// fuente: https://www.youtube.com/watch?v=LUq4UtsGcyU&list=PLq3uEqRnr_2H2KjoG2WHY9Yzfq-ZkzN3X&index=1
@RestController
public class UploadDownloadWithFileSystemController {

    @Autowired
    FileStorageService fileStorageService;

    // Sube un archivo en una posicion especifica del proyecto
    @PostMapping("single/upload")
    FileUploadResponse singleFileUpload(@RequestParam("file")  MultipartFile file){

        // Este es el servicio que almacena el archivo localmente en el proyecto
        String fileName = fileStorageService.storageFile(file);

        // http://localhost:8082/[download/abc.jpg] <-----
        // esta accion lo que hace es crear un URL para descargar el archivo
        // almacenado en localmente en el proyecto
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/download/")
                .path(fileName)
                .toUriString();

        String contentType = file.getContentType();

        // Esta clase un un DTO hecho en casa
        FileUploadResponse response = new FileUploadResponse(fileName, contentType, url);

        return response;

    }

    // obtenemos el archivo almacenado localmente
    @GetMapping("/download/{fileName}")
    ResponseEntity<Resource> downLoadSingleFile(@PathVariable String fileName){

        // Resuelve el argumento como un URI
        Resource resource = (Resource) fileStorageService.downloadFile(fileName);

        // para este ejemplo definimos tipo JPEG
        MediaType contentType = MediaType.IMAGE_JPEG;

        // devuelve el URI como un attachment
        return  ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders
                        .CONTENT_DISPOSITION,
                        "attachment;fileName="+resource.getFilename())
                .body(resource);

    }
}
