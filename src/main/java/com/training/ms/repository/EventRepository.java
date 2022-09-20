package com.training.ms.repository;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.training.ms.entity.EventStore;

@Repository
public interface EventRepository extends MongoRepository<EventStore, Long> {

	Iterable<EventStore> findByEntityId(Long id);

	Iterable<EventStore> findByEntityIdAndEventTimeLessThanEqual(Long id, LocalDateTime date);

	Iterable<EventStore> findByEntityIdAndEventTimeBetween(Long id,
			LocalDateTime fromm, LocalDateTime till);
}
