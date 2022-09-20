package com.training.ms.controller;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.training.ms.CardSwipe;
import com.training.ms.SwipeInEvent;
import com.training.ms.SwipeOutEvent;
import com.training.ms.entity.EventStore;
import com.training.ms.service.EventService;

@RestController
@RequestMapping("/api")
public class AttendanceController {

	@Autowired
	private EventService service;

	@Autowired
	private ObjectMapper mapper;

	public static final String TOPIC = "attendance";

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	@PostMapping("/attendance/in")
	public void swipeIn(@RequestBody CardSwipe swipeInRequest) throws JsonProcessingException {

		SwipeInEvent event = SwipeInEvent.builder().cardSwipeDetails(swipeInRequest).build();
		service.swipeInEvent(event);
	}

	@PostMapping("/attendance/out")
	public void swipeOut(@RequestBody CardSwipe swipeOutRequest) throws JsonProcessingException {

		SwipeOutEvent event = SwipeOutEvent.builder().cardSwipeDetails(swipeOutRequest).build();
		service.swipeOutEvent(event);
	}

	@GetMapping("/attendance")
	public CardSwipe getAttendanceForDay(@RequestParam("date") String date, @RequestParam("id") Long id) {

		String[] dateArray = date.split("-");

		LocalDateTime till = LocalDate
				.of(Integer.parseInt(dateArray[0]), Integer.parseInt(dateArray[1]), Integer.parseInt(dateArray[2]))
				.atTime(23, 59);

		LocalDateTime from = LocalDate
				.of(Integer.parseInt(dateArray[0]), Integer.parseInt(dateArray[1]), Integer.parseInt(dateArray[2]))
				.atTime(00, 00);

		Iterable<EventStore> events = service.fetchAllEventsForDay(id, from, till);

		CardSwipe emp = new CardSwipe();

		emp.setEmployeeId(id);
		emp.setDate(from.toLocalDate());

		LocalDateTime firstSwipeTime = null;
		LocalDateTime lastSwipeOut = null;

		for (EventStore event : events) {

			if (event.getEventType().equals("SWIPE_IN") && firstSwipeTime == null) {

				CardSwipe attendance = new Gson().fromJson(event.getEventData(), CardSwipe.class);
				emp.setName(attendance.getName());
				firstSwipeTime = event.getEventTime();
			} else if (event.getEventType().equals("SWIPE_OUT")) {
				lastSwipeOut = event.getEventTime();
			}

		}

		System.out.println("firstSwipeTime: " + firstSwipeTime);
		System.out.println("lastSwipeOut: " + lastSwipeOut);

		if (firstSwipeTime != null && lastSwipeOut != null) {
			Duration diff = Duration.between(firstSwipeTime, lastSwipeOut);

			System.out.println("Hours: " + diff.toHours());

			long hrs = diff.toHours();
			emp.setAttendance(hrs);

			try {

				System.out.println("Publishing to topic " + TOPIC);
				String json = mapper.writeValueAsString(emp);
				CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
					kafkaTemplate.send(TOPIC, json);
				});

				future.get(10, TimeUnit.SECONDS);
			} catch (JsonProcessingException | InterruptedException | ExecutionException | TimeoutException e) {
				e.printStackTrace();
			}
		}

		return emp;

	}

	@GetMapping("/events")
	public Iterable<EventStore> getEvents(@RequestParam("id") Long id) throws JsonProcessingException {

		Iterable<EventStore> events = service.fetchAllEvents(id);

		return events;

	}

}
