package com.z.shop.user_service.service;

import com.z.shop.common.FastSnowflakeIdGenerator;
import com.z.shop.user_service.domain.User;
import com.z.shop.user_service.mapper.UserMapper;
import com.z.shop.user_service.util.JwtUtil;
import com.z.shop.user_service.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private FastSnowflakeIdGenerator idGenerator;

    public String login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (!PasswordUtil.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new RuntimeException("用户已禁用");
        }

        return jwtUtil.generateToken(user.getId());
    }


    public void createUser(User user) {
        user.setId(idGenerator.nextId());
        user.setStatus(1);
        // 密码加密
        user.setPassword(PasswordUtil.encode(user.getPassword()));
        userMapper.insert(user);
    }
}
