package com.example.uploadanddownload.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    private Path fileStoragePath;
    private String fileStorageLocation;

    // Consturctor
    /*
    * La anotacion @Value, va a obtener en valor que esta almacenado
    * locamente en el proyecto en la varible file.storage.location */
    public FileStorageService (@Value("${file.storage.location:temp}") String fileStorageLocation){
        this.fileStorageLocation=fileStorageLocation;
        // normaliza el path
        fileStoragePath = Paths.get(fileStorageLocation)
                .toAbsolutePath()
                .normalize();

        try {
            // crea el folder
            Files.createDirectories(fileStoragePath);
        } catch (IOException e) {
            throw new RuntimeException("Issue in creating file directory");
        }
    }

    public String storageFile(MultipartFile file) {

        // obtiene el nombe del archivo que es pasado por argumento
        String fileName = StringUtils.cleanPath(file.getOriginalFilename()) ;

        // agrega el nombre del archivo a la ruta de almacenamiento local
        Path filePath = Paths.get(fileStoragePath+"\\"+fileName);

        try {
            // toma el archivo pasado y lo copia a la area local del proyecto
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Issue in storing the file",e);
        }

        return fileName;
    }

    public Resource downloadFile(String fileName) {

        // resuelve la ruta a travez de argumento
        Path path = Paths.get(fileStorageLocation).toAbsolutePath().resolve(fileName);

        Resource resource;
        try {
            // convierte la ruta en un URI
            resource = (Resource) new UrlResource(path.toUri());

        } catch (MalformedURLException e) {
            throw new RuntimeException("Issue in reading the file", e);
        }

        if (resource.exists() && resource.isReadable()){
            return resource;
        } else {
            throw new RuntimeException("the file doesn't exist or not redable");
        }
    }
}
