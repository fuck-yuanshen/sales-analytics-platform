package com.example.salesanalysis.mapper;

import com.example.salesanalysis.domain.BackupRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BackupRecordMapper {

    @Insert("""
        INSERT INTO backup_records(file_name, file_path, backup_at, comment)
        VALUES(#{fileName}, #{filePath}, NOW(), #{comment})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BackupRecord backupRecord);

    @Select("SELECT * FROM backup_records ORDER BY id DESC")
    List<BackupRecord> findAll();
}
