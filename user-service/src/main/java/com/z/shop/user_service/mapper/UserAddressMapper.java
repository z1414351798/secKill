package com.z.shop.user_service.mapper;

import com.z.shop.user_service.domain.Address;
import com.z.shop.user_service.domain.UserAddress;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

public interface UserAddressMapper {

    int insert(UserAddress userAddress);

    int clearDefault(Long userId);

    int setDefault(Long userId, Long addressId);

    List<Address> selectAddressByUser(Long userId);
}
