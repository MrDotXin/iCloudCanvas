package com.mrdotxin.icloudcanvas.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

@Configuration
public class Swagger2Config {

    @Bean
    public Docket defaultApi2() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        .title("iCloudCanvas接口文档")
                        .description("mi-icloud-canvas")
                        .version("β1.0")
                        .build())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.mrdotxin.icloudcanvas.controller"))
                .paths(PathSelectors.any())
                .build()
                // 关键：将 Long/long 类型映射为 String
                .directModelSubstitute(Long.class, String.class)
                .directModelSubstitute(long.class, String.class);
    }
}
