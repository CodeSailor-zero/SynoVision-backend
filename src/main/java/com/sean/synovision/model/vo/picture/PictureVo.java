package com.sean.synovision.model.vo.picture;

import cn.hutool.json.JSONUtil;
import com.sean.synovision.model.entity.Picture;
import com.sean.synovision.model.vo.user.UserVo;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author sean
 * @Date 2025/32/21
 */
@Data
public class PictureVo implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 图片地址
     */
    private String url;

    /**
     * 缩略图地址
     */
    private String thumbnailUrl;

    /**
     * 原始图片地址
     */
    private String originalUrl;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 图片简介
     */
    private String introduction;

    /**
     * 图片分类
     */
    private String category;

    /**
     * 图片标签(JSON数组)
     */
    private List<String> tagList;

    /**
     * 图片体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 空间id
     */
    private Long spaceId;

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

    /**
     * 上传用户信息
     */
    private UserVo userVo;

    /**
     *  权限列表
     */
    private List<String> parmissionList = new ArrayList<>();

    public static PictureVo objToVo(Picture picture){
        if (picture == null){
            return null;
        }
        PictureVo pictureVo = new PictureVo();
        BeanUtils.copyProperties(picture, pictureVo);
        String tags = picture.getTags();
        pictureVo.setTagList(JSONUtil.toList(tags,String.class));
        return pictureVo;
    }

    public static Picture voToObj(PictureVo pictureVo){
        if (pictureVo == null){
            return null;
        }
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureVo,picture);
        List<String> tagList = pictureVo.getTagList();
        picture.setTags(JSONUtil.toJsonStr(tagList));
        return picture;
    }
}
