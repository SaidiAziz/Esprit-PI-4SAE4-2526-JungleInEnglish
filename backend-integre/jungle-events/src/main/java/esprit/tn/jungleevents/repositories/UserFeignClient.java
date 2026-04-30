package esprit.tn.jungleevents.repositories;

import esprit.tn.jungleevents.DTO.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "USER-SERVICE")
public interface UserFeignClient {

    @GetMapping("/users/by-email")
    UserDTO getUserByEmail(@RequestParam("email") String email);
}