package exp.collaborationroommicroservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CollaborationRoomMicroserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CollaborationRoomMicroserviceApplication.class, args);
    }
}
