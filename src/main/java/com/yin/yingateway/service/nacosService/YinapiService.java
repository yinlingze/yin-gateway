package com.yin.yingateway.service.nacosService;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Author: yin7331
 * Date: 2023/4/13 11:20
 * Describe:
 */
@Component
@FeignClient(name = "springboot-init11111")
public interface YinapiService {
    /**
     * api接口请求的计数
     *
     * @param apiSign
     * @return api/requestGateway/invokeCount/"+apiSign;
     */
    @GetMapping("/api/requestGateway/invokeCount/{apiSign}")
    public Boolean invokeCount(@PathVariable("apiSign") String apiSign);

    /**
     * 检查这个api是否存在
     * @param apiSign
     * @return
     */
    @GetMapping("/api/requestGateway/invokeApiSign/{apiSign}")
    public Boolean invokeApiSign(
            @PathVariable("apiSign") String apiSign);


    /**
     * 检查是否伪造了时间戳
     * @param timeStamp
     * @param genSign
     * @param accessKey
     * @return
     */
    @GetMapping("/api/requestGateway/checkTimeStamp/{accessKey}/{timeStamp}/{genSign}")
    public Boolean checkTimeStamp(@PathVariable("timeStamp") String timeStamp,
                                  @PathVariable("genSign") String genSign,
                                  @PathVariable("accessKey") String accessKey
    );


    /**
     * 检查api的请求频率
     * @param sign
     * @param timestamp
     * @return
     */
    @GetMapping("/api/requestGateway/isFrequenceAllowed/{sign}/{timestamp}")
    public Boolean isFrequenceAllowed(
            @PathVariable("sign") String sign,
            @PathVariable("timestamp") Long timestamp
    );

}
