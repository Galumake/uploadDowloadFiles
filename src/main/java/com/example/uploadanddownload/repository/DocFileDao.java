package com.example.uploadanddownload.repository;

import com.example.uploadanddownload.domain.FileDocument;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface DocFileDao extends CrudRepository<FileDocument, Long> {
    FileDocument finByFileName(String fileName);
}
