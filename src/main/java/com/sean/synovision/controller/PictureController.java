package com.sean.synovision.controller;


import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sean.synovision.annotation.AuthCheck;
import com.sean.synovision.api.ailyunai.AliYunAiApi;
import com.sean.synovision.api.ailyunai.model.CreateOutPaintingTaskRequest;
import com.sean.synovision.api.ailyunai.model.CreateOutPaintingTaskResponse;
import com.sean.synovision.api.ailyunai.model.GetOutPaintingTaskResponse;
import com.sean.synovision.common.BaseResponse;
import com.sean.synovision.common.DeleteRequest;
import com.sean.synovision.costant.UserConstant;
import com.sean.synovision.exception.BussinessException;
import com.sean.synovision.exception.ErrorCode;
import com.sean.synovision.exception.ResultUtils;
import com.sean.synovision.model.dto.picture.*;
import com.sean.synovision.model.dto.space.SpaceLevel;
import com.sean.synovision.model.entity.Picture;
import com.sean.synovision.model.entity.User;
import com.sean.synovision.model.enums.SpaceLevelEnum;
import com.sean.synovision.model.vo.picture.PictureTagCategeoy;
import com.sean.synovision.model.vo.picture.PictureVo;
import com.sean.synovision.service.PictureService;
import com.sean.synovision.service.TagService;
import com.sean.synovision.service.UserService;
import com.sean.synovision.utill.ThrowUtill;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author sean
 * @Date 2025/13/21
 */
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private UserService userService;
    @Resource
    private PictureService pictureService;


    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private TagService tagService;

    @Resource
    private AliYunAiApi aliYunAiApi;

    // region 业务操作

    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest,
                                                 HttpServletRequest request) {
        // 1. 参数校验
        ThrowUtill.throwIf(pictureReviewRequest == null || pictureReviewRequest.getId() < 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }

    //    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/upload")
    public BaseResponse<PictureVo> upload(
            @RequestPart("file") MultipartFile file,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVo pictureVo = pictureService.uploadPicture(file, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVo);
    }

    @PostMapping("/upload/url")
    public BaseResponse<PictureVo> uploadByUrl(@RequestBody PictureUploadRequest pictureUploadRequest,
                                               HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVo pictureVo = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVo);
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/upload/batch")
    public BaseResponse<Integer> uploadByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
                                               HttpServletRequest request) {
        ThrowUtill.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        int uploadCount = pictureService.uploadPictureBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(uploadCount);
    }

    // endregion

    //region 增删改查
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/delete/admin")
    public BaseResponse<Boolean> deletePictureByAdmin(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtill.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = deleteRequest.getId();
        ThrowUtill.throwIf(id == null || id < 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        userService.isAdmin(loginUser);
        boolean result = pictureService.deletePictureByAdmin(id, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtill.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = deleteRequest.getId();
        ThrowUtill.throwIf(id == null || id < 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = pictureService.deletePicture(id, loginUser);
        return ResultUtils.success(result);
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/update")
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        ThrowUtill.throwIf(pictureUpdateRequest == null || pictureUpdateRequest.getId() < 0, ErrorCode.PARAMS_ERROR);
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        List<String> tags = pictureUpdateRequest.getTags();
        picture.setTags(JSONUtil.toJsonStr(tags));
        pictureService.vaildPicture(picture);
        Picture oldPicture = pictureService.getById(picture.getId());
        ThrowUtill.throwIf(oldPicture == null, ErrorCode.PARAMS_ERROR, "当前图片不存在");
        //补充图片审核校验参数
        User loginUser = userService.getLoginUser(request);
        pictureService.fullPictureReviewPramas(picture, loginUser);
        boolean result = pictureService.updateById(picture);
        ThrowUtill.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片更新失败");
        return ResultUtils.success(result);
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/get")
    public BaseResponse<Picture> getPicture(Long id, HttpServletRequest request) {
        ThrowUtill.throwIf(id == null || id < 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Picture picture = pictureService.getById(id);
        ThrowUtill.throwIf(picture == null, ErrorCode.PARAMS_ERROR, "当前图片不存在");
        return ResultUtils.success(picture);
    }

    @GetMapping("/get/vo")
    public BaseResponse<PictureVo> getPictureVo(Long id, HttpServletRequest request) {
        ThrowUtill.throwIf(id == null || id < 0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtill.throwIf(picture == null, ErrorCode.PARAMS_ERROR, "当前图片不存在");
        User loginUser = userService.getLoginUser(request);
        PictureVo pictureVo = pictureService.getPictureVo(picture, loginUser);
        return ResultUtils.success(pictureVo);
    }

    @PostMapping("/list")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPicture(@RequestBody PictureQueryRequest pictureQueryRequest) {
        Page<Picture> picturePage = pictureService.listPicturePage(pictureQueryRequest);
        return ResultUtils.success(picturePage);
    }

    @PostMapping("/list/vo")
    public BaseResponse<Page<PictureVo>> listPictureVo(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        Page<PictureVo> pictureVoPage = pictureService.listPictureVoPage(pictureQueryRequest, request);
        return ResultUtils.success(pictureVoPage);
    }

    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        ThrowUtill.throwIf(pictureEditRequest == null || pictureEditRequest.getId() < 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = pictureService.editPicture(pictureEditRequest, loginUser);
        return ResultUtils.success(result);
    }

    // endregion
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategeoy> getTagCategory() {
        PictureTagCategeoy pictureTagCategeoy = new PictureTagCategeoy();
        List<String> tagNameList = tagService.getTagNameList();
        List<String> categeoyList = Arrays.asList("模板", "表情包", "素材", "海报");
        pictureTagCategeoy.setTagList(tagNameList);
        pictureTagCategeoy.setCategeoyList(categeoyList);
        return ResultUtils.success(pictureTagCategeoy);
    }

    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> getSpaceLevel() {
        List<SpaceLevel> spaceLevelList = Arrays.
                stream(SpaceLevelEnum.values())
                .map(spaceLevelEnum -> {
                    return new SpaceLevel(
                            spaceLevelEnum.getValue(),
                            spaceLevelEnum.getText(),
                            spaceLevelEnum.getMaxCount(),
                            spaceLevelEnum.getMaxSize()
                    );
                })
                .collect(Collectors.toList());
        return ResultUtils.success(spaceLevelList);
    }

    @PostMapping("/out_painting/create_task")
    public BaseResponse<CreateOutPaintingTaskResponse> createOutPaintingTask(@RequestBody CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest,
                                                                             HttpServletRequest request) {
        if (createPictureOutPaintingTaskRequest == null || createPictureOutPaintingTaskRequest.getPictureId() == null) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        User loginUser = userService.getLoginUser(request);
        CreateOutPaintingTaskResponse createOutPaintingTaskResponse = pictureService.createPictureOutPaintingTask(createPictureOutPaintingTaskRequest, loginUser);
        return ResultUtils.success(createOutPaintingTaskResponse);
    }
    @GetMapping("/out_painting/get_task")
    public BaseResponse<GetOutPaintingTaskResponse> getOutPaintingTask(String taskId) {
        ThrowUtill.throwIf(taskId == null, ErrorCode.PARAMS_ERROR);
        GetOutPaintingTaskResponse task = aliYunAiApi.getOutPaintingTask(taskId);
        return ResultUtils.success(task);
    }
}
