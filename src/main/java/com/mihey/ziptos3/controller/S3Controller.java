package com.mihey.ziptos3.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.mihey.ziptos3.service.impl.S3ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/upload")
public class S3Controller {

    @Autowired
    private S3ServiceImpl service;

    @PostMapping
    public String upload(@RequestParam("file") MultipartFile file) throws IOException {
        if(file.isEmpty()) {
            return "File is empty";
        }
        service.upload((File) file);
        return "file upload";
    }

}
