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
    // Teď už vracíme JSON – tohle je skutečné REST API.

    @PostMapping // endpoint pro odesílání dat
    public Quote addQuote(@RequestBody Quote quote) { // @RequestBody - Spring vezme JSON a převede ho na objekt Quote

        // jednoduché generování ID
        quote.setId((long) (quotes.size() + 1));

        quotes.add(quote);

        return quote;
    }
    // Teď budeme data nejen číst, ale i posílat do serveru.

    // Parametr v URL (path variable)
    // Teď nechceme všechno, ale jen jeden konkrétní záznam.
    // {id} - část URL, která se mění
    // @PathVariable - vezme hodnotu z URL a předá ji do metody
    @GetMapping("/{id}")
    public Quote getQuote(@PathVariable Long id) {

        return quotes.stream()
                .filter(q -> q.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }
}