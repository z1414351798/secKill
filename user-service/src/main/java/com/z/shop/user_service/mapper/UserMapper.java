package com.z.shop.user_service.mapper;

import com.z.shop.user_service.domain.User;
import org.apache.ibatis.annotations.Mapper;

public interface UserMapper {

    User selectByUsername(String username);

    User selectById(Long id);

    int insert(User user);

    int update(User user);

    int deleteById(Long id);
}
