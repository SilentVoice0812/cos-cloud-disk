package com.baoge.utils;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.UUID;

@Data
@Component
public class CosUtils {

    @Value("${cos.secret-id}")
    private String secretId;

    @Value("${cos.secret-key}")
    private String secretKey;

    @Value("${cos.region}")
    private String region;

    @Value("${cos.bucket-name}")
    private String bucketName;

    @Value("${cos.bucket-domain}")
    private String bucketDomain;

    private static final String TEMP_DIR = "/root/cos-tmp";

    public COSClient getCOSClient() {
        COSCredentials credentials = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        clientConfig.setHttpProtocol(HttpProtocol.https);
        return new COSClient(credentials, clientConfig);
    }

    public String uploadFile(MultipartFile file, Long parentId) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String key = "files/" + parentId + "/" + UUID.randomUUID().toString().replace("-", "") + extension;
        
        File tmpDir = new File(TEMP_DIR);
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }
        
        File tempFile = File.createTempFile("cos-upload", extension, tmpDir);
        
        // 直接用流复制，不用transferTo
        try (InputStream is = file.getInputStream();
             OutputStream os = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        }
        
        COSClient cosClient = getCOSClient();
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, tempFile);
            cosClient.putObject(putObjectRequest);
        } finally {
            cosClient.shutdown();
            tempFile.delete();
        }
        
        return key;
    }

    public String downloadAndUpload(String sourceKey, Long targetParentId, String originalFileName) {
        COSClient cosClient = getCOSClient();
        File tempFile = null;
        try {
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String newKey = "files/" + targetParentId + "/" + UUID.randomUUID().toString().replace("-", "") + extension;
            
            File tmpDir = new File(TEMP_DIR);
            if (!tmpDir.exists()) {
                tmpDir.mkdirs();
            }
            tempFile = File.createTempFile("cos-copy", extension, tmpDir);
            GetObjectRequest getRequest = new GetObjectRequest(bucketName, sourceKey);
            cosClient.getObject(getRequest, tempFile);
            
            PutObjectRequest putRequest = new PutObjectRequest(bucketName, newKey, tempFile);
            cosClient.putObject(putRequest);
            
            return newKey;
        } catch (Exception e) {
            throw new RuntimeException("复制文件失败: " + e.getMessage());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
            cosClient.shutdown();
        }
    }

    public void deleteObject(String key) {
        COSClient cosClient = getCOSClient();
        try {
            cosClient.deleteObject(bucketName, key);
        } finally {
            cosClient.shutdown();
        }
    }

    public String getPresignedDownloadUrl(String key, int expireSeconds) {
        COSClient cosClient = getCOSClient();
        try {
            java.util.Date expirationDate = new java.util.Date(System.currentTimeMillis() + expireSeconds * 1000L);
            return cosClient.generatePresignedUrl(bucketName, key, expirationDate).toString();
        } finally {
            cosClient.shutdown();
        }
    }

    public String getImageThumbnailUrl(String key, int width, int height) {
        String thumbnailParam = "?imageMogr2/thumbnail/!" + width + "x" + height + "r/gravity/center/format/png/q/80";
        return bucketDomain + "/" + key + thumbnailParam;
    }

    public String getVideoThumbnailUrl(String key) {
        String videoThumbParam = "?vframe/jpg/offset/1/w/200/h/200";
        return bucketDomain + "/" + key + videoThumbParam;
    }

    public String getFileMd5(String key) {
        COSClient cosClient = getCOSClient();
        try {
            ObjectMetadata metadata = cosClient.getObjectMetadata(bucketName, key);
            String etag = metadata.getETag();
            if (etag != null) {
                return etag.replace("\"", "").toLowerCase();
            }
            return null;
        } catch (Exception e) {
            return null;
        } finally {
            cosClient.shutdown();
        }
    }

    public ObjectMetadata getObjectMetadata(String key) {
        COSClient cosClient = getCOSClient();
        try {
            return cosClient.getObjectMetadata(bucketName, key);
        } finally {
            cosClient.shutdown();
        }
    }
}
