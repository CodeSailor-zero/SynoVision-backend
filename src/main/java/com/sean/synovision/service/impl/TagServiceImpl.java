package com.sean.synovision.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sean.synovision.exception.BussinessException;
import com.sean.synovision.exception.ErrorCode;
import com.sean.synovision.model.dto.tag.TagAddRequest;
import com.sean.synovision.model.dto.tag.TagQueryRequest;
import com.sean.synovision.model.dto.tag.TagUpdateRequest;
import com.sean.synovision.model.entity.Tag;
import com.sean.synovision.model.entity.User;
import com.sean.synovision.model.vo.tag.TagVo;
import com.sean.synovision.service.TagService;
import com.sean.synovision.mapper.TagMapper;
import com.sean.synovision.service.UserService;
import com.sean.synovision.utill.ThrowUtill;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author 24395
* @description 针对表【tag(tag表)】的数据库操作Service实现
* @createDate 2025-05-24 00:58:12
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{


    @Resource
    private UserService userService;

   // region 增删查改
    @Override
    public long addTag(TagAddRequest tagAddRequest, User loginUser) {
        ThrowUtill.throwIf(tagAddRequest == null, ErrorCode.PARAMS_ERROR,"tag对象不存在");
        String tagName = tagAddRequest.getTagName();
        ThrowUtill.throwIf(StringUtils.isBlank(tagName) || tagName.length() > 10, ErrorCode.PARAMS_ERROR,"tag名称有问题");
        String tagType = tagAddRequest.getTagType();
        ThrowUtill.throwIf(StringUtils.isBlank(tagType), ErrorCode.PARAMS_ERROR,"tag类型有问题");
        Tag tag = new Tag();
        tag.setTagName(tagName);
        //目前只有管理员可以使用
        tag.setTagType(tagType);
        tag.setCreateId(loginUser.getId());
        int insert = this.baseMapper.insert(tag);
        ThrowUtill.throwIf(insert != 1, ErrorCode.OPERATION_ERROR,"添加失败");
        return tag.getId();
    }

    @Override
    public boolean deleteTag(Long id) {
        ThrowUtill.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR,"tag不存在");
        boolean result = this.removeById(id);
        ThrowUtill.throwIf(!result, ErrorCode.OPERATION_ERROR,"删除失败");
        return true;
    }

    @Override
    public boolean updateTag(TagUpdateRequest tagUpdateRequest) {
        ThrowUtill.throwIf(tagUpdateRequest == null || tagUpdateRequest.getId() < 0, ErrorCode.PARAMS_ERROR,"tag对象不存在");
        Tag tag = new Tag();
        BeanUtils.copyProperties(tagUpdateRequest,tag);
        boolean result = this.updateById(tag);
        ThrowUtill.throwIf(!result, ErrorCode.OPERATION_ERROR,"更新失败");
        return true;
    }

    @Override
    public List<TagVo> fetchTagVoList(TagQueryRequest tagQueryRequest) {
        ThrowUtill.throwIf(tagQueryRequest == null, ErrorCode.PARAMS_ERROR,"参数有误");
        List<Tag> tagList = list(getQueryWrapper(tagQueryRequest));
        return getTagVoList(tagList);
    }

    @Override
    public Page<Tag> fetchTagPage(TagQueryRequest tagQueryRequest, HttpServletRequest request) {
        ThrowUtill.throwIf(tagQueryRequest == null, ErrorCode.PARAMS_ERROR,"参数有误");
        Page<Tag> tags = this.page(new Page<>(tagQueryRequest.getCurrent(), tagQueryRequest.getPageSize())
                , getQueryWrapper(tagQueryRequest));
        return tags;
    }

    // endregion

    @Override
    public List<String> getTagNameList () {
        TagQueryRequest tagQueryRequest = new TagQueryRequest();
        tagQueryRequest.setSortOrder("desc");
        tagQueryRequest.setSortField("tagCount");

        return fetchTagVoList(tagQueryRequest)
                .stream()
                .map(TagVo::getTagName)
                .collect(Collectors.toList());
    }

    @Override
    public List<TagVo> getTagVoList(List<Tag> tags) {
        ThrowUtill.throwIf(CollectionUtil.isEmpty(tags), ErrorCode.PARAMS_ERROR,"参数有误");
        List<TagVo> TagVos = tags.stream().map(TagVo::objToVo).collect(Collectors.toList());
        List<Long> createIds = tags.stream().map(Tag::getCreateId).collect(Collectors.toList());
        Map<Long, List<User>> idUserMap = userService.listByIds(createIds).stream().collect(Collectors.groupingBy(User::getId));
        TagVos.stream().forEach(tagVo -> {
            Long createId = tagVo.getCreateId();
            User user = null;
            if (idUserMap.containsKey(createId)) {
                user = idUserMap.get(createId).get(0);
            }
            tagVo.setUserVo(userService.getUserVo(user));
        });
        return TagVos;
    }

    @Override
    public QueryWrapper<Tag> getQueryWrapper(TagQueryRequest tagQueryRequest) {
        if (tagQueryRequest == null) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR,"参数异常");
        }
        Long id = tagQueryRequest.getId();
        String tagName = tagQueryRequest.getTagName();
        String tagType = tagQueryRequest.getTagType();
        Long createId = tagQueryRequest.getCreateId();
        List<String> userIds = tagQueryRequest.getUserIds();
        Integer tagCount = tagQueryRequest.getTagCount();
        String searchText = tagQueryRequest.getSearchText();
        String sortField = tagQueryRequest.getSortField();
        String sortOrder = tagQueryRequest.getSortOrder();


        QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
        if (StrUtil.isNotBlank(searchText)) {
            tagQueryWrapper.and(
                    qw -> qw.like("tagName" , searchText)
                            .or()
                            .eq("tagType",searchText)
            );
        }


        tagQueryWrapper.eq(ObjectUtil.isNotNull(id), "id", id);
        tagQueryWrapper.eq(StrUtil.isNotBlank(tagName), "tagName", tagName);
        tagQueryWrapper.eq(StrUtil.isNotBlank(tagType), "tagType", tagType);
        tagQueryWrapper.eq(ObjectUtil.isNotEmpty(createId), "createId", createId);
        tagQueryWrapper.eq(ObjectUtil.isNotEmpty(tagCount), "tagCount", tagCount);
        if (CollectionUtil.isNotEmpty(userIds)) {
            for (String userId : userIds) {
                tagQueryWrapper.like("userIds", "\"" + userId + "\"");
            }
        }
        tagQueryWrapper.orderBy(StrUtil.isNotEmpty(sortOrder), sortOrder.equals("ascend"),sortField);
        return tagQueryWrapper;
    }

}




