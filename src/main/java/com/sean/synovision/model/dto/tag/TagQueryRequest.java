package com.sean.synovision.model.dto.tag;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.sean.synovision.common.PageResult;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author sean
 * @Date 2025/57/24
 */
@Data
public class TagQueryRequest extends PageResult implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * system[admin] / user 设置
     */
    private String tagType;

    /**
     * 创建人Id
     */
    private Long createId;

    /**
     * 使用人ids【使用json】
     */
    private List<String> userIds;

    /**
     * 标签出现次数
     */
    private Integer tagCount;
    private String searchText;
}
