package com.sean.synovision.model.dto.picture;

import com.sean.synovision.api.ailyunai.model.CreateOutPaintingTaskRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * @author sean
 * @Date 2025/05/29
 */
@Data
public class CreatePictureOutPaintingTaskRequest implements Serializable {
    /**
     * 图片的id
     */
    private Long pictureId;
    /**
     * 扩图参数
     */
    private CreateOutPaintingTaskRequest.Parameters param;
}
