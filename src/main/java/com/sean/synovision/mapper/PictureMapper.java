package com.sean.synovision.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sean.synovision.model.entity.Picture;
import org.apache.ibatis.annotations.MapKey;

import java.util.List;
import java.util.Map;

/**
* @author 24395
* @description 针对表【picture(图片表)】的数据库操作Mapper
* @createDate 2025-05-21 11:20:34
* @Entity com.sean.synovision.model.entity.Picture
*/
public interface PictureMapper extends BaseMapper<Picture> {
    /**
     * 物理删除 isDelete = 1 的图片记录
     */
    void deletePicture();

    /**
     * 查询被逻辑删除的图片 URL 信息
     * @return 包含 url、thumbnailUrl、originalUrl 的 Map 列表
     */
    @MapKey("id")
    List<Map<Long, Object>> selectDeletedPictureUrls();
}




