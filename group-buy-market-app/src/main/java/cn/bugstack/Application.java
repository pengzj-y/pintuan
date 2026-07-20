package cn.bugstack;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {RabbitAutoConfiguration.class})
@Configurable
@EnableScheduling
public class Application {

    public static void main(String[] args){
        SpringApplication.run(Application.class);
    }

}
