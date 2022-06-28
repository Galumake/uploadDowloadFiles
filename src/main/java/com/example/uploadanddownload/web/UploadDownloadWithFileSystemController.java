package com.example.uploadanddownload.web;

import com.example.uploadanddownload.service.FileStorageService;
import com.example.uploadanddownload.service.dto.FileUploadResponse;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;



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

    // obtenemos el archivo almacenado localmente.
    @GetMapping("/download/{fileName}")
    ResponseEntity<Resource> downLoadSingleFile(@PathVariable String fileName, HttpServletRequest request){

        // Resuelve el argumento como un URI
        Resource resource = fileStorageService.downloadFile(fileName);

        // Se define el tipo de archivo
        String mimeType;
        try {
            // dinamicamente establese el tipo de MIME
            mimeType = request.getServletContext()
                    .getMimeType(
                            resource.getFile().getAbsolutePath()
                    );
        } catch (IOException e) {
            mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        //MediaType contentType = MediaType.IMAGE_JPEG;

        // devuelve el URI como un attachment
        /* Hay dos tipos de CONTENT DISPOSITION
        * INLINE: el resultado lo renderiza en el browser
        * ATTACHMENT: el resultado lo descarga del browser*/
        return  ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders
                        .CONTENT_DISPOSITION,
                        "attachment;fileName="+resource.getFilename())
                        //"inline;fileName="+resource.getFilename())
                .body(resource);
    }

    // carga barios archivos
    @PostMapping("/multiple/upload")
    List<FileUploadResponse> multipleUpload(@RequestParam("files") MultipartFile[] files){

        System.out.println("file size: "+files.length);
        if (files.length > 7){
            throw new RuntimeException("too many files");
        }

        List<FileUploadResponse> uploadResponseList = new ArrayList<>();

        // recorre el array de archivos
        Arrays.asList(files)
                .forEach(file -> {
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

                    uploadResponseList.add(response);

                });
        return uploadResponseList;
    }


    // Empaqueta los arcchivos y los envia como un attachment de tipo ZIP
    @GetMapping("zipDownload")
    void zipDowload(@RequestParam("fileName") String[] files, HttpServletResponse response) throws IOException {
        // Directo al browser con este ejemplo: http://localhost:8082/zipDownload?fileName=full%20scan.png%fileName=Ademdum5.pdf

        try(ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())){
            Arrays.asList(files)
                    .stream()
                    .forEach( file -> {
                        Resource resource = fileStorageService
                                .downloadFile(file); // resuelve la ruta del archivo
                        // Tomo el archivo y lo comprime
                        ZipEntry zipEntry = new ZipEntry(resource.getFilename());
                        try {
                            zipEntry.setSize(resource.contentLength());
                            // lo argurega al zip y espera por otro
                            zos.putNextEntry(zipEntry);
                            StreamUtils.copy(resource.getInputStream(), zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            System.out.println("some exception while zipping");
                        }
                    });
            zos.finish();
        }
    }
}
