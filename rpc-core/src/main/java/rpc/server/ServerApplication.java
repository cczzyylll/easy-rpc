package rpc.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("rpc.impl")

public class ServerApplication {
    public static void main(String[] args){
        SpringApplication.run(ServerApplication.class);
    }
}
