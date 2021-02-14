package ru.kpfu.itis.hbaseloader.proxy;

import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.URI;

@FeignClient(
        name = "coordinator",
        url = "${application.config.coordinator-url}"
)
public interface CoordinatorProxy {
    @GetMapping
    @RequestLine("GET")
    String coordinate(URI uri);
}
