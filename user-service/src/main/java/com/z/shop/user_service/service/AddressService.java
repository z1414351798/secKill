package com.z.shop.user_service.service;

import com.z.shop.common.FastSnowflakeIdGenerator;
import com.z.shop.user_service.domain.Address;
import com.z.shop.user_service.domain.UserAddress;
import com.z.shop.user_service.mapper.AddressMapper;
import com.z.shop.user_service.mapper.UserAddressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressService {

    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private UserAddressMapper userAddressMapper;
    @Autowired
    private FastSnowflakeIdGenerator idGenerator;

    @Transactional
    public void addAddress(Long userId, Address address, boolean isDefault) {
        address.setId(idGenerator.nextId());
        addressMapper.insert(address);

        if (isDefault) {
            userAddressMapper.clearDefault(userId);
        }

        UserAddress ua = new UserAddress();
        ua.setUserId(userId);
        ua.setAddressId(address.getId());
        ua.setIsDefault(isDefault ? 1 : 0);

        userAddressMapper.insert(ua);
    }

    public List<Address> listUserAddress(Long userId) {
        return userAddressMapper.selectAddressByUser(userId);
    }
}
