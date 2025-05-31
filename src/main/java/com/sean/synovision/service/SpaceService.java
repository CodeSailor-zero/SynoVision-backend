package com.sean.synovision.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sean.synovision.model.dto.picture.PictureQueryRequest;
import com.sean.synovision.model.dto.space.SpaceAddRequest;
import com.sean.synovision.model.dto.space.SpaceQueryRequest;
import com.sean.synovision.model.entity.Picture;
import com.sean.synovision.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sean.synovision.model.entity.User;
import com.sean.synovision.model.vo.picture.PictureVo;
import com.sean.synovision.model.vo.space.SpaceVo;

import javax.servlet.http.HttpServletRequest;

/**
* @author 24395
* @description 针对表【space(空间表)】的数据库操作Service
* @createDate 2025-05-26 19:25:16
*/
public interface SpaceService extends IService<Space> {

    // region 业务方法

    /**
     * 创建空间
     * @param spaceAddRequest 创建空间请求对象
     * @param user 当前用户
     * @return 创建的空间id
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User user);
    // endregion

    // region 公用方法
    void vaildSpace(Space space,boolean add);
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    SpaceVo getSpaceVo(Space space, HttpServletRequest request);

    Page<SpaceVo> getSpaceVoPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 通过空间级别 来赋值其他属性
     * @param space 空间对象
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 检查空间权限
     * @param loginUser
     * @param space
     */
    void checkSpaceAuth(User loginUser, Space space);
    // endregion
}
