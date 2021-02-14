package ru.kpfu.itis.hbaseloader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class HbaseLoaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(HbaseLoaderApplication.class, args);
    }

}
