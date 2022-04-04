package cn.wcy.hotel;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("cn.wcy.hotel.mapper")
@SpringBootApplication
public class HotelDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelDemoApplication.class, args);
    }

}
