package tn.esprit.administrationservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.administrationservice.entity.OwnerType;
import tn.esprit.administrationservice.entity.TimeSlot;

import java.util.List;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    List<TimeSlot> findByOwnerTypeAndOwnerId(OwnerType ownerType, Long ownerId);
    List<TimeSlot> findByOwnerType(OwnerType ownerType);
}
