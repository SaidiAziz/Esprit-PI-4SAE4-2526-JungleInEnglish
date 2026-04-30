package esprit.tn.jungleevents.services;

import esprit.tn.jungleevents.entities.Event;
import esprit.tn.jungleevents.entities.EventStatus;
import esprit.tn.jungleevents.repositories.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public Event createEvent(Event event) {
        if(event.getStatus() == null){
            event.setStatus(EventStatus.PUBLISHED);
        }
        return eventRepository.save(event);
    }

    @Override
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Override
    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event introuvable"));
    }

    @Override
    public Event updateEvent(Long id, Event event) {
        Event existing = getEventById(id);

        existing.setTitle(event.getTitle());
        existing.setDescription(event.getDescription());
        existing.setCategory(event.getCategory());
        existing.setLocation(event.getLocation());
        existing.setPrice(event.getPrice());
        existing.setFormat(event.getFormat());
        existing.setStartDate(event.getStartDate());
        existing.setEndDate(event.getEndDate());
        existing.setStatus(event.getStatus());
        if(event.getStatus() != null){
            existing.setStatus(event.getStatus());
        }

        return eventRepository.save(existing);
    }

    @Override
    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }
}
