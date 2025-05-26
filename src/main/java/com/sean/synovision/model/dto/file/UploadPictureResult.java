package com.sean.synovision.model.dto.file;

import lombok.Data;

/**
 * @author sean
 * @Date 2025/41/21
 */
@Data
public class UploadPictureResult {
    private String url;
    private String thumbnailUrl;
    private String picName;
    private long picSize;
    private int picWidth;
    private int picHeight;
    //图片的宽高比
    private Double picScale;
    //图片格式
    private String picFormat;
}
