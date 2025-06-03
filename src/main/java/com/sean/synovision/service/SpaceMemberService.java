package com.sean.synovision.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sean.synovision.model.dto.spaceMember.SpaceMemberAddRequest;
import com.sean.synovision.model.dto.spaceMember.SpaceMemberQueryRequest;
import com.sean.synovision.model.entity.SpaceMember;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sean.synovision.model.entity.User;
import com.sean.synovision.model.vo.spaceMember.SpaceMemberVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 24395
 * @description 针对表【spacemember(空间成员表)】的数据库操作Service
 * @createDate 2025-06-02 15:30:36
 */
public interface SpaceMemberService extends IService<SpaceMember> {
    /**
     * 添加空间成员
     * @param spaceMemberAddRequest
     * @return
     */
    long addSpaceMember(SpaceMemberAddRequest spaceMemberAddRequest);

    /**
     *  校验参数
     * @param spaceMember
     * @param add
     */
    void vaildSpaceMember(SpaceMember spaceMember, boolean add);

    /**
     * 获取单个空间成员
     * @param spaceMember
     * @param request
     * @return
     */
    SpaceMemberVo getSpaceMemberVo(SpaceMember spaceMember, HttpServletRequest request);

    /**
     *  获取空间成员列表
     * @param spaceMemberList
     * @return
     */
    List<SpaceMemberVo> getSpaceMemberVoList(List<SpaceMember> spaceMemberList);

    /**
     *  获取查询条件
     * @param spaceMemberQueryRequest
     * @return
     */
    QueryWrapper<SpaceMember> getQueryWrapper(SpaceMemberQueryRequest spaceMemberQueryRequest);

}
