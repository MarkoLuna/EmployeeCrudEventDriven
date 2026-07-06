package com.employee.repositories;

import com.employee.entities.DeadLetterMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DeadLetterRepository extends MongoRepository<DeadLetterMessage, String> {}
