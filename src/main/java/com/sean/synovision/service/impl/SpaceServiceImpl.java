package com.sean.synovision.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sean.synovision.exception.BussinessException;
import com.sean.synovision.exception.ErrorCode;
import com.sean.synovision.manager.auth.SpaceUserAuthManager;
import com.sean.synovision.manager.sharding.DynamicShardingManager;
import com.sean.synovision.mapper.SpaceMapper;
import com.sean.synovision.model.dto.space.SpaceAddRequest;
import com.sean.synovision.model.dto.space.SpaceQueryRequest;
import com.sean.synovision.model.entity.Space;
import com.sean.synovision.model.entity.SpaceMember;
import com.sean.synovision.model.entity.User;
import com.sean.synovision.model.enums.SpaceLevelEnum;
import com.sean.synovision.model.enums.SpaceRoleEnum;
import com.sean.synovision.model.enums.SpaceTypeEnum;
import com.sean.synovision.model.vo.space.SpaceVo;
import com.sean.synovision.model.vo.user.UserVo;
import com.sean.synovision.service.SpaceMemberService;
import com.sean.synovision.service.SpaceService;
import com.sean.synovision.service.UserService;
import com.sean.synovision.utill.ThrowUtill;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author 24395
 * @description 针对表【space(空间表)】的数据库操作Service实现
 * @createDate 2025-05-26 19:25:16
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {

    @Resource
    private UserService userService;

    // spring 的事务管理
    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private SpaceMemberService spaceMemberService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

//    @Resource
//    @Lazy
//    private DynamicShardingManager dynamicShardingManager;

    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User user) {
        // 1. 获取参数属性，进行校验
        String spaceName = spaceAddRequest.getSpaceName();
        Integer spaceLevel = spaceAddRequest.getSpaceLevel();
        Integer spaceType = spaceAddRequest.getSpaceType();
        if (spaceLevel == null) {
            spaceLevel = SpaceLevelEnum.COMMON.getValue();
        }
        if (spaceType == null) {
            spaceType = SpaceTypeEnum.PRIVATE.getValue();
        }
        // 1.1 数据填充
        Long userId = user.getId();
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddRequest, space);
        space.setUserId(userId);
        this.fillSpaceBySpaceLevel(space);
        this.vaildSpace(space, true);
        // 2.判断权限
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        boolean isAdmin = userService.isAdmin(user);
        // 用户不是管理员并且这个创建的空间不是普通空间
        if (!isAdmin && !SpaceLevelEnum.COMMON.equals(spaceLevelEnum)) {
            throw new BussinessException(ErrorCode.NO_AUTH_ERROR, "无权限创建其他空间");
        }
        // 3.创建空间，写入数据库
        // 使用 synchronized 锁，并且以用户id为key,intern()方法从数据池获取数据，保证锁的唯一性
        String lock = String.valueOf(userId).intern();
        synchronized (lock) {
            Integer finalSpaceType = spaceType;
            Long spaceId = transactionTemplate.execute(status -> {
                // 1.判断当前空间是否存在
                boolean exists = this.lambdaQuery()
                        .eq(Space::getUserId, userId)
                        .eq(Space::getSpaceType, finalSpaceType)
                        .exists();
                ThrowUtill.throwIf(exists, ErrorCode.NOT_FOUND_ERROR, "一个用户只能创建一个类型的空间");
                // 2.直接写入数据库
                boolean result = this.save(space);
                ThrowUtill.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建空间失败");
                // 3.如果创建的时团队空间，将本人设置为管理员，并且保存到数据库
                if (SpaceTypeEnum.TEAM.getValue() == finalSpaceType) {
                    SpaceMember spaceMember = new SpaceMember();
                    spaceMember.setSpaceId(space.getId());
                    spaceMember.setUserId(userId);
                    spaceMember.setSpaceUserRole(SpaceRoleEnum.ADMIN.getValue());
                    result = spaceMemberService.save(spaceMember);
                    ThrowUtill.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建团队空间失败");
                }
//                //创建分表（仅对旗舰版团队空间生效）
//                dynamicShardingManager.createSpacePictureTable(space);
                return space.getId();
            });
            return Optional.ofNullable(spaceId).orElse(-1L);
        }
    }

    // region 公用方法
    @Override
    public void vaildSpace(Space space, boolean add) {
        ThrowUtill.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        Integer spaceType = space.getSpaceType();
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(spaceType);
        if (add) {
            //创建时校验参数
            ThrowUtill.throwIf(StrUtil.isBlank(spaceName), ErrorCode.PARAMS_ERROR, "请输入你的空间名");
            ThrowUtill.throwIf(spaceLevelEnum == null, ErrorCode.PARAMS_ERROR, "请选择你的空间级别");
            //校验空间类型
            ThrowUtill.throwIf(spaceType == null, ErrorCode.PARAMS_ERROR, "请选择你的空间类型");

        }
        //修改时校验参数
        ThrowUtill.throwIf(StrUtil.isBlank(spaceName) && spaceName.length() < 30, ErrorCode.PARAMS_ERROR, "空间名不合规");
        ThrowUtill.throwIf(spaceLevel != null && spaceLevelEnum == null, ErrorCode.PARAMS_ERROR, "空间级别不合规");
        ThrowUtill.throwIf(spaceType != null && spaceTypeEnum == null, ErrorCode.PARAMS_ERROR, "空间类型不合规");
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        if (spaceQueryRequest == null) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "参数异常");
        }
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        Integer spaceType = spaceQueryRequest.getSpaceType();
        String searchText = spaceQueryRequest.getSearchText();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();

        QueryWrapper<Space> spaceQueryWrapper = new QueryWrapper<>();
        if (StrUtil.isNotBlank(searchText)) {
            spaceQueryWrapper.and(
                    qw -> qw.like("spaceName", searchText)
            );
        }
        spaceQueryWrapper.eq(ObjectUtil.isNotNull(id), "id", id);
        spaceQueryWrapper.eq(ObjectUtil.isNotEmpty(userId), "userId", userId);
        spaceQueryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        spaceQueryWrapper.eq(ObjectUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
        spaceQueryWrapper.eq(ObjectUtil.isNotEmpty(spaceType), "spaceType", spaceType);
        spaceQueryWrapper.orderBy(StrUtil.isNotEmpty(sortOrder), sortOrder.equals("ascend"), sortField);
        return spaceQueryWrapper;
    }

    @Override
    public SpaceVo getSpaceVo(Space space, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        //实体类转Vo类
        SpaceVo spaceVo = SpaceVo.objToVo(space);
        Long userId = spaceVo.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVo userVo = userService.getUserVo(user);
            spaceVo.setUserVo(userVo);
        }
        spaceVo.setParmissionList(spaceUserAuthManager.getPermissionList(space, loginUser));
        return spaceVo;
    }

    @Override
    public Page<SpaceVo> getSpaceVoPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVo> pictureVoPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollectionUtil.isEmpty(spaceList)) {
            return pictureVoPage;
        }
        List<SpaceVo> spaceVoList = spaceList.stream().map(SpaceVo::objToVo).collect(Collectors.toList());
        List<Long> userIds = spaceList.stream().map(Space::getUserId).collect(Collectors.toList());
        Map<Long, List<User>> idUserMap = userService.listByIds(userIds)
                .stream()
                .collect(Collectors.groupingBy(User::getId));
        spaceVoList.stream().forEach(spaceVo -> {
            Long userId = spaceVo.getUserId();
            User user = null;
            if (idUserMap.containsKey(userId)) {
                user = idUserMap.get(userId).get(0);
            }
            spaceVo.setUserVo(userService.getUserVo(user));
        });
        pictureVoPage.setRecords(spaceVoList);
        return pictureVoPage;
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum != null) {
            // 空间的最大容量
            Long maxSize = space.getMaxSize();
            // 如果管理员制定空间大小，那么管理员的算
            if (maxSize == null) {
                space.setMaxSize(spaceLevelEnum.getMaxSize());
            }
            // 空间的最大数量
            Long maxCount = space.getMaxCount();
            if (maxCount == null) {
                space.setMaxCount(spaceLevelEnum.getMaxCount());
            }
        }
    }

    /**
     * 校验空间权限
     *
     * @param loginUser
     * @param space
     */
    @Override
    public void checkSpaceAuth(User loginUser, Space space) {
        // 仅本人或管理员有权限
        if (!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BussinessException(ErrorCode.NO_AUTH_ERROR, "没有权限");
        }
    }
    // endregion
}




