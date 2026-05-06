package tn.esprit.administrationservice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.administrationservice.dto.TimeSlotRequest;
import tn.esprit.administrationservice.entity.OwnerType;
import tn.esprit.administrationservice.entity.TimeSlot;
import tn.esprit.administrationservice.exception.BadRequestException;
import tn.esprit.administrationservice.repository.TimeSlotRepository;

import java.util.List;

@Service
public class TimeSlotService {
    private final TimeSlotRepository repo;
    private final TimeSlotNotificationService notificationService;

    public TimeSlotService(TimeSlotRepository repo,
                           TimeSlotNotificationService notificationService) {
        this.repo = repo;
        this.notificationService = notificationService;
    }

    public List<TimeSlot> getAll() {
        return repo.findAll();
    }

    public TimeSlot getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new BadRequestException("TimeSlot not found: " + id));
    }

    public List<TimeSlot> getByOwner(OwnerType ownerType, Long ownerId) {
        if (ownerType == OwnerType.PLATFORM) return repo.findByOwnerType(OwnerType.PLATFORM);
        if (ownerId == null) throw new BadRequestException("ownerId is required when ownerType=TUTOR");
        return repo.findByOwnerTypeAndOwnerId(ownerType, ownerId);
    }

    @Transactional
    public TimeSlot create(TimeSlotRequest r) {
        validate(r);
        TimeSlot slot = TimeSlot.builder()
                .ownerType(r.getOwnerType())
                .ownerId(r.getOwnerType() == OwnerType.PLATFORM ? null : r.getOwnerId())
                .dayOfWeek(r.getDayOfWeek())
                .startTime(r.getStartTime())
                .endTime(r.getEndTime())
                .validFrom(r.getValidFrom())
                .validTo(r.getValidTo())
                .active(r.getActive())
                .build();

        TimeSlot saved = repo.save(slot);

        // ✅ Notify only if PLATFORM (the service itself checks too)
        notificationService.onCreated(saved);

        return saved;
    }

    @Transactional
    public TimeSlot update(Long id, TimeSlotRequest r) {
        validate(r);
        TimeSlot slot = repo.findById(id).orElseThrow(() -> new BadRequestException("TimeSlot not found: " + id));

        // snapshot before changes (for email "previous")
        TimeSlot before = TimeSlot.builder()
                .ownerType(slot.getOwnerType())
                .ownerId(slot.getOwnerId())
                .dayOfWeek(slot.getDayOfWeek())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .validFrom(slot.getValidFrom())
                .validTo(slot.getValidTo())
                .build();

        slot.setOwnerType(r.getOwnerType());
        slot.setOwnerId(r.getOwnerType() == OwnerType.PLATFORM ? null : r.getOwnerId());
        slot.setDayOfWeek(r.getDayOfWeek());
        slot.setStartTime(r.getStartTime());
        slot.setEndTime(r.getEndTime());
        slot.setValidFrom(r.getValidFrom());
        slot.setValidTo(r.getValidTo());
        slot.setActive(r.getActive());

        TimeSlot saved = repo.save(slot);

        notificationService.onUpdated(before, saved);

        return saved;
    }

    @Transactional
    public void delete(Long id) {
        TimeSlot slot = repo.findById(id).orElseThrow(() -> new BadRequestException("TimeSlot not found: " + id));
        repo.delete(slot);

        notificationService.onDeleted(slot);
    }

    private void validate(TimeSlotRequest r) {
        if (!r.getStartTime().isBefore(r.getEndTime()))
            throw new BadRequestException("startTime must be before endTime");

        if (r.getOwnerType() == OwnerType.TUTOR && r.getOwnerId() == null)
            throw new BadRequestException("ownerId is required when ownerType=TUTOR");

        if (r.getValidFrom() != null && r.getValidTo() != null && r.getValidFrom().isAfter(r.getValidTo()))
            throw new BadRequestException("validFrom must be <= validTo");
    }
}