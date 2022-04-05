package cn.wcy.hotel.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
public class HotelDoc {
    private Long id;
    private String name;
    private String address;
    private Integer price;
    private Integer score;
    private String brand;
    private String city;
    private String starName;
    private String business;
    private String location;
    private String pic;
    private Object distance;
    private Boolean isAD;
    private List<String> suggestion;

    public HotelDoc(Hotel hotel) {
        this.id = hotel.getId();
        this.name = hotel.getName();
        this.address = hotel.getAddress();
        this.price = hotel.getPrice();
        this.score = hotel.getScore();
        this.brand = hotel.getBrand();
        this.city = hotel.getCity();
        this.starName = hotel.getStarName();
        this.business = hotel.getBusiness();
        this.location = hotel.getLatitude() + ", " + hotel.getLongitude();
        this.pic = hotel.getPic();
        if (this.business.contains("/")||this.business.contains("、")) {
//有多个 需要切割
            String[] split;
            if (this.business.contains("/")){
                 split = this.business.split("/");
            }
            else{
                  split = this.business.split("、");
            }
            //添加元素
            this.suggestion=new ArrayList<>();
            this.suggestion.add(this.brand);
            Collections.addAll(this.suggestion,split);
        } else {
            this.suggestion = Arrays.asList(this.brand, this.business);
        }
    }


}
