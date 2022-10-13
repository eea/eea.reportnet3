package org.eea.dataset.axon.error;

import org.eea.exception.EEAException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class AdviceController {

    @ExceptionHandler(value= {EEAException.class})
    public ResponseEntity<Object> handleIllegalStateException(IllegalStateException ex, WebRequest request) {

        return new ResponseEntity<>("test", new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
