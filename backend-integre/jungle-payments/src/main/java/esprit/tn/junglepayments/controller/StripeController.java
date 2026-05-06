package esprit.tn.junglepayments.controller;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import esprit.tn.junglepayments.DTO.PaymentIntentRequest;
import esprit.tn.junglepayments.DTO.PaymentIntentResponse;
import esprit.tn.junglepayments.DTO.PromoCodeValidationRequest;
import esprit.tn.junglepayments.DTO.PromoCodeValidationResponse;
import esprit.tn.junglepayments.services.PromoCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "http://localhost:4200")
public class StripeController {

    @Value("${stripe.publishable-key}")
    private String publishableKey;

    @Autowired
    private PromoCodeService promoCodeService;

    @PostMapping("/validate-promo")
    public ResponseEntity<PromoCodeValidationResponse> validatePromoCode(
            @RequestBody PromoCodeValidationRequest request) {
        PromoCodeValidationResponse response = promoCodeService.validatePromoCode(request);
        if (!response.isValid()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, response.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    // ✅ Étape 1 : Angular appelle cet endpoint pour créer un PaymentIntent
    @PostMapping("/create-intent")
    public ResponseEntity<PaymentIntentResponse> createIntent(
            @RequestBody PaymentIntentRequest request) throws StripeException {

        Double amount = request.getAmount() != null ? request.getAmount() : 0.0;
        Double discount = 0.0;
        Double total = amount;

        System.out.println("Received promoCode: " + request.getPromoCode());
        System.out.println("Received eventId: " + request.getEventId());

        if (request.getPromoCode() != null && !request.getPromoCode().isBlank()) {
            PromoCodeValidationRequest promoRequest = new PromoCodeValidationRequest();
            promoRequest.setPromoCode(request.getPromoCode());
            promoRequest.setEventId(request.getEventId());
            promoRequest.setAmount(amount);

            System.out.println("Validating promo code: " + request.getPromoCode() + " for event: " + request.getEventId());

            PromoCodeValidationResponse promoResponse = promoCodeService.validatePromoCode(promoRequest);
            if (!promoResponse.isValid()) {
                System.out.println("Promo validation failed: " + promoResponse.getMessage());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, promoResponse.getMessage());
            }

            discount = promoResponse.getDiscountAmount();
            total = promoResponse.getFinalAmount();
            System.out.println("Promo applied: discount=" + discount + ", total=" + total);
            promoCodeService.incrementUses(request.getPromoCode());
        }

        long amountInCents = Math.round(total * 100);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("usd") // ⚠️ Stripe TEST ne supporte pas TND, on utilise USD
                .setDescription("Paiement événement : " + request.getEventTitle())
                .putMetadata("eventId", String.valueOf(request.getEventId()))
                .putMetadata("userEmail", request.getUserEmail())
                .putMetadata("promoCode", request.getPromoCode() == null ? "" : request.getPromoCode())
                .putMetadata("discountAmount", String.valueOf(discount))
                .putMetadata("originalAmount", String.valueOf(amount))
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

        return ResponseEntity.ok(new PaymentIntentResponse(
                intent.getClientSecret(),
                publishableKey,
                intent.getId()
        ));
    }
}