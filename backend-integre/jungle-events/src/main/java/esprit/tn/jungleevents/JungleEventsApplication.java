package esprit.tn.jungleevents;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class JungleEventsApplication {

	public static void main(String[] args) {
		SpringApplication.run(JungleEventsApplication.class, args);
	}

}
