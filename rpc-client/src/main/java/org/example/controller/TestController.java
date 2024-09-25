package org.example.controller;

import org.example.common.RpcReference;
import org.example.interfaces.TestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @RpcReference
    private TestService testService;
    @GetMapping("/test")
    public String test(){
        return testService.test();
    }
}
