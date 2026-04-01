package com.baoge.service;

import com.baoge.entity.CosFile;
import com.baoge.repository.CosFileRepository;
import com.baoge.utils.CosUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CosFileService {

    @Autowired
    private CosFileRepository cosFileRepository;

    @Autowired
    private CosUtils cosUtils;

    /**
     * 获取目录下的文件和文件夹
     */
    public List<CosFile> getFilesByParentId(Long parentId) {
        return cosFileRepository.findByParentIdAndDeletedOrderByFileTypeAscFileNameAsc(parentId, 0);
    }

    /**
     * 创建文件夹
     */
    public CosFile createFolder(String folderName, Long parentId) {
        CosFile folder = new CosFile();
        folder.setFileName(folderName);
        folder.setFileType("folder");
        folder.setParentId(parentId);
        return cosFileRepository.save(folder);
    }

    /**
     * 上传文件
     */
    public CosFile uploadFile(MultipartFile file, Long parentId) throws Exception {
        String filePath = cosUtils.uploadFile(file, parentId);
        
        CosFile cosFile = new CosFile();
        cosFile.setFileName(file.getOriginalFilename());
        cosFile.setFilePath(filePath);
        cosFile.setFileSize(file.getSize());
        cosFile.setMimeType(file.getContentType());
        cosFile.setFileType("file");
        cosFile.setParentId(parentId);
        cosFile.setBucketName(cosUtils.getBucketName());
        cosFile.setCosUrl(cosUtils.getBucketDomain() + "/" + filePath);
        
        // 计算MD5
        String md5 = calculateMD5(file.getInputStream());
        cosFile.setMd5(md5);
        
        return cosFileRepository.save(cosFile);
    }

    /**
     * 删除文件或文件夹
     */
    @Transactional
    public void deleteFile(Long id) {
        Optional<CosFile> fileOpt = cosFileRepository.findById(id);
        if (!fileOpt.isPresent()) {
            throw new RuntimeException("文件不存在");
        }
        
        CosFile file = fileOpt.get();
        
        if ("file".equals(file.getFileType())) {
            cosUtils.deleteObject(file.getFilePath());
        }
        
        if ("folder".equals(file.getFileType())) {
            long count = cosFileRepository.countByParentIdAndDeleted(id, 0);
            if (count > 0) {
                throw new RuntimeException("文件夹不为空，请先删除其中的文件");
            }
        }
        
        file.setDeleted(1);
        cosFileRepository.save(file);
    }

    /**
     * 重命名
     */
    public CosFile rename(Long id, String newName) {
        Optional<CosFile> fileOpt = cosFileRepository.findById(id);
        if (!fileOpt.isPresent()) {
            throw new RuntimeException("文件不存在");
        }
        
        CosFile file = fileOpt.get();
        file.setFileName(newName);
        return cosFileRepository.save(file);
    }

    /**
     * 移动文件到目标目录
     */
    public CosFile moveTo(Long id, Long targetParentId) {
        Optional<CosFile> fileOpt = cosFileRepository.findById(id);
        if (!fileOpt.isPresent()) {
            throw new RuntimeException("文件不存在");
        }
        
        CosFile file = fileOpt.get();
        file.setParentId(targetParentId);
        return cosFileRepository.save(file);
    }

    /**
     * 复制文件到目标目录
     */
    public CosFile copyFile(Long id, Long targetParentId) {
        Optional<CosFile> fileOpt = cosFileRepository.findById(id);
        if (!fileOpt.isPresent()) {
            throw new RuntimeException("文件不存在");
        }
        
        CosFile sourceFile = fileOpt.get();
        if (!"file".equals(sourceFile.getFileType())) {
            throw new RuntimeException("只支持复制文件");
        }
        
        // 复制COS对象
        String newFilePath = cosUtils.downloadAndUpload(sourceFile.getFilePath(), targetParentId, sourceFile.getFileName());
        
        // 创建新的数据库记录
        CosFile newFile = new CosFile();
        newFile.setFileName(sourceFile.getFileName());
        newFile.setFilePath(newFilePath);
        newFile.setFileSize(sourceFile.getFileSize());
        newFile.setMimeType(sourceFile.getMimeType());
        newFile.setFileType("file");
        newFile.setParentId(targetParentId);
        newFile.setBucketName(cosUtils.getBucketName());
        newFile.setCosUrl(cosUtils.getBucketDomain() + "/" + newFilePath);
        newFile.setMd5(sourceFile.getMd5());
        
        return cosFileRepository.save(newFile);
    }

    /**
     * 搜索文件
     */
    public List<CosFile> searchFiles(String keyword) {
        return cosFileRepository.findByFileNameContainingAndDeletedOrderByUpdatedAtDesc(keyword, 0);
    }

    /**
     * 获取文件下载URL
     */
    public String getDownloadUrl(Long id) {
        Optional<CosFile> fileOpt = cosFileRepository.findById(id);
        if (!fileOpt.isPresent() || !"file".equals(fileOpt.get().getFileType())) {
            throw new RuntimeException("文件不存在");
        }
        
        CosFile file = fileOpt.get();
        return cosUtils.getPresignedDownloadUrl(file.getFilePath(), 3600);
    }

    /**
     * 获取文件MD5
     */
    public String getFileMd5(Long id) {
        Optional<CosFile> fileOpt = cosFileRepository.findById(id);
        if (!fileOpt.isPresent() || !"file".equals(fileOpt.get().getFileType())) {
            throw new RuntimeException("文件不存在");
        }
        
        CosFile file = fileOpt.get();
        
        // 如果数据库已有MD5直接返回
        if (file.getMd5() != null && !file.getMd5().isEmpty()) {
            return file.getMd5();
        }
        
        // 否则从COS获取
        return cosUtils.getFileMd5(file.getFilePath());
    }

    /**
     * 获取文件预览信息（缩略图URL等）
     */
    public Map<String, Object> getFilePreview(Long id) {
        Optional<CosFile> fileOpt = cosFileRepository.findById(id);
        if (!fileOpt.isPresent()) {
            throw new RuntimeException("文件不存在");
        }
        
        CosFile file = fileOpt.get();
        Map<String, Object> result = new HashMap<>();
        result.put("fileName", file.getFileName());
        result.put("fileSize", file.getFileSize());
        result.put("mimeType", file.getMimeType());
        result.put("md5", file.getMd5());
        result.put("cosUrl", file.getCosUrl());
        result.put("fileType", file.getFileType());
        
        // 根据文件类型生成预览信息
        String mimeType = file.getMimeType();
        String previewUrl = null;
        String thumbnailUrl = null;
        
        if (mimeType != null) {
            if (mimeType.startsWith("image/")) {
                // 图片：生成缩略图和预览URL
                thumbnailUrl = cosUtils.getImageThumbnailUrl(file.getFilePath(), 200, 200);
                previewUrl = cosUtils.getPresignedDownloadUrl(file.getFilePath(), 3600);
            } else if (mimeType.startsWith("video/")) {
                // 视频：生成视频缩略图
                thumbnailUrl = cosUtils.getVideoThumbnailUrl(file.getFilePath());
                previewUrl = cosUtils.getPresignedDownloadUrl(file.getFilePath(), 3600);
            } else if (mimeType.equals("application/pdf")) {
                // PDF
                thumbnailUrl = "/assets/file-icons/pdf.png";
                previewUrl = cosUtils.getPresignedDownloadUrl(file.getFilePath(), 3600);
            } else if (mimeType.contains("word") || mimeType.contains("document")) {
                thumbnailUrl = "/assets/file-icons/doc.png";
                previewUrl = cosUtils.getPresignedDownloadUrl(file.getFilePath(), 3600);
            } else if (mimeType.contains("excel") || mimeType.contains("spreadsheet")) {
                thumbnailUrl = "/assets/file-icons/xls.png";
                previewUrl = cosUtils.getPresignedDownloadUrl(file.getFilePath(), 3600);
            } else if (mimeType.contains("powerpoint") || mimeType.contains("presentation")) {
                thumbnailUrl = "/assets/file-icons/ppt.png";
                previewUrl = cosUtils.getPresignedDownloadUrl(file.getFilePath(), 3600);
            } else if (mimeType.contains("zip") || mimeType.contains("rar") || mimeType.contains("tar") || mimeType.contains("gz")) {
                thumbnailUrl = "/assets/file-icons/zip.png";
                previewUrl = cosUtils.getPresignedDownloadUrl(file.getFilePath(), 3600);
            } else if (mimeType.contains("audio/")) {
                thumbnailUrl = "/assets/file-icons/audio.png";
                previewUrl = cosUtils.getPresignedDownloadUrl(file.getFilePath(), 3600);
            } else if (mimeType.contains("text/")) {
                thumbnailUrl = "/assets/file-icons/txt.png";
                previewUrl = cosUtils.getPresignedDownloadUrl(file.getFilePath(), 3600);
            } else {
                thumbnailUrl = "/assets/file-icons/file.png";
                previewUrl = cosUtils.getPresignedDownloadUrl(file.getFilePath(), 3600);
            }
        } else {
            thumbnailUrl = "/assets/file-icons/file.png";
        }
        
        result.put("previewUrl", previewUrl);
        result.put("thumbnailUrl", thumbnailUrl);
        
        return result;
    }

    /**
     * 计算MD5
     */
    private String calculateMD5(InputStream inputStream) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
