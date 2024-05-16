package com.employee.config.feign;

import com.employee.config.ApplicationExceptionHandler;
import com.employee.exceptions.EmployeeServiceConsumerException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;

import java.util.Optional;

public class MessageErrorDecoder implements ErrorDecoder {

    private ErrorDecoder errorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        var errors = response.headers().get(ApplicationExceptionHandler.RESPONSE_ENTITY_ERROR_HEADER);
        Optional<String> error = CollectionUtils.isEmpty(errors) ? Optional.empty() : Optional.of(String.join(",", errors));

        if (HttpStatus.valueOf(response.status()).is4xxClientError()) {
            return new EmployeeServiceConsumerException(HttpStatus.valueOf(response.status()),
                    error.orElse( "Error while calling kafka producer, please try again later."));
        } else {
            return errorDecoder.decode(methodKey, response);
        }
    }
}
