package com.atguigu.gmall.ums.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.ums.entity.Member;
import com.atguigu.gmall.ums.mapper.MemberMapper;
import com.atguigu.gmall.ums.service.MemberService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

/**
 * <p>
 * 会员表 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2019-05-08
 */
@Service
@Component
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {


    @Autowired
    MemberMapper memberMapper;

    @Override
    public Member login(String username, String password) {

        String digest = DigestUtils.md5DigestAsHex(password.getBytes());


        Member member = memberMapper.selectOne(new QueryWrapper<Member>()
                .eq("username", username)
                .eq("password", digest)
        );
        return member;
    }
}
