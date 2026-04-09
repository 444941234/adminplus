package com.adminplus.converter.file;

import com.adminplus.pojo.dto.response.FileResponse;
import com.adminplus.pojo.entity.FileEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * FileEntity → FileResponse 转换器
 *
 * @author AdminPlus
 */
@Component
public class FileConverter implements Converter<FileEntity, FileResponse> {

    @Override
    public FileResponse convert(FileEntity source) {
        return new FileResponse(
                source.getId(),
                source.getOriginalName(),
                source.getFileName(),
                source.getFileExt(),
                source.getFileSize(),
                source.getContentType(),
                source.getFileUrl(),
                source.getStorageType(),
                source.getDirectory(),
                source.getStatus(),
                source.getCreateUser(),
                source.getUpdateUser(),
                source.getCreateTime(),
                source.getUpdateTime()
        );
    }
}