package esprit.tn.junglepayments.controller;

import esprit.tn.junglepayments.entities.LoyaltyAccount;
import esprit.tn.junglepayments.entities.PromoCode;
import esprit.tn.junglepayments.services.LoyaltyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments/loyalty")
@CrossOrigin(origins = "http://localhost:4200")
public class LoyaltyController {

    @Autowired
    private LoyaltyService loyaltyService;

    /** GET /api/payments/loyalty/{email} — compte fidélité utilisateur */
    @GetMapping("/{email}")
    public ResponseEntity<LoyaltyAccount> getAccount(@PathVariable String email) {
        LoyaltyAccount account = loyaltyService.getAccount(email);
        if (account == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(account);
    }

    /** GET /api/payments/loyalty/{email}/codes — codes promo personnels de l'utilisateur */
    @GetMapping("/{email}/codes")
    public ResponseEntity<List<PromoCode>> getUserCodes(@PathVariable String email) {
        return ResponseEntity.ok(loyaltyService.getLoyaltyCodesByUser(email));
    }

    /** GET /api/payments/loyalty/codes/all — tous les codes fidélité (admin) */
    @GetMapping("/codes/all")
    public ResponseEntity<List<PromoCode>> getAllLoyaltyCodes() {
        return ResponseEntity.ok(loyaltyService.getAllLoyaltyCodes());
    }
}
