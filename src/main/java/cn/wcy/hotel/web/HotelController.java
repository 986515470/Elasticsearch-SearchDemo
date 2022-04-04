package cn.wcy.hotel.web;

import cn.wcy.hotel.pojo.PageResult;
import cn.wcy.hotel.pojo.RequestParams;
import cn.wcy.hotel.service.IHotelService;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author Wcy
 * @Date 2022/4/4 20:36
 */
@RestController
@RequestMapping("/hotel")
public class HotelController {

    @Resource
    private IHotelService hotelService;

    @Bean
    public RestHighLevelClient client(){
        return  new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://ip:9200")));
    }

    @PostMapping("/list")
    public PageResult search(@RequestBody RequestParams requestParams) throws IOException {
     return  hotelService.search(requestParams);
    }
}
