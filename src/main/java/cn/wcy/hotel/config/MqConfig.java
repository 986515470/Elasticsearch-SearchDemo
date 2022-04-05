package cn.wcy.hotel.config;

import cn.wcy.hotel.constans.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Wcy
 * @Date 2022/4/5 19:37
 */
@Configuration
public class MqConfig {

    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(MqConstants.HOTEL_EXCHANGE,true,false);
    }

    @Bean
    public Queue insertQueue(){
        return new Queue(MqConstants.HOTEL_INSERT_QUEUE,true);
    }

    @Bean
    public Queue DeleteQueue(){
        return new Queue(MqConstants.HOTEL_DELETE_QUEUE,true);
    }



    @Bean
    public Binding insertBinding(){
        return BindingBuilder.bind(
                insertQueue()).to(topicExchange()).with(MqConstants.HOTEL_INSERT_KEY);
    }

    @Bean
    public Binding DeleteBinding(){
        return BindingBuilder.bind(
                DeleteQueue()).to(topicExchange()).with(MqConstants.HOTEL_DELETE_KEY);
    }


}
