package com.mihey.ziptos3.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class S3ServiceImpl {

    @Value("${destinationDirectory}")
    private String destinationDirectory;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private AmazonS3 s3client;

    @Autowired
    public S3ServiceImpl(AmazonS3 s3client) {
        this.s3client = s3client;
    }

    public String upload(MultipartFile zipFile) throws IOException {

        String zipFileName = unZip(zipFile);
        File uploadFile = new File(destinationDirectory + "/" + zipFileName);
        s3client.putObject(bucket, zipFileName, uploadFile);
        return "File uploaded";

    }

    private String unZip(MultipartFile file) throws IOException {
        File destDir = new File(destinationDirectory);
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(convertMultiPartToFile(file)));
        ZipEntry zipEntry = zis.getNextEntry();
        String name = zipEntry.getName();
        while (zipEntry != null) {
            File newFile = newFile(destDir, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory" + newFile);
                }
            } else {
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory" + parent);
                }
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }
        return name;
    }

    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        return destFile;
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }
}

