package tn.esprit.administrationservice.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tn.esprit.administrationservice.config.StudentMailProperties;
import tn.esprit.administrationservice.entity.OwnerType;
import tn.esprit.administrationservice.entity.TimeSlot;

import java.time.LocalTime;
import java.util.List;

@Service
public class TimeSlotNotificationService {

    private final EmailService emailService;
    private final StudentMailProperties studentMailProperties;

    public TimeSlotNotificationService(EmailService emailService,
                                       StudentMailProperties studentMailProperties) {
        this.emailService = emailService;
        this.studentMailProperties = studentMailProperties;
    }

    @Async
    public void onCreated(TimeSlot slot) {
        System.out.println("🔔 NOTIF create ownerType=" + slot.getOwnerType());

        if (slot.getOwnerType() != OwnerType.PLATFORM) return;

        List<String> emails = studentMailProperties.getEmails();
        System.out.println("📩 Students emails loaded = " + emails);

        sendToAllStudents(
                "[Accent] New time slot added",
                buildBody("added", slot, null)
        );
    }
    @Async
    public void onUpdated(TimeSlot before, TimeSlot after) {
        if (after.getOwnerType() != OwnerType.PLATFORM) return;
        sendToAllStudents(
                "[Accent] Time slot updated",
                buildBody("updated", after, before)
        );
    }

    @Async
    public void onDeleted(TimeSlot slot) {
        if (slot.getOwnerType() != OwnerType.PLATFORM) return;
        sendToAllStudents(
                "[Accent] Time slot removed",
                buildBody("removed", slot, null)
        );
    }

    private void sendToAllStudents(String subject, String body) {
        List<String> emails = studentMailProperties.getEmails();
        if (emails == null || emails.isEmpty()) return;

        for (String email : emails) {
            if (email != null && !email.isBlank()) {
                emailService.send(email.trim(), subject, body);
            }
        }
    }

    private String buildBody(String action, TimeSlot slot, TimeSlot oldSlot) {
        String oldPart = "";
        if (oldSlot != null) {
            oldPart = """
                    
                    Previous:
                    - Day of week: %d
                    - Time: %s - %s
                    """.formatted(oldSlot.getDayOfWeek(), fmt(oldSlot.getStartTime()), fmt(oldSlot.getEndTime()));
        }

        return ("""
                Hello,

                The administration %s a time slot in the planning:

                Current:
                - Day of week: %d
                - Time: %s - %s
                %s
                Regards,
                Accent Platform
                """).formatted(
                action,
                slot.getDayOfWeek(),
                fmt(slot.getStartTime()),
                fmt(slot.getEndTime()),
                oldPart
        );
    }

    private String fmt(LocalTime t) {
        return t == null ? "N/A" : t.toString();
    }
}