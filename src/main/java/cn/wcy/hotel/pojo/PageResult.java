package cn.wcy.hotel.pojo;

import lombok.Data;

import java.util.List;

/**
 * @author Wcy
 * @Date 2022/4/4 20:35
 */
@Data
public class PageResult {
    private long total;
    private List<HotelDoc> hotels;

    public PageResult() {
    }

    public PageResult(long total, List<HotelDoc> hotels) {
        this.total = total;
        this.hotels = hotels;
    }
}
