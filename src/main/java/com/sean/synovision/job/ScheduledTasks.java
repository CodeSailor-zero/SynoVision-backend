package com.sean.synovision.job;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.sean.synovision.mapper.PictureMapper;
import com.sean.synovision.model.entity.Picture;
import com.sean.synovision.model.entity.Tag;
import com.sean.synovision.service.PictureService;
import com.sean.synovision.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author sean
 * @Date 2025/12/24
 */
@Slf4j
@Component
public class ScheduledTasks {

    @Resource
    private PictureService pictureService;

    @Resource
    private PictureMapper pictureMapper;

    @Resource
    private TagService tagService;

    @Resource
    private TransactionTemplate transactionTemplate;

    //每周日晚上00:00开始 统计图片tag的使用情况
    @Scheduled(cron = "0 0 0 * * SUN", zone = "Asia/Shanghai")
    public void everyWeekTagCount() {
        // 3.构建查询条件进行查询
        QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
        pictureQueryWrapper.select("tags");
        List<Object> tagsStrList = pictureService.getBaseMapper().selectObjs(pictureQueryWrapper);
        List<String> tagsList = tagsStrList.stream()
                .filter(ObjUtil::isNotNull)
                .map(Object::toString)
                .collect(Collectors.toList());
        //将标签扁平化 ： ["a","b"],["c","d"]] -> ["a","b","c","d"]
        Map<String, Long> tagCountMap = tagsList.stream()
                .flatMap(tagsStr -> JSONUtil.toList(tagsStr, String.class).stream())
                .collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));
        // 将次数存储到 tag数据库
        tagCountMap.entrySet().forEach(entry -> {
            if (StrUtil.isBlank(entry.getKey())) {
                return;
            }
            UpdateWrapper<Tag> tagUpdateWrapper = new UpdateWrapper<>();
            tagUpdateWrapper.set("tagCount", entry.getValue())
                    .eq("tagName", entry.getKey());
            log.info("此处的sql：{}", tagUpdateWrapper.getSqlSet());
            tagService.update(tagUpdateWrapper);
        });
    }

    //每个月的1号0点执行一次，清理一个月之前的删除的图片
    @Scheduled(cron = "0 0 0 1 * ?", zone = "Asia/Shanghai")
    public void clearPicture() {
        // 删除cos图片
        transactionTemplate.execute(status -> {
            pictureMapper.selectDeletedPictureUrls().forEach(map -> {
                String url = map.get("url").toString();
                String thumbnailUrl = map.get("thumbnailUrl").toString();
                String originalUrl = map.get("originalUrl").toString();
                Long spaceId = (Long) map.get("spaceId");
                // 编写方法删除cos图片
                pictureService.deletePictureInCos(url, thumbnailUrl, originalUrl,spaceId);
            });
            pictureMapper.deletePicture();
            return null;
        });
    }
}
