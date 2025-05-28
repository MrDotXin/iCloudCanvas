package com.mrdotxin.icloudcanvas;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.mrdotxin.icloudcanvas.mapper")
public class ICloudCanvasApplication {

    public static void main(String[] args) {
        SpringApplication.run(ICloudCanvasApplication.class, args);
    }

}
