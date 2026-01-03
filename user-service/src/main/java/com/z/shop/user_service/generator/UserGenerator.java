//package com.z.shop.user_service.generator;
//
//import com.z.shop.user_service.domain.User;
//import com.z.shop.user_service.service.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//@Component
//public class UserGenerator implements CommandLineRunner {
//
//    @Autowired
//    private UserService userService;
//    ExecutorService pool = Executors.newFixedThreadPool(10);
//
//
//    @Override
//    public void run(String... args) {
//        for (int i = 1; i <= 100_000; i++) {
//            final int idx = i;
//            pool.submit(() -> {
//                User user = new User();
//                user.setUsername("user_" + idx);
//                user.setPassword("123456");
//                userService.createUser(user);
//            });
//        }
//        pool.shutdown();
//    }
//}
