package esprit.tn.jungleevents.services;

import esprit.tn.jungleevents.entities.Event;

import java.util.List;

public interface EventService {
    Event createEvent(Event event);

    List<Event> getAllEvents();

    Event getEventById(Long id);

    Event updateEvent(Long id, Event event);

    void deleteEvent(Long id);

}
