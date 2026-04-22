package cz.tul.stin.paveltyl.quoteapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

// DŘÍVE:
// Když service vyhodila NoSuchElementException,
// Spring ji v testu nechal propadnout ven jako ServletException.
//
// NYNÍ:
// Přidáváme centrální zpracování výjimek pro REST controllery.
//
// PROČ:
// Chceme místo nehezkého pádu vracet srozumitelnou HTTP odpověď.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Tento handler zachytí NoSuchElementException odkudkoli z controller vrstvy.
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException ex) {

        // Vytvoříme jednoduché tělo odpovědi s textem chyby.
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());

        // Vrátíme HTTP 404 Not Found + JSON s message.
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }
}