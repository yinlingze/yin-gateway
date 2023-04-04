package com.yin.yingateway;

import com.yin.yingateway.service.YinapiGateway;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class YinGatewayApplicationTests {

    @Resource
    YinapiGateway yinapiGateway;

    @Test
    void contextLoads() throws InterruptedException {
        String sk = "qqq";
        String aa = "qwqqw";
    }

}
