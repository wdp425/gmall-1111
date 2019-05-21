package com.atguigu.gmall.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
public class LoginController {


    @Autowired
    StringRedisTemplate redisTemplate;



    @GetMapping("/login")
    public String login(@RequestParam(value = "redirec_url") String redirec_url,
                        @CookieValue(value = "sso_user",required = false) String ssoUser,
                        HttpServletResponse response,
                        Model model) throws IOException {
        System.out.println("认证中心开始认证.....");
        //1、判断之前是否登录过
        if(!StringUtils.isEmpty(ssoUser)){
            //登录过,回到之前的地方，并且把当前ssoserver获取到的cookie以url方式传递给其他域名【cookie同步】
            String url = redirec_url+"?"+"sso_user="+ssoUser;
            response.sendRedirect(url);
            return null;
        }else {
            //没有登录过
            model.addAttribute("redirec_url",redirec_url);
            return "login";
        }
    }

    @PostMapping("/doLogin")
    public void doLogin(String username, String password,String redirec_url,
                        HttpServletResponse response,
                        HttpServletRequest request) throws IOException {
        //1、模拟用户的信息
        Map<String,Object> map = new HashMap<>();
        map.put("username",username);
        map.put("email",username+"@qq.com");

        //2、以上标识用户登录;
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(token,JSON.toJSONString(map));

        //3、登录成功。做两件事。
        //1）、命令浏览器把当前的token保存为cookie；  sso_user=token
        Cookie cookie = new Cookie("sso_user",token);
        response.addCookie(cookie);
        response.sendRedirect(redirec_url+"?"+"sso_user="+token);


        //2）、命令浏览器重定向到他之前的位置；
        //StringBuffer requestURL = request.getRequestURL();
        //System.out.println("将要去的地方是："+requestURL.toString());




    }
}
