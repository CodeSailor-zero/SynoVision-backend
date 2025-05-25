package com.sean.synovision.model.vo.tag;

import cn.hutool.json.JSONUtil;
import com.sean.synovision.model.entity.Tag;
import com.sean.synovision.model.vo.user.UserVo;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;

/**
 * @author sean
 * @Date 2025/53/24
 */
@Data
public class TagVo {
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
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private UserVo userVo;

    public static TagVo objToVo(Tag tag){
        if (tag == null){
            return null;
        }
        TagVo tagVo = new TagVo();
        BeanUtils.copyProperties(tag, tagVo);
        String userIds = tag.getUserIds();
        tagVo.setUserIds(JSONUtil.toList(userIds,String.class));
        return tagVo;
    }

    public static Tag voToObj(TagVo tagVo){
        if (tagVo == null){
            return null;
        }
        Tag tag = new Tag();
        BeanUtils.copyProperties(tagVo,tag);
        List<String> userIds = tagVo.getUserIds();
        tag.setUserIds(JSONUtil.toJsonStr(userIds));
        return tag;
    }
}
