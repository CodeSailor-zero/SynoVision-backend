package com.sean.synovision.controller;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sean.synovision.annotation.AuthCheck;
import com.sean.synovision.common.BaseResponse;
import com.sean.synovision.common.DeleteRequest;
import com.sean.synovision.costant.UserConstant;
import com.sean.synovision.exception.BussinessException;
import com.sean.synovision.exception.ErrorCode;
import com.sean.synovision.exception.ResultUtils;
import com.sean.synovision.model.dto.picture.PictureEditRequest;
import com.sean.synovision.model.dto.picture.PictureQueryRequest;
import com.sean.synovision.model.dto.picture.PictureUpdateRequest;
import com.sean.synovision.model.dto.picture.PictureUploadRequest;
import com.sean.synovision.model.dto.tag.TagQueryRequest;
import com.sean.synovision.model.entity.Picture;
import com.sean.synovision.model.entity.Tag;
import com.sean.synovision.model.entity.User;
import com.sean.synovision.model.vo.picture.PictureTagCategeoy;
import com.sean.synovision.model.vo.picture.PictureVo;
import com.sean.synovision.model.vo.tag.TagVo;
import com.sean.synovision.service.PictureService;
import com.sean.synovision.service.TagService;
import com.sean.synovision.service.UserService;
import com.sean.synovision.utill.ThrowUtill;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
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


    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/upload")
    public BaseResponse<PictureVo> upload(
            @RequestPart("file") MultipartFile file,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVo pictureVo = pictureService.uploadPicture(file, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVo);
    }

    //region 增删改查
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtill.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = deleteRequest.getId();
        ThrowUtill.throwIf(id == null || id < 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Picture oldPicture = pictureService.getById(id);
        ThrowUtill.throwIf(oldPicture == null, ErrorCode.PARAMS_ERROR, "当前图片不存在");
        //权限校验
        Long oldPictureUserId = oldPicture.getUserId();
        Long loginUserId = loginUser.getId();
        boolean isAdmin = userService.isAdmin(loginUser);
        if (!isAdmin && !loginUserId.equals(oldPictureUserId)) {
            throw new BussinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除图片");
        }
        boolean result = pictureService.removeById(id);
        ThrowUtill.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片删除失败");
        return ResultUtils.success(true);
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/update")
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest) {
        ThrowUtill.throwIf(pictureUpdateRequest == null || pictureUpdateRequest.getId() < 0, ErrorCode.PARAMS_ERROR);
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        List<String> tags = pictureUpdateRequest.getTags();
        picture.setTags(JSONUtil.toJsonStr(tags));
        pictureService.vaildPicture(picture);
        Picture oldPicture = pictureService.getById(picture.getId());
        ThrowUtill.throwIf(oldPicture == null, ErrorCode.PARAMS_ERROR, "当前图片不存在");
        boolean result = pictureService.updateById(picture);
        ThrowUtill.throwIf(result, ErrorCode.OPERATION_ERROR, "图片更新失败");
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
        User loginUser = userService.getLoginUser(request);
        Picture picture = pictureService.getById(id);
        ThrowUtill.throwIf(picture == null, ErrorCode.PARAMS_ERROR, "当前图片不存在");
        //
        PictureVo pictureVo = pictureService.getPictureVo(picture,request);
        return ResultUtils.success(pictureVo);
    }

    @PostMapping("/list")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPicture(@RequestBody PictureQueryRequest pictureQueryRequest) {
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }
    // todo 前端主页，如果选择多个tag 就会显示获取数据失败。
    @PostMapping("/list/vo")
public BaseResponse<Page<PictureVo>> listPictureVo(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        // 1. redisson 分布式锁
        // 2. redisson 限流器【令牌】
        // 3. 缓存 √

        // current  size            start   end
        //   1       10          =>  0       9
        //   2       10          =>  10      19
        //   3       10          =>  20      29
        //   4       10          =>  30      39
        //   c       s           => (c-1)*s   cs-1
        User loginUser = userService.getLoginUser(request);
        //先查询redis缓存
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        long start = (long) (current - 1) * pageSize;
        long end = (long) current * pageSize - 1;
        ListOperations listOperations = redisTemplate.opsForList();
        List pageList = listOperations.range("key", start, end);
        if (CollectionUtil.isNotEmpty(pageList)) {
            Page<PictureVo> pictureVoPage = new Page<>(current,pageSize);
            pictureVoPage.setRecords((List<PictureVo>) pageList.get(0));
            return ResultUtils.success(pictureVoPage);
        }

        BaseResponse<Page<Picture>> pictureResponse = listPicture(pictureQueryRequest);
        Page<Picture> pictures = pictureResponse.getData();
        Page<PictureVo> pictureVoPage = pictureService.getPictureVoPage(pictures,request);
        String Key = "PictureVo:" + current + ":" + loginUser.getId();
        // 为了防止前端频繁进行查询，同时秒数小是为了防止数据不一致问题
        listOperations.rightPush(Key,pictureVoPage);
        redisTemplate.expire(Key, 10, TimeUnit.SECONDS);

        //进行 tag 的统计

        return ResultUtils.success(pictureVoPage);
    }

    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest,HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtill.throwIf(pictureEditRequest == null || pictureEditRequest.getId() < 0, ErrorCode.PARAMS_ERROR);
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        List<String> tags = pictureEditRequest.getTags();
        picture.setTags(JSONUtil.toJsonStr(tags));
        picture.setEditTime(new Date());
        pictureService.vaildPicture(picture);
        Picture oldPicture = pictureService.getById(picture.getId());
        ThrowUtill.throwIf(oldPicture == null, ErrorCode.PARAMS_ERROR, "当前图片不存在");
        //权限校验
        Long oldPictureUserId = oldPicture.getUserId();
        Long loginUserId = loginUser.getId();
        boolean isAdmin = userService.isAdmin(loginUser);
        if (!isAdmin && !loginUserId.equals(oldPictureUserId)) {
            throw new BussinessException(ErrorCode.NO_AUTH_ERROR, "无权限编辑图片");
        }
        boolean result = pictureService.updateById(picture);
        ThrowUtill.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片更新失败");
        return ResultUtils.success(result);
    }
    // endregion
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategeoy> getTagCategory() {
        PictureTagCategeoy pictureTagCategeoy = new PictureTagCategeoy();
        List<String> tagNameList = tagService.getTagNameList();
        List<String> categeoyList = Arrays.asList("模板","表情包","素材","海报");
        pictureTagCategeoy.setTagList(tagNameList);
        pictureTagCategeoy.setCategeoyList(categeoyList);
        return ResultUtils.success(pictureTagCategeoy);
    }
}
