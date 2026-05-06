package esprit.tn.junglepayments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class JunglePaymentsApplication {

    public static void main(String[] args) {
        SpringApplication.run(JunglePaymentsApplication.class, args);
    }

}
