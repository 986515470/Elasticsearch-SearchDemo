package cn.wcy.hotel;

import cn.wcy.hotel.pojo.RequestParams;
import cn.wcy.hotel.service.impl.HotelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@SpringBootTest
class HotelDemoApplicationTests {

    @Test
    void contextLoads() throws IOException {

    }

    @Autowired
    private HotelService hotelService;




}
