package com.yousells;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@MapperScan(basePackages = "com.yousells.modules", markerInterface = BaseMapper.class)
public class YousellsApplication {

    public static void main(String[] args) {
        SpringApplication.run(YousellsApplication.class, args);
    }
}
