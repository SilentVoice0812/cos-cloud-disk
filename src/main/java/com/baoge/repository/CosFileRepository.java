package com.baoge.repository;

import com.baoge.entity.CosFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CosFileRepository extends JpaRepository<CosFile, Long> {
    
    /**
     * 获取目录下的文件和文件夹
     */
    List<CosFile> findByParentIdAndDeletedOrderByFileTypeAscFileNameAsc(Long parentId, Integer deleted);
    
    /**
     * 搜索文件
     */
    List<CosFile> findByFileNameContainingAndDeletedOrderByUpdatedAtDesc(String keyword, Integer deleted);
    
    /**
     * 统计父目录下的文件数量
     */
    long countByParentIdAndDeleted(Long parentId, Integer deleted);
}
