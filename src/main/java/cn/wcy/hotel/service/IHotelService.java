package cn.wcy.hotel.service;

import cn.wcy.hotel.pojo.Hotel;
import cn.wcy.hotel.pojo.PageResult;
import cn.wcy.hotel.pojo.RequestParams;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IHotelService extends IService<Hotel> {


    PageResult search(RequestParams requestParams) throws IOException;

    Map<String, List<String>> filters(RequestParams params) throws IOException;

    List<String> suggestions(String key);

    void deleteById(Long id);

    void insertById(Long id);
}
