package com.yin.yingateway.config;

import com.yin.yingateway.service.nacosService.YinapiService;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

// 这个类的作用，是将Client包装一层，在这里做异步处理
@Slf4j
@Component
public class OpenFeignClientHolder {

    @Lazy // 重点：这里必须使用@Lazy
    @Autowired
    private YinapiService yinapiService;

    @Async // 重点：这里必须在异步线程中执行，执行结果返回Future
    public Future<Boolean> invokeCount(String apiSign) {
        Boolean aBoolean = null;
        try {

            aBoolean = yinapiService.invokeCount(apiSign);

        } catch (Exception e) {
            System.out.println("except:========="+e);
        }
        return (Future<Boolean>) new AsyncResult<Boolean>(aBoolean);

    }

    @Async // 重点：这里必须在异步线程中执行，执行结果返回Future
    public Future<Boolean> checkTimeStamp(String timeStamp, String genSign, String accessKey) {
        Boolean aBoolean = null;
        try {

            aBoolean = yinapiService.checkTimeStamp(timeStamp, genSign, accessKey);


        } catch (Exception e) {
            System.out.println("except:========="+e);
        }
        return (Future<Boolean>) new AsyncResult<Boolean>(aBoolean);

    }

    @Async // 重点：这里必须在异步线程中执行，执行结果返回Future
    public Future<Boolean> invokeApiSign(String apiSign) {

        Boolean aBoolean = null;
        try {

            aBoolean = yinapiService.invokeApiSign(apiSign);


        } catch (Exception e) {
            System.out.println("except:========="+e);
        }
        return (Future<Boolean>) new AsyncResult<Boolean>(aBoolean);
    }

    @Async // 重点：这里必须在异步线程中执行，执行结果返回Future
    public Future<Boolean> isFrequenceAllowed(String sign, Long timeStamp) {

        Boolean aBoolean = null;
        try {

            aBoolean = yinapiService.isFrequenceAllowed(sign, timeStamp);


        } catch (Exception e) {
            System.out.println("except:========="+e);
        }
        return (Future<Boolean>) new AsyncResult<Boolean>(aBoolean);
    }



    @Bean
    @ConditionalOnMissingBean
    public HttpMessageConverters messageConverters(ObjectProvider<HttpMessageConverter<?>> converters) {
        return new HttpMessageConverters(converters.orderedStream().collect(Collectors.toList()));
    }

}
