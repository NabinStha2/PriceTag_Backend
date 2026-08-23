package com.example.pricetag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PricetagApplication {
    public static void main(String[] args) {
        SpringApplication.run(PricetagApplication.class, args);
    }


    // public void run(String... args) {
    // User adminAccount = userRepo.findByAppUserRole(AppUserRole.ADMIN);
    // if (adminAccount == null) {
    // User user = User
    // .builder()
    // .name("admin")
    // .email("admin@gmail.com")
    // .appUserRole(AppUserRole.ADMIN)
    // .password(new BCryptPasswordEncoder().encode("hello123"))
    // .build();
    // System.out.println(user);
    // userRepo.save(user);
    // }
    // }

}
