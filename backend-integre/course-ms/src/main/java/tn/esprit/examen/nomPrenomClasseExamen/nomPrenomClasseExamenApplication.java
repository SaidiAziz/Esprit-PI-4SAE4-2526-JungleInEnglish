package tn.esprit.examen.nomPrenomClasseExamen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAspectJAutoProxy
@EnableScheduling
@EnableDiscoveryClient
@SpringBootApplication
public class nomPrenomClasseExamenApplication {

    public static void main(String[] args) {
        SpringApplication.run(nomPrenomClasseExamenApplication.class, args);
    }

}
