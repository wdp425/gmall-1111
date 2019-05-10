package com.atguigu.gmall.pms;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.DigestUtils;


/**
 * 1、配置整合dubbo
 * 2、配置整合MyBatisPlus
 *
 * logstash整合，
 * 1）、导jar
 * 2）、导日志配置
 * 3）、在kibana里面建立好日志的索引，就可以可视化检索
 *
 *
 * 1、给内存中存一个值。另外一个类获取出来使用？
 *
 * HelloUtils{
 *     private static Map  map = new HashMap();
 *     {
 *         map.put
 *         map.get
 *     }
 * }
 *
 * 2、请问你们项目怎么联调？
 *      联调？（前后端测写的接口）
 *      接口？（interface）【我们用Controller暴露出来的请求】
 * 3、dubbo原理是什么？
 *      1）、rpc原理：两个不同的服务（不同机器【不同进程】），建立连接，传输数据。
 *      2）、那张图。
 *
 * 0925：
 *     1）、学的很多....
 *     2）、3年经验（核心主流会）。。。  6年经验+（掌握和精通）；
 *          ssm，springboot；ssh；
 *          Spring：设计模式、装饰模式；
 *     3）、学习能力和读文档...
 *
 * 4、联调；
 *      前端Vue，后台开发Server；（接口文档【swagger】）
 *      接口文档：研讨每一个功能的设计（数据库，接口的设计，业务逻辑的设计）；
 *          1）、后台程序员，知道前端需要什么数据，能传来数据数据；
 *          2）、原型；（原型设计师（UE））
 *                 原型：
 *                      1）、UI：设计界面（Android、IOS、Web）
 *                      2）、Server：按照开发功能
 *
 *                 需求分析（设计）---编码---测试---上线
 *         联调发现，前后端设计的问题。
 *
 */
@EnableDubbo
@MapperScan(basePackages = "com.atguigu.gmall.pms.mapper")
@SpringBootApplication
public class GmallPmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallPmsApplication.class, args);
    }

}
