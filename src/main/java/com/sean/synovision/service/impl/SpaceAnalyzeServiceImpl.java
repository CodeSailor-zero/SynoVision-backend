package com.sean.synovision.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sean.synovision.exception.BussinessException;
import com.sean.synovision.exception.ErrorCode;
import com.sean.synovision.mapper.SpaceMapper;
import com.sean.synovision.model.dto.space.analyze.*;
import com.sean.synovision.model.entity.Picture;
import com.sean.synovision.model.entity.Space;
import com.sean.synovision.model.entity.User;
import com.sean.synovision.model.vo.space.analyze.*;
import com.sean.synovision.service.*;
import com.sean.synovision.utill.ThrowUtill;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author sean
 * @Date 2025/05/30
 */
@Service
public class SpaceAnalyzeServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceAnalyzeService {

    @Resource
    private UserService userService;
    @Resource
    private SpaceService spaceService;

    @Resource
    private PictureService pictureService;

    @Resource
    private TagService tagService;

    @Override
    public SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser) {
        // 1. 校验参数
        boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
        boolean queryAll = spaceAnalyzeRequest.isQueryAll();
        SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = null;
        // 2. 校验权限
        if (queryPublic || queryAll) {
            // 需要管理员权限
            checkSpaceAnalyzeAuth(spaceAnalyzeRequest, loginUser);
            // 查询公开或者全部，从picture表查询
            QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
            pictureQueryWrapper.select("picSize");
            fillAnalyzeQueryWrapper(spaceAnalyzeRequest, pictureQueryWrapper);
            List<Object> pictureObjs = pictureService.getBaseMapper().selectObjs(pictureQueryWrapper);
            long picSize = pictureObjs.stream().mapToLong(obj -> (Long) obj).sum();
            long usedCount = pictureObjs.size();
            // 封装返回结果
            spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();
            spaceUsageAnalyzeResponse.setUsedSize(picSize);
            spaceUsageAnalyzeResponse.setUsedCount(usedCount);
            spaceUsageAnalyzeResponse.setMaxSize(null);
            spaceUsageAnalyzeResponse.setSizeUsageRation(null);
            spaceUsageAnalyzeResponse.setMaxCount(null);
            spaceUsageAnalyzeResponse.setCountUsageRation(null);
            return spaceUsageAnalyzeResponse;
        } else {
            Long spaceId = spaceAnalyzeRequest.getSpaceId();
            ThrowUtill.throwIf(spaceId == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            // 需要空间权限，即本人或管理员
            checkSpaceAnalyzeAuth(spaceAnalyzeRequest, loginUser);
            // 查询特定空间
            Space space = spaceService.getById(spaceId);
            // 封装返回结果
            spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();
            spaceUsageAnalyzeResponse.setUsedSize(space.getTotalSize());
            spaceUsageAnalyzeResponse.setUsedCount(space.getTotalCount());
            spaceUsageAnalyzeResponse.setMaxSize(space.getMaxSize());
            spaceUsageAnalyzeResponse.setMaxCount(space.getMaxCount());
            // 计算百分比
            double sizeUsageRation = NumberUtil.round(space.getTotalSize() * 100.0 / space.getMaxSize(), 2).doubleValue();
            double countUsageRation = NumberUtil.round(space.getTotalCount() * 100.0 / space.getMaxCount(), 2).doubleValue();
            spaceUsageAnalyzeResponse.setSizeUsageRation(sizeUsageRation);
            spaceUsageAnalyzeResponse.setCountUsageRation(countUsageRation);
        }
        return spaceUsageAnalyzeResponse;
    }

    @Override
    public List<SpaceCategoryAnalyzeResponse> getPictureCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser) {
        // 1. 校验参数
        ThrowUtill.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        // 2. 校验权限
        checkSpaceAnalyzeAuth(spaceCategoryAnalyzeRequest, loginUser);
        // 3.构建查询条件进行查询
        QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceCategoryAnalyzeRequest, pictureQueryWrapper);
        pictureQueryWrapper.select("category", "count(*) as count", "sum(picSize) as totalSize")
                .groupBy("category");
        return pictureService.getBaseMapper().selectMaps(pictureQueryWrapper)
                .stream()
                .map(res -> {
                    String category = (String) res.get("category");
                    Long count = ((Number) res.get("count")).longValue();
                    Long totalSize = ((Number) res.get("totalSize")).longValue();
                    return new SpaceCategoryAnalyzeResponse(category, count, totalSize);
                }).collect(Collectors.toList());
    }

    /**
     * 查询图片标签使用情况
     *
     * @param spaceTagAnalyzeRequest
     * @param loginUser
     * @return
     */
    @Override
    public List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser) {
        // 1.检查权限
        ThrowUtill.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        // 2. 校验权限
        checkSpaceAnalyzeAuth(spaceTagAnalyzeRequest, loginUser);
        // 3.构建查询条件进行查询
        QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceTagAnalyzeRequest, pictureQueryWrapper);
        pictureQueryWrapper.select("tags");
        List<Object> tagsStrList = pictureService.getBaseMapper().selectObjs(pictureQueryWrapper);
        List<String> tagsList = tagsStrList.stream()
                .filter(ObjUtil::isNotNull)
                .map(Object::toString)
                .collect(Collectors.toList());
        //将标签扁平化 ： ["a","b"],["c","d"]] -> ["a","b","c","d"]
        Map<String, Long> collect = tagsList.stream()
                .flatMap(tagsStr -> JSONUtil.toList(tagsStr, String.class).stream())
                .collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));
        // 转化为响应对象
        return collect.entrySet()
                .stream()
                .map(entry -> new SpaceTagAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * 查询空间大小使用情况
     *
     * @param spaceSizeAnalyzeRequest
     * @param loginUser
     * @return
     */
    @Override
    public List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser) {
        // 1. 校验参数
        ThrowUtill.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        // 2. 校验权限
        checkSpaceAnalyzeAuth(spaceSizeAnalyzeRequest, loginUser);
        // 3.构建查询条件进行查询
        QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceSizeAnalyzeRequest, pictureQueryWrapper);
        pictureQueryWrapper.select("picSize");
        List<Object> picSizeStrList = pictureService.getBaseMapper().selectObjs(pictureQueryWrapper);
        List<Long> picSizeList = picSizeStrList.stream()
                .filter(ObjUtil::isNotNull)
                .map(size -> (Long) size)
                .collect(Collectors.toList());
        //定义分段范围，注意使用有序的的map
        Map<String, Long> sizeRanges = new LinkedHashMap<>();
        sizeRanges.put("<100KB", picSizeList.stream().filter(size -> size < 1024 * 100).count());
        sizeRanges.put("100KB-500KB", picSizeList.stream().filter(size -> size >= 1024 * 100 && size < 1024 * 500).count());
        sizeRanges.put("500KB-1MB", picSizeList.stream().filter(size -> size >= 1024 * 500 && size < 1024 * 1024).count());
        sizeRanges.put(">1MB", picSizeList.stream().filter(size -> size > 1024 * 1024).count());

        // 转化为响应对象
        return sizeRanges.entrySet()
                .stream()
                .map(entry -> new SpaceSizeAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * 查询用户使用情况
     *
     * @param spaceUserAnalyzeRequest
     * @param loginUser
     * @return
     */
    @Override
    public List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser) {
        // 1. 校验参数
        ThrowUtill.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        // 2. 校验权限
        checkSpaceAnalyzeAuth(spaceUserAnalyzeRequest, loginUser);
        // 3.构建查询条件进行查询
        QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceUserAnalyzeRequest, pictureQueryWrapper);
        Long userId = spaceUserAnalyzeRequest.getUserId();
        pictureQueryWrapper.eq(ObjUtil.isNotNull(userId), "userId", userId);
        String timeDimension = spaceUserAnalyzeRequest.getTimeDimension();
        switch (timeDimension) {
            case "day":
                pictureQueryWrapper.select("DATE_FORMAT(createTime, '%Y-%m-%d') as period", "count(*) as count");
                break;
            case "week":
                pictureQueryWrapper.select("YEARWEEK(createTime) as period", "count(*) as count");
                break;
            case "month":
                pictureQueryWrapper.select("DATE_FORMAT(createTime, '%Y-%m') as period", "count(*) as count");
                break;
            default:
                throw new BussinessException(ErrorCode.PARAMS_ERROR, "时间维度错误");
        }
        // 分组查询
        pictureQueryWrapper.groupBy("period").orderByAsc("period");

        //查询封装返回结果
        List<Map<String, Object>> maps = pictureService.getBaseMapper().selectMaps(pictureQueryWrapper);

        return maps
                .stream()
                .map(map ->
                        new SpaceUserAnalyzeResponse(map.get("period").toString(),
                                ((Number) map.get("count")).longValue()))
                .collect(Collectors.toList());
    }

    /**
     * 查询空间使用情况排名（仅管理员使用）
     *
     * @param spaceRankAnalyzeRequest
     * @param loginUser
     * @return
     */
    @Override
    public List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser) {
        // 1.  校验参数
        ThrowUtill.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        // 2.权限校验
        ThrowUtill.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "需要管理员权限");
        // 3.构建查询条件进行查询
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "spaceName", "userId", "totalSize")
                .eq("id", spaceRankAnalyzeRequest.getSpaceId())
                .orderByDesc("totalSize")
                .last("limit " + spaceRankAnalyzeRequest.getTopNum());
        return spaceService.list(queryWrapper);
    }

    /**
     * 检验空间分析权限
     *
     * @param spaceAnalyzeRequest
     * @param loginUser
     */
    public void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser) {

        boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
        boolean queryAll = spaceAnalyzeRequest.isQueryAll();
        if (queryPublic || queryAll) {
            // 需要管理员权限
            ThrowUtill.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "需要管理员权限");
        } else {
            // 需要空间权限，即本人或管理员
            Long spaceId = spaceAnalyzeRequest.getSpaceId();
            ThrowUtill.throwIf(spaceId == null, ErrorCode.PARAMS_ERROR, "空间ID不能为空");
            Space space = spaceService.getById(spaceId);
            ThrowUtill.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            spaceService.checkSpaceAuth(loginUser, space);
        }
    }

    /**
     * 根据不同的请求，封装QueryWrapper
     *
     * @param spaceAnalyzeRequest
     * @param queryWrapper
     */
    public void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper) {
        boolean queryAll = spaceAnalyzeRequest.isQueryAll();
        if (queryAll) {
            //查询所有
            return;
        }
        boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
        if (queryPublic) {
            // 查询公开
            queryWrapper.isNull("spaceId");
        }
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        if (spaceId != null) {
            // 查询特定空间
            queryWrapper.eq("spaceId", spaceId);
        }
    }
}
