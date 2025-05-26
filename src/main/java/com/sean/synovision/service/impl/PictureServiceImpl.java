package com.sean.synovision.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sean.synovision.costant.UserConstant;
import com.sean.synovision.exception.BussinessException;
import com.sean.synovision.exception.ErrorCode;
import com.sean.synovision.manager.upload.FileUploadPictureTemplateImpl;
import com.sean.synovision.manager.upload.UploadPictureTemplate;
import com.sean.synovision.manager.upload.UrlUploadPictureTemplateImpl;
import com.sean.synovision.mapper.PictureMapper;
import com.sean.synovision.model.dto.file.UploadPictureResult;
import com.sean.synovision.model.dto.picture.PictureQueryRequest;
import com.sean.synovision.model.dto.picture.PictureReviewRequest;
import com.sean.synovision.model.dto.picture.PictureUploadByBatchRequest;
import com.sean.synovision.model.dto.picture.PictureUploadRequest;
import com.sean.synovision.model.entity.Picture;
import com.sean.synovision.model.entity.User;
import com.sean.synovision.model.enums.PictureReviewEnum;
import com.sean.synovision.model.vo.picture.PictureVo;
import com.sean.synovision.model.vo.user.UserVo;
import com.sean.synovision.service.PictureService;
import com.sean.synovision.service.UserService;
import com.sean.synovision.utill.ThrowUtill;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author 24395
 * @description 针对表【picture(图片表)】的数据库操作Service实现
 * @createDate 2025-05-21 11:20:34
 */
@Slf4j
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {


    @Resource
    private FileUploadPictureTemplateImpl fileUploadPictureTemplate;

    @Resource
    private UrlUploadPictureTemplateImpl urlUploadPictureTemplate;

    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final Cache<String, String> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(1024)
            .maximumSize(10_000L)
            .expireAfterWrite(Duration.ofSeconds(5))//原来是分钟单位
            .build();

    @Override
    public void vaildPicture(Picture picture) {
        ThrowUtill.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        Long id = picture.getId();
        String name = picture.getName();
        ;
        String introduction = picture.getIntroduction();
        ThrowUtill.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        ThrowUtill.throwIf(StrUtil.isBlank(name), ErrorCode.PARAMS_ERROR);
        ThrowUtill.throwIf(StrUtil.isBlank(introduction) || introduction.length() > 800, ErrorCode.PARAMS_ERROR);

    }


    //todo 上传图片太慢了
    @Override
    public PictureVo uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User user) {
        ThrowUtill.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);
        //判断是否为新增
        Long pictureId = null;
        String namePrefix = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
            namePrefix = pictureUploadRequest.getNamePrefix();
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
        final String FILE_PREFIX = String.format("/public/%s", user.getId());
        UploadPictureTemplate uploadPictureTemplate = fileUploadPictureTemplate;
        if (inputSource instanceof String) {
            uploadPictureTemplate = urlUploadPictureTemplate;
        }
        UploadPictureResult uploadPictureResult = uploadPictureTemplate.uploadFile(inputSource, FILE_PREFIX);
        // 保存图片信息到数据库

        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());
        String picName = uploadPictureResult.getPicName();
        if (namePrefix != null) {
            picName = namePrefix + picName;
        }
        picture.setName(picName);
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
        this.fullPictureReviewPramas(picture, user);
        boolean update = this.saveOrUpdate(picture);
        ThrowUtill.throwIf(!update, ErrorCode.OPERATION_ERROR);
        return PictureVo.objToVo(picture);
    }

    @Override
    public Integer uploadPictureBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User user) {
        //1. 校验参数
        String searchText = pictureUploadByBatchRequest.getSearchText();
        Integer count = pictureUploadByBatchRequest.getCount();
        ThrowUtill.throwIf(count == null || count > 10, ErrorCode.PARAMS_ERROR, "最多10条");
        String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }
        //2. 抓取网站内容
        String fileUrl = String.format("https://cn.bing.com/images/search?q=%s&mmasync=1", searchText);
        Document document = null;
        try {
            document = Jsoup.connect(fileUrl).get();
        } catch (IOException e) {
            log.error("抓取失败", e);
            throw new BussinessException(ErrorCode.OPERATION_ERROR, "抓取失败");
        }
        //3. 解析内容
        Element element = document.getElementsByClass("dgControl").first();
        ThrowUtill.throwIf(element == null, ErrorCode.OPERATION_ERROR, "获取图片网站元素失败");
        Elements elements = element.select("img.mimg");
        int uploadCount = 0;
        //todo 我发现我只需要 2 张图片，但是他获取的所有的图片 <img :src="" /> 标签
        for (Element e : elements) {
            //获取图片列表的url
            // https://tse1-mm.cn.bing.net/th/id/OIP-C.Ec0dXTZ5ZvsGqCK5JtUX3QHaJ_?w=194&amp;h=262&amp;c=7&amp;r=0&amp;o=7&amp;cb=iwp2&amp;pid=1.7&amp
            // https://tse1-mm.cn.bing.net/th/id/OIP-C.Ec0dXTZ5ZvsGqCK5JtUX3QHaJ_
            String pictureUrl = e.attr("src");
            if (StrUtil.isBlank(pictureUrl)) {
                log.info("当前链接为空，已经跳过，{}", pictureUrl);
                continue;
            }
            // 图片url的处理
            // 因为图片url可能携带参数 ：?id=1&w=qqq 这种的，会和腾讯云cos起冲突
            int questionMarkIndex = pictureUrl.indexOf("?");
            if (questionMarkIndex > -1) { //图片解析结果 https://tse1-mm.cn.bing.net/th/id
                pictureUrl = pictureUrl.substring(0, questionMarkIndex); //[)
            }
            //4. 上传图片
            try {
                PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
                pictureUploadRequest.setFileUrl(pictureUrl);
                pictureUploadRequest.setNamePrefix(namePrefix + (uploadCount + 1));
                PictureVo pictureVo = this.uploadPicture(pictureUrl, pictureUploadRequest, user);
                log.info("上传成功，id = {}", pictureVo.getId());
                uploadCount++;
            } catch (Exception ex) {
                log.error("上传失败", ex);
                continue;
            }
            if (uploadCount >= count) {
                break;
            }
        }
        return uploadCount;
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
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "参数异常");
        }
        //2. 业务校验
        Picture oldPicture = this.getById(id);
        ThrowUtill.throwIf(oldPicture == null, ErrorCode.PICTURE_NOT_EXIST);
        //2.1 判断是否 是重复审核
        Integer oldReviewStatus = oldPicture.getReviewStatus();
        if (oldReviewStatus == null || oldReviewStatus.equals(reviewStatus)) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "参数异常");
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

        //1.查询redis缓存前，查询本地缓存
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String localKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String localCacheData = LOCAL_CACHE.getIfPresent(localKey);
        if (StrUtil.isNotBlank(localCacheData)) {
            return JSONUtil.toBean(localCacheData, Page.class);
        }

        // 2.先查询redis缓存，如果存在则直接返回
        String redisKey = String.format("SynoVision:listPictureVoPage:%s", localKey);
        ValueOperations<String, String> stringStringValueOperations = stringRedisTemplate.opsForValue();
        String cachedValue = stringStringValueOperations.get(redisKey);
        if (StrUtil.isNotBlank(cachedValue)) {
            return JSONUtil.toBean(cachedValue, Page.class);
        }

        //3.查询数据库
        //只允许查询已经通过的照片
        pictureQueryRequest.setReviewStatus(PictureReviewEnum.PASS.getValue());
        Page<Picture> picturePage = listPicturePage(pictureQueryRequest);
        Page<PictureVo> pictureVoPage = this.getPictureVoPage(picturePage, request);

        //4.更新缓存
        String pictureVoPageStr = JSONUtil.toJsonStr(pictureVoPage);
        //4.1 更新本地缓存
        LOCAL_CACHE.put(localKey, pictureVoPageStr);
        //4.2 更新redis缓存
        // 设置过期时间， 5 - 10 分钟，防止缓存雪崩
        int cacheTime = 300 + RandomUtil.randomInt(0,300);
        stringStringValueOperations.set(redisKey, pictureVoPageStr,cacheTime,TimeUnit.SECONDS);
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
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "参数异常");
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
                    qw -> qw.like("name", searchText)
                            .or()
                            .like("introduction", searchText)
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
        pictureQueryWrapper.orderBy(StrUtil.isNotEmpty(sortOrder), sortOrder.equals("ascend"), sortField);
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




