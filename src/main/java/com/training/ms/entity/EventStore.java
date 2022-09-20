package com.training.ms.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "eventstore")
public class EventStore {

	@Id
	private String eventId;

	private String eventType;

	private Long entityId;
	
	private String eventData;

	private LocalDateTime eventTime;

}
