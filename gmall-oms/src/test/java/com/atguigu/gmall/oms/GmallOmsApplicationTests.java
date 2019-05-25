package com.atguigu.gmall.oms;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallOmsApplicationTests {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    public void contextLoads() {
        rabbitTemplate.convertAndSend("order-exchange","order","123456");
        System.out.println("。。。。。。。。。。。。。。。。。");
    }

}
