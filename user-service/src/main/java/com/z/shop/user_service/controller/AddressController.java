package com.z.shop.user_service.controller;

import com.z.shop.user_service.domain.Address;
import com.z.shop.user_service.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @PostMapping("/add")
    public void add(@RequestParam Long userId,
                    @RequestParam(defaultValue = "false") boolean isDefault,
                    @RequestBody Address address) {
        addressService.addAddress(userId, address, isDefault);
    }

    @GetMapping("/list")
    public List<Address> list(@RequestParam Long userId) {
        return addressService.listUserAddress(userId);
    }
}
