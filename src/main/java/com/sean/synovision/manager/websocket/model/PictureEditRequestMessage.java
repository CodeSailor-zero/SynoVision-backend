package com.sean.synovision.manager.websocket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author sean
 * @Date 2025/06/03
 * 图片编辑请求消息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PictureEditRequestMessage implements Serializable {
    /**
     * 消息类型，例如："ENTER_EDIT"
     */
    private String type;

    /**
     * 编辑动作
     */
    private String editAction;
}
