package cn.wcy.hotel.pojo;

import lombok.Data;

/**
 * @author Wcy
 * @Date 2022/4/4 20:32
 */
@Data
public class RequestParams {
    private String key;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String city;
    private String brand;
    private String starName;
    private Integer minPrice;
    private Integer maxPrice;
    private String location;
}
