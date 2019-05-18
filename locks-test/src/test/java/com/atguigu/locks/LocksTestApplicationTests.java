package com.atguigu.locks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LocksTestApplicationTests {

    @Autowired
    JedisPool jedisPool;
    @Test
    public void contextLoads() {

        System.out.println(jedisPool);
        Jedis jedis = jedisPool.getResource();
        jedis.set("hello","666");


        System.out.println(jedis.get("hello"));

    }

}
