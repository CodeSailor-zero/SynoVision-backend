package com.sean.synovision.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author sean
 * @Date 2025/30/21
 */
@Data
public class PictureEditRequest implements Serializable {
    /**
     * 图片id
     */
    private Long id;
    private String name;
    private String introduction;
    // 分类
    private String category;
    private List<String> tags;
}
