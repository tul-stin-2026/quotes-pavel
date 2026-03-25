package cz.tul.stin.paveltyl.quoteapi.controller;

import cz.tul.stin.paveltyl.quoteapi.model.Quote;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/quotes")
public class QuoteController {

    private final List<Quote> quotes = new ArrayList<>();

    public QuoteController() {
        quotes.add(new Quote(1L, "Komu se nelení, tomu se zelení", "Unknown Grandma"));
    }

    @GetMapping
    public List<Quote> getQuotes() {
        return quotes;
    }
}

// Teď už vracíme JSON – tohle je skutečné REST API.