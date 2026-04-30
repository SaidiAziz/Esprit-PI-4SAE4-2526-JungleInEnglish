package com.example.booking.BookingMS;

import com.pusher.pushnotifications.PushNotifications;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class PusherBeamsService {

    private final PushNotifications beamsClient;

    public PusherBeamsService() {
        this.beamsClient = new PushNotifications(
                "de0b5920-5b65-4a1f-8e2f-49038d5f5918",
                "9748C3AF6BA3FBAE9CE215FED3FB69A4C4EB53386A62D0C3E2446E0275BA1393"
        );
    }

    public void notifyTutor(Long tutorId, String title, String body) {
        try {
            // L'intérêt est "tutor-{id}" pour cibler un tuteur spécifique
            String interest = "tutor-" + tutorId;

            Map<String, Map<String, String>> publishRequest = new HashMap<>();

            Map<String, String> webNotification = new HashMap<>();
            webNotification.put("title", title);
            webNotification.put("body", body);

            Map<String, String> web = new HashMap<>();
            web.put("title", title);
            web.put("body", body);

            publishRequest.put("web", web);

            beamsClient.publishToInterests(
                    Collections.singletonList(interest),
                    publishRequest
            );

            System.out.println("[Pusher] Notification sent to interest: " + interest);

        } catch (Exception e) {
            System.err.println("[Pusher] Failed to send notification: " + e.getMessage());
        }
    }
}