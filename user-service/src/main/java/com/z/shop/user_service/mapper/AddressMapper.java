package com.z.shop.user_service.mapper;

import com.z.shop.user_service.domain.Address;
import org.apache.ibatis.annotations.Mapper;

public interface AddressMapper {

    int insert(Address address);

    Address selectById(Long id);

    int update(Address address);

    int deleteById(Long id);
}
