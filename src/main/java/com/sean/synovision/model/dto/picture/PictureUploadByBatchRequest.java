package com.sean.synovision.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @author sean
 * @Date 2025/05/25
 * 批量上传图片请求体
 */
@Data
public class PictureUploadByBatchRequest implements Serializable {
    /**
     * 搜索词
     */
   private String searchText;
    /**
     * 默认抓取的个数
     */
   private Integer count = 5;

    /**
     * 图片名字前缀
     */
   private String namePrefix;
}
