package com.sean.synovision.api.ailyunai;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.sean.synovision.api.ailyunai.model.CreateOutPaintingTaskRequest;
import com.sean.synovision.api.ailyunai.model.CreateOutPaintingTaskResponse;
import com.sean.synovision.api.ailyunai.model.GetOutPaintingTaskResponse;
import com.sean.synovision.exception.BussinessException;
import com.sean.synovision.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLHandshakeException;
import java.net.ConnectException;
import java.net.SocketException;

@Slf4j
@Component
public class AliYunAiApi {
// 文档地址：https://bailian.console.aliyun.com/?switchAgent=12104993&productCode=p_efm&switchUserType=3&tab=api#/doc/?type=model&url=https%3A%2F%2Fhelp.aliyun.com%2Fdocument_detail%2F2796845.html

    // 读取配置文件
    @Value("${aliYunAi.apiKey}")
    private String apiKey;

    // 创建任务地址           https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting
    public static final String CREATE_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";

    // 查询任务状态           https://dashscope.aliyuncs.com/api/v1/tasks/{your_task_id}
    public static final String GET_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";


    /**
     * 创建任务
     *
     * @param createOutPaintingTaskRequest
     * @return
     */
    public CreateOutPaintingTaskResponse createOutPaintingTask(CreateOutPaintingTaskRequest createOutPaintingTaskRequest) {
        if (createOutPaintingTaskRequest == null) {
            throw new BussinessException(ErrorCode.OPERATION_ERROR, "扩图参数为空");
        }
        // 发送请求
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(CREATE_OUT_PAINTING_TASK_URL);
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setHeader("X-DashScope-Async", "enable");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(JSONUtil.toJsonStr(createOutPaintingTaskRequest)));
            try (CloseableHttpResponse response  = httpClient.execute(httpPost);) {
                String responseBody = EntityUtils.toString(response.getEntity());
                if (response.getStatusLine().getStatusCode() != 200) {
                    log.error("请求异常：{}", responseBody);
                    throw new BussinessException(ErrorCode.OPERATION_ERROR, "AI 扩图失败");
                }
                CreateOutPaintingTaskResponse createOutPaintingTaskResponse = JSONUtil.toBean(responseBody, CreateOutPaintingTaskResponse.class);
                if (createOutPaintingTaskResponse.getCode() != null) {
                    String errorMessage = createOutPaintingTaskResponse.getMessage();
                    log.error("请求异常：{}", errorMessage);
                    throw new BussinessException(ErrorCode.OPERATION_ERROR, "AI 扩图失败，" + errorMessage);
                }
                return createOutPaintingTaskResponse;
            }
        }catch (Exception e) {
            log.error("请求异常详细信息:", e);
            throw new BussinessException(ErrorCode.OPERATION_ERROR, "AI 扩图失败");
        }
    }

    /**
     * 查询创建的任务结果
     *
     * @param taskId
     * @return
     */
    public GetOutPaintingTaskResponse getOutPaintingTask(String taskId) {
        if (StrUtil.isBlank(taskId)) {
            throw new BussinessException(ErrorCode.OPERATION_ERROR, "任务 ID 不能为空");
        }
        // 处理响应
        String url = String.format(GET_OUT_PAINTING_TASK_URL, taskId);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Authorization", "Bearer " + apiKey);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                if (response.getStatusLine().getStatusCode() != 200) {
                    log.error("请求异常：{}", responseBody);
                    throw new BussinessException(ErrorCode.OPERATION_ERROR, "获取任务结果失败");
                }
                return JSONUtil.toBean(responseBody, GetOutPaintingTaskResponse.class);
            }
        } catch (Exception e) {
            log.error("请求异常详细信息:", e);
            throw new BussinessException(ErrorCode.OPERATION_ERROR, "获取任务结果失败");
        }
//        try (HttpResponse httpResponse = HttpRequest.get(url)
//                .header("Authorization", "Bearer " + apiKey)
//                .execute()) {
//            if (!httpResponse.isOk()) {
//                log.error("请求异常：{}", httpResponse.body());
//                throw new BussinessException(ErrorCode.OPERATION_ERROR, "获取任务结果失败");
//            }
//            return JSONUtil.toBean(httpResponse.body(), GetOutPaintingTaskResponse.class);
//        }
    }
}