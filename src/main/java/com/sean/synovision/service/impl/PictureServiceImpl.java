package com.sean.synovision.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sean.synovision.common.BaseResponse;
import com.sean.synovision.costant.UserConstant;
import com.sean.synovision.exception.BussinessException;
import com.sean.synovision.exception.ErrorCode;
import com.sean.synovision.exception.ResultUtils;
import com.sean.synovision.manager.FileManager;
import com.sean.synovision.model.dto.file.UploadPictureResult;
import com.sean.synovision.model.dto.picture.PictureQueryRequest;
import com.sean.synovision.model.dto.picture.PictureReviewRequest;
import com.sean.synovision.model.dto.picture.PictureUploadRequest;
import com.sean.synovision.model.entity.Picture;
import com.sean.synovision.model.entity.User;
import com.sean.synovision.model.enums.PictureReviewEnum;
import com.sean.synovision.model.vo.picture.PictureVo;
import com.sean.synovision.model.vo.user.UserVo;
import com.sean.synovision.service.PictureService;
import com.sean.synovision.mapper.PictureMapper;
import com.sean.synovision.service.UserService;
import com.sean.synovision.utill.ThrowUtill;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* @author 24395
* @description 针对表【picture(图片表)】的数据库操作Service实现
* @createDate 2025-05-21 11:20:34
*/
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

    @Resource
    private FileManager fileManager;

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public void vaildPicture(Picture picture) {
        ThrowUtill.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        Long id = picture.getId();
        String name = picture.getName();;
        String introduction = picture.getIntroduction();
        ThrowUtill.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        ThrowUtill.throwIf(StrUtil.isBlank(name), ErrorCode.PARAMS_ERROR);
        ThrowUtill.throwIf(StrUtil.isBlank(introduction) || introduction.length() > 800, ErrorCode.PARAMS_ERROR);

    }


    @Override
    public PictureVo uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User user) {
        ThrowUtill.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);
        //判断是否为新增
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }
        if (pictureId != null) {
            Picture oldPicture = this.baseMapper.selectById(pictureId);
            ThrowUtill.throwIf(oldPicture == null, ErrorCode.PICTURE_NOT_EXIST);
            //权限校验，用户不可以改其他人的图片，管理员随便
            if (!user.getId().equals(oldPicture.getUserId()) && !UserConstant.ADMIN_ROLE.equals(user.getUserRole())) {
                throw new BussinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
        //上传图片
        final String FILE_PREFIX = String.format("/public/%s",user.getId());
        UploadPictureResult uploadPictureResult = fileManager.uploadFile(multipartFile, FILE_PREFIX);
        // 保存图片信息到数据库
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(user.getId());
        //填充 picture 更新参数
        if (pictureId != null) {
            picture.setId(pictureId);
            picture.setEditTime(DateUtil.date());
        }
        //补充图片审核校验参数
        this.fullPictureReviewPramas(picture,user);
        boolean update = this.saveOrUpdate(picture);
        ThrowUtill.throwIf(!update, ErrorCode.OPERATION_ERROR);
        return PictureVo.objToVo(picture);
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        //1. 参数校验
        ThrowUtill.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewEnum pictureReviewEnum = PictureReviewEnum.getEnumByValue(reviewStatus);
        String reviewMessage = pictureReviewRequest.getReviewMessage();
        // 审核状态不可能为待审核
        if (id == null || PictureReviewEnum.REVIEWING.equals(pictureReviewEnum)) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR,"参数异常");
        }
        //2. 业务校验
        Picture oldPicture = this.getById(id);
        ThrowUtill.throwIf(oldPicture == null, ErrorCode.PICTURE_NOT_EXIST);
        //2.1 判断是否 是重复审核
        Integer oldReviewStatus = oldPicture.getReviewStatus();
        if (oldReviewStatus == null || oldReviewStatus.equals(reviewStatus)) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR,"参数异常");
        }
        //3.数据库操作
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureReviewRequest, picture);
        picture.setReviewId(loginUser.getId());
        picture.setReviewTime(DateUtil.date());
        boolean result = this.updateById(picture);
        ThrowUtill.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }



    // region 增删查改
    @Override
    public Page<Picture> listPicturePage(PictureQueryRequest pictureQueryRequest) {
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        return page(new Page<>(current, pageSize),
                this.getQueryWrapper(pictureQueryRequest));
    }

    @Override
    public Page<PictureVo> listPictureVoPage(PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        // 1. redisson 分布式锁
        // 2. redisson 限流器【令牌】
        // 3. 缓存 √

        // current  size            start   end
        //   1       10          =>  0       9
        //   2       10          =>  10      19
        //   3       10          =>  20      29
        //   4       10          =>  30      39
        //   c       s           => (c-1)*s   cs-1
        //先查询redis缓存，如果存在则直接返回
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        long start = (long) (current - 1) * pageSize;
        long end = (long) current * pageSize - 1;
        ListOperations listOperations = redisTemplate.opsForList();
        List pageList = listOperations.range("key", start, end);
        if (CollectionUtil.isNotEmpty(pageList)) {
            Page<PictureVo> pictureVoPage = new Page<>(current,pageSize);
            pictureVoPage.setRecords((List<PictureVo>) pageList.get(0));
            return pictureVoPage;
        }
        //只允许查询已经通过的照片
        pictureQueryRequest.setReviewStatus(PictureReviewEnum.PASS.getValue());
        Page<Picture> picturePage = listPicturePage(pictureQueryRequest);
        Page<PictureVo> pictureVoPage = this.getPictureVoPage(picturePage,request);
        String Key = "PictureVo:" + current + ":" + loginUser.getId();
        // 为了防止前端频繁进行查询，同时秒数小是为了防止数据不一致问题
        listOperations.rightPush(Key,pictureVoPage);
        redisTemplate.expire(Key, 30, TimeUnit.SECONDS);
        return pictureVoPage;
    }

    // endregion

    @Override
    public void fullPictureReviewPramas(Picture picture, User loginUser) {
        // 1. 管理员 ---- 自动通过
        if (UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            picture.setReviewStatus(PictureReviewEnum.PASS.getValue());
            picture.setReviewId(loginUser.getId());
            picture.setReviewMessage("管理员自动过审");
            picture.setReviewTime(DateUtil.date());
        }
        //2. 用户
        if (UserConstant.DEFALUT_ROLE.equals(loginUser.getUserRole())) {
            picture.setReviewStatus(PictureReviewEnum.REVIEWING.getValue());
        }
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        if (pictureQueryRequest == null) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR,"参数异常");
        }
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        Long userId = pictureQueryRequest.getUserId();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewId = pictureQueryRequest.getReviewId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        String searchText = pictureQueryRequest.getSearchText();

        QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
        if (StrUtil.isNotBlank(searchText)) {
            pictureQueryWrapper.and(
                    qw -> qw.like("name" , searchText)
                            .or()
                            .like("introduction",searchText)
            );
        }


        pictureQueryWrapper.eq(ObjectUtil.isNotNull(id), "id", id);
        pictureQueryWrapper.eq(ObjectUtil.isNotEmpty(userId), "userId", userId);
        pictureQueryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        pictureQueryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        pictureQueryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        pictureQueryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        pictureQueryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        pictureQueryWrapper.eq(ObjectUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        pictureQueryWrapper.eq(ObjectUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        pictureQueryWrapper.eq(ObjectUtil.isNotEmpty(picSize), "picSize", picSize);
        pictureQueryWrapper.eq(ObjectUtil.isNotEmpty(picScale), "picScale", picScale);
        pictureQueryWrapper.eq(ObjectUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        pictureQueryWrapper.eq(ObjectUtil.isNotEmpty(reviewId), "reviewId", reviewId);
        if (CollectionUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                pictureQueryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        pictureQueryWrapper.orderBy(StrUtil.isNotEmpty(sortOrder), sortOrder.equals("ascend"),sortField);
        return pictureQueryWrapper;
    }

    @Override
    public PictureVo getPictureVo(Picture picture, HttpServletRequest request) {
        PictureVo pictureVo = PictureVo.objToVo(picture);
        Long userId = pictureVo.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVo userVo = userService.getUserVo(user);
            pictureVo.setUserVo(userVo);
        }
        return pictureVo;
    }

    @Override
    public Page<PictureVo> getPictureVoPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVo> pictureVoPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollectionUtil.isEmpty(pictureList)) {
            return pictureVoPage;
        }
        List<PictureVo> pictureVoList = pictureList.stream().map(PictureVo::objToVo).collect(Collectors.toList());
        List<Long> userIds = pictureList.stream().map(Picture::getUserId).collect(Collectors.toList());
        Map<Long, List<User>> idUserMap = userService.listByIds(userIds)
                .stream()
                .collect(Collectors.groupingBy(User::getId));
        pictureVoList.stream().forEach(pictureVo -> {
            Long userId = pictureVo.getUserId();
            User user = null;
            if (idUserMap.containsKey(userId)) {
                user = idUserMap.get(userId).get(0);
            }
            pictureVo.setUserVo(userService.getUserVo(user));
        });
        pictureVoPage.setRecords(pictureVoList);
        return pictureVoPage;
    }
}




