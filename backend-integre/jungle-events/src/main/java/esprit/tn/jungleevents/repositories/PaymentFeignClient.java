package esprit.tn.jungleevents.repositories;

import esprit.tn.jungleevents.DTO.PaymentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "jungle-payments")
public interface PaymentFeignClient {

    // Appelle l'endpoint de création du microservice Payment
    @PostMapping("/api/payments/create")
    Object createPayment(@RequestBody PaymentDTO paymentData);
}