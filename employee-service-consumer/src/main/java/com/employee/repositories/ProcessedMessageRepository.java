package com.employee.repositories;

import com.employee.entities.ProcessedMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProcessedMessageRepository extends MongoRepository<ProcessedMessage, String> {}
