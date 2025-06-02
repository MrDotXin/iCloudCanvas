package com.mrdotxin.icloudcanvas;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.mrdotxin.icloudcanvas.mapper")
public class ICloudCanvasApplication {

    public static void main(String[] args) {
        SpringApplication.run(ICloudCanvasApplication.class, args);
    }

}
