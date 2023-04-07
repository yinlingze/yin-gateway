package com.yin.yingateway;

import com.yin.yingateway.service.GlobeGateway;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class YinGatewayApplicationTests {

    @Resource
    GlobeGateway yinapiGateway;

    @Test
    void contextLoads() throws InterruptedException {
        String sk = "qqq";
        String aa = "qwqqw";
    }

}
