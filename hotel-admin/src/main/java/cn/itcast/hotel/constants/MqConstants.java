package cn.itcast.hotel.constants;

/**
 * @author Wcy
 * @Date 2022/4/5 19:32
 */
public class MqConstants {

    public static final String QUEUE_NAME_ORDER = "order";

    public static final String HOTEL_EXCHANGE = "hotel.topic";

    public static final String HOTEL_INSERT_QUEUE = "hotel.insert.queue";

    public static final String HOTEL_DELETE_QUEUE = "hotel.delete.queue";

//    public static final String HOTEL_UPDATE_QUEUE = "hotel.update.queue";

    /**
     * 新增或修改的RoutingKey 可以自定义
     */
    public static final String HOTEL_INSERT_KEY = "hotel.insert";

    public static final String HOTEL_DELETE_KEY = "hotel.delete";

//    public static final String HOTEL_UPDATE_KEY = "hotel.update";


}
