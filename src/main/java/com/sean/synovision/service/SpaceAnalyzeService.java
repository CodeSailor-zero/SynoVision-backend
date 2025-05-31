package com.sean.synovision.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sean.synovision.model.dto.space.analyze.*;
import com.sean.synovision.model.entity.Space;
import com.sean.synovision.model.entity.User;
import com.sean.synovision.model.vo.space.analyze.*;

import java.util.List;

/**
 * @author sean
 * @Date 2025/05/30
 */
public interface SpaceAnalyzeService extends IService<Space> {

    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser);

    List<SpaceCategoryAnalyzeResponse> getPictureCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser);

    List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);

    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);

    List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);

    List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser);
}
