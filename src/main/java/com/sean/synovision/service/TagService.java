package com.sean.synovision.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sean.synovision.model.dto.tag.TagAddRequest;
import com.sean.synovision.model.dto.tag.TagQueryRequest;
import com.sean.synovision.model.dto.tag.TagUpdateRequest;
import com.sean.synovision.model.entity.Tag;
import com.sean.synovision.model.entity.User;
import com.sean.synovision.model.vo.tag.TagVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 24395
* @description 针对表【tag(tag表)】的数据库操作Service
* @createDate 2025-05-24 00:58:12
*/
public interface TagService extends IService<Tag> {

    /**
     * 添加tag
     * @param tagAddRequest
     * @param user
     * @return
     */
   long addTag(TagAddRequest tagAddRequest, User user);

    /**
     * 删除tag
     * @param id
     * @return
     */
   boolean deleteTag(Long id);

    /**
     * 更新图表
     * @param tagUpdateRequest
     * @return
     */
   boolean updateTag(TagUpdateRequest tagUpdateRequest);

    List<TagVo> fetchTagVoList(TagQueryRequest tagQueryRequest);


    QueryWrapper<Tag> getQueryWrapper(TagQueryRequest tagQueryRequest);

    Page<Tag> fetchTagPage(TagQueryRequest tagQueryRequest, HttpServletRequest request);

    List<String> getTagNameList();

    List<TagVo> getTagVoList(List<Tag> tags);

}
