package com.example.uploadanddownload.web;

import com.example.uploadanddownload.repository.DocFileDao;
import com.example.uploadanddownload.domain.FileDocument;
import com.example.uploadanddownload.service.dto.FileUploadResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
public class UploadDownloadWhitDataBaseController {

    private DocFileDao docFileDao;

    public UploadDownloadWhitDataBaseController(DocFileDao docFileDao) {
        this.docFileDao = docFileDao;
    }


    // Almacena un archivo en la basde de datos y crea un url para luego descargarlo
    @PostMapping("single/uploadDb")
    FileUploadResponse singleFileUpload(@RequestParam("file") MultipartFile file) throws IOException{

        String name = StringUtils.cleanPath(file.getOriginalFilename());

        FileDocument fileDocument = new FileDocument();
        fileDocument.setFileName(name);
        fileDocument.setDocFile(file.getBytes());

        //aqui guarda el archivo en la base de datos
        docFileDao.save(fileDocument);

        // http://localhost:8082/[downloadFromDB/abc.jpg] <-----
        // esta accion lo que hace es crear un URL para lugo descargar el archivo
        // almacenado en la base de datos
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFromDB/")
                .path(name)
                .toUriString();

        String contentType = file.getContentType();

        // Esta clase un un DTO hecho en casa
        FileUploadResponse response = new FileUploadResponse(name, contentType, url);

        return response;
    }

    // obtenemos el archivo almacenado localmente.
    @GetMapping("/downloadFromDB/{fileName}")
    ResponseEntity<byte[]> downLoadSingleFile(@PathVariable String fileName, HttpServletRequest request){

        FileDocument doc = docFileDao.finByFileName(fileName);

        // Se define el tipo de archivo
        // dinamicamente establese el tipo de MIME
        String mimeType = request.getServletContext()
                .getMimeType(doc.getFileName());

        // devuelve el URI como un attachment
        /* Hay dos tipos de CONTENT DISPOSITION
         * INLINE: el resultado lo renderiza en el browser
         * ATTACHMENT: el resultado lo descarga del browser*/
        return  ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders
                                .CONTENT_DISPOSITION,
                        "attachment;fileName="+doc.getFileName())
                //"inline;fileName="+resource.getFilename())
                .body(doc.getDocFile());
    }

}
