package com.baoge.controller;

import com.baoge.entity.CosFile;
import com.baoge.service.CosFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private CosFileService cosFileService;

    /**
     * 获取目录列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list(@RequestParam(defaultValue = "0") Long parentId) {
        List<CosFile> files = cosFileService.getFilesByParentId(parentId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", files);
        result.put("message", "success");
        return ResponseEntity.ok(result);
    }

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "0") Long parentId) {
        try {
            CosFile cosFile = cosFileService.uploadFile(file, parentId);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("data", cosFile);
            result.put("message", "上传成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "上传失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 创建文件夹
     */
    @PostMapping("/createFolder")
    public ResponseEntity<Map<String, Object>> createFolder(
            @RequestParam String folderName,
            @RequestParam(defaultValue = "0") Long parentId) {
        CosFile folder = cosFileService.createFolder(folderName, parentId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", folder);
        result.put("message", "创建成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 删除文件或文件夹
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        try {
            cosFileService.deleteFile(id);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "删除成功");
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 400);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 重命名
     */
    @PutMapping("/{id}/rename")
    public ResponseEntity<Map<String, Object>> rename(
            @PathVariable Long id,
            @RequestParam String newName) {
        try {
            CosFile file = cosFileService.rename(id, newName);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("data", file);
            result.put("message", "重命名成功");
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 400);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 移动文件
     */
    @PutMapping("/{id}/move")
    public ResponseEntity<Map<String, Object>> move(
            @PathVariable Long id,
            @RequestParam Long targetParentId) {
        try {
            CosFile file = cosFileService.moveTo(id, targetParentId);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("data", file);
            result.put("message", "移动成功");
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 400);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 复制文件
     */
    @PostMapping("/{id}/copy")
    public ResponseEntity<Map<String, Object>> copy(
            @PathVariable Long id,
            @RequestParam Long targetParentId) {
        try {
            CosFile file = cosFileService.copyFile(id, targetParentId);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("data", file);
            result.put("message", "复制成功");
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 400);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 搜索文件
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestParam String keyword) {
        List<CosFile> files = cosFileService.searchFiles(keyword);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", files);
        result.put("message", "success");
        return ResponseEntity.ok(result);
    }

    /**
     * 获取下载链接
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Map<String, Object>> getDownloadUrl(@PathVariable Long id) {
        try {
            String url = cosFileService.getDownloadUrl(id);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("data", url);
            result.put("message", "success");
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 400);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取文件MD5
     */
    @GetMapping("/{id}/md5")
    public ResponseEntity<Map<String, Object>> getFileMd5(@PathVariable Long id) {
        try {
            String md5 = cosFileService.getFileMd5(id);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("data", md5);
            result.put("message", "success");
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 400);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取文件预览信息
     */
    @GetMapping("/{id}/preview")
    public ResponseEntity<Map<String, Object>> getFilePreview(@PathVariable Long id) {
        try {
            Map<String, Object> preview = cosFileService.getFilePreview(id);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("data", preview);
            result.put("message", "success");
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 400);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
