package com.sean.synovision.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sean.synovision.exception.BussinessException;
import com.sean.synovision.exception.ErrorCode;
import com.sean.synovision.manager.FileManager;
import com.sean.synovision.model.dto.file.UploadPictureResult;
import com.sean.synovision.model.dto.picture.PictureQueryRequest;
import com.sean.synovision.model.dto.picture.PictureUploadRequest;
import com.sean.synovision.model.entity.Picture;
import com.sean.synovision.model.entity.User;
import com.sean.synovision.model.vo.picture.PictureVo;
import com.sean.synovision.model.vo.user.UserVo;
import com.sean.synovision.service.PictureService;
import com.sean.synovision.mapper.PictureMapper;
import com.sean.synovision.service.UserService;
import com.sean.synovision.utill.ThrowUtill;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
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
            boolean exists = this.lambdaQuery()
                    .eq(Picture::getId, pictureId)
                    .exists();
            ThrowUtill.throwIf(!exists, ErrorCode.PICTURE_NOT_EXIST);
        }
        //上传图片
        final String FILE_PREFIX = String.format("/public/%s",user.getId());
        UploadPictureResult uploadPictureResult = fileManager.uploadFile(multipartFile, FILE_PREFIX);
        // 保存图片信息到数据库
        Picture picture = new Picture();
        // //public/1924703832596598786/2025-05-21_ced42e9c-12c7-451f-80eb-bb336ee5da4d.jpg
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(user.getId());
        boolean update = this.saveOrUpdate(picture);
        ThrowUtill.throwIf(!update, ErrorCode.OPERATION_ERROR);
        return PictureVo.objToVo(picture);
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
        pictureQueryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        pictureQueryWrapper.eq(ObjectUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        pictureQueryWrapper.eq(ObjectUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        pictureQueryWrapper.eq(ObjectUtil.isNotEmpty(picSize), "picSize", picSize);
        pictureQueryWrapper.eq(ObjectUtil.isNotEmpty(picScale), "picScale", picScale);
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
        ThrowUtill.throwIf(CollectionUtil.isEmpty(pictureList), ErrorCode.PARAMS_ERROR);
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




