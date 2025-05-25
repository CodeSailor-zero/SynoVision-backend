package com.sean.synovision.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @author sean
 * @Date 2025/30/21
 */
@Data
public class PictureUploadRequest implements Serializable {
    /**
     * 图片id(用于修改)
     */
    private Long id;

    /**
     * 文件url
     */
    private String fileUrl;

    /**
     * 图片名前缀
     */
    private String namePrefix;
}
