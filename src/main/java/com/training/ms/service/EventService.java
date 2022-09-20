package com.training.ms.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.ms.SwipeInEvent;
import com.training.ms.SwipeOutEvent;
import com.training.ms.entity.EventStore;
import com.training.ms.repository.EventRepository;

@Service
public class EventService {

	@Autowired
	private EventRepository repo;

	public void swipeInEvent(SwipeInEvent event) throws JsonProcessingException {

		EventStore eventStore = new EventStore();

		eventStore.setEventData(new ObjectMapper().writeValueAsString(event.getCardSwipeDetails()));

		eventStore.setEventType("SWIPE_IN");

		eventStore.setEntityId(event.getCardSwipeDetails().getEmployeeId());

		eventStore.setEventTime(LocalDateTime.now());
		eventStore.setEventId(LocalDateTime.now().toString());

		repo.save(eventStore);
	}

	public void swipeOutEvent(SwipeOutEvent event) throws JsonProcessingException {

		EventStore eventStore = new EventStore();

		eventStore.setEventData(new ObjectMapper().writeValueAsString(event.getCardSwipeDetails()));

		eventStore.setEventType("SWIPE_OUT");
		eventStore.setEntityId(event.getCardSwipeDetails().getEmployeeId());

		eventStore.setEventTime(LocalDateTime.now());
		eventStore.setEventId(LocalDateTime.now().toString());
		repo.save(eventStore);
	}

	public Iterable<EventStore> fetchAllEvents(Long id) {

		return repo.findByEntityId(id);

	}
	
	public Iterable<EventStore> fetchAllEventsTillDate(Long id,LocalDateTime date) {

		return repo.findByEntityIdAndEventTimeLessThanEqual(id, date);

	}



	public Iterable<EventStore> fetchAllEventsForDay(Long id, LocalDateTime from, LocalDateTime till) {

		return repo.findByEntityIdAndEventTimeBetween(id, from,till);

	}
}
