package cn.wcy.hotel.service;

import cn.wcy.hotel.pojo.Hotel;
import cn.wcy.hotel.pojo.PageResult;
import cn.wcy.hotel.pojo.RequestParams;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.IOException;

public interface IHotelService extends IService<Hotel> {


    PageResult search(RequestParams requestParams) throws IOException;
}
