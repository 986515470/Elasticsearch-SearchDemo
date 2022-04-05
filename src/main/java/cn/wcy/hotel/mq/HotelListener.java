package cn.wcy.hotel.mq;

import cn.wcy.hotel.constans.MqConstants;
import cn.wcy.hotel.service.IHotelService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Wcy
 * @Date 2022/4/5 20:07
 */
@Component
public class HotelListener {


    @Resource
    private IHotelService hotelService;
    /**
     * 接收酒店消息
     * @param id
     */
    @RabbitListener(queues = MqConstants.HOTEL_INSERT_QUEUE)
    public void ListenHotelInsertOrUpdate(Long id) {
        hotelService.insertById(id);
    }

    /**
     * 接收删除酒店消息
     * @param id
     */
    @RabbitListener(queues = MqConstants.HOTEL_DELETE_QUEUE)
    public void ListenHotelDelete(Long id) {
        hotelService.deleteById(id);
    }
}
