package com.sean.synovision.manager.websocket.model;

import com.sean.synovision.model.vo.user.UserVo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author sean
 * @Date 2025/06/03
 * 图片编辑响应消息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PictureEditResponseMessage implements Serializable {
    /**
     * 消息类型，例如："INFO"
     */
    private String type;

    /**
     * 消息内容
     */
    private String message;

    /**
     * 执行的编辑动作
     */
    private String editAction;

    /**
     * 用户信息
     */
    private UserVo userVo;
}
