package org.game.throne.web;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

/**
 * Created by lvtu on 2017/7/29.
 */
@SpringBootApplication
@MapperScan(basePackages = "org.game.throne.web", annotationClass = Mapper.class)
@ComponentScan(basePackages = "org.game.throne.web")
@ImportResource(value = "classpath:spring-all.xml")
//@EnableCaching
public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
