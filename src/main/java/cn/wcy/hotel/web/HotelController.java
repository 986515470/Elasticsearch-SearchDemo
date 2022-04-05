package cn.wcy.hotel.web;

import cn.wcy.hotel.pojo.PageResult;
import cn.wcy.hotel.pojo.RequestParams;
import cn.wcy.hotel.service.IHotelService;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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
                HttpHost.create("http://yourIp:9200")));
    }

    @PostMapping("/list")
    public PageResult search(@RequestBody RequestParams requestParams) throws IOException {
     return  hotelService.search(requestParams);
    }

    @PostMapping("/filters")
    public Map<String, List<String>> getFilters(@RequestBody RequestParams requestParams) throws IOException {
        return hotelService.filters(requestParams);
    }

    @GetMapping("/suggestion")
    public List<String> getSuggestions(@RequestParam("key") String key) throws IOException {
        return hotelService.suggestions(key);
    }
}
