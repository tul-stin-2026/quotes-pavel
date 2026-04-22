package cz.tul.stin.paveltyl.quoteapi.controller;

import cz.tul.stin.paveltyl.quoteapi.model.Quote;
import cz.tul.stin.paveltyl.quoteapi.service.QuoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Anotace - Tato třída je REST controller (přijímá HTTP requesty, vrací JSON)
@RequestMapping("/quotes") // Anotace - Všechny endpointy v této třídě začínají /quotes.
@Slf4j
public class QuoteController {

    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    } // 1

    @GetMapping
    public List<Quote> getQuotes() { // 1
        return quoteService.getQuotes();
    }
    // Teď už vracíme JSON – tohle je skutečné REST API.

    @PostMapping // Endpoint pro odesílání dat // 2
    public Quote addQuote(@RequestBody Quote quote) { // @RequestBody - Spring vezme JSON a převede ho na objekt Quote
        return quoteService.addQuote(quote);
    }
    // Teď budeme data nejen číst, ale i posílat na server.

    // Parametr v URL (path variable)
    // Teď nechceme všechno, ale jen jeden konkrétní záznam.
    // {id} - část URL, která se mění
    // @PathVariable - vezme hodnotu z URL a předá ji do metody
    @GetMapping("/{id}") // Tahle metoda reaguje na GET request s parametrem v URL.
    public Quote getQuote(@PathVariable Long id) { // Metoda vrací jeden objekt Quote.
                                                   // Hodnota z URL (/quotes/1) se uloží do proměnné id.
        return quoteService.getQuote(id);
    }

    @GetMapping("/random")
    public Quote getRandomQuote() {
        return quoteService.getRandomQuote();
    }

    @PostMapping("/random")
    public Quote saveRandomQuote() {
        return quoteService.saveRandomQuote();
    }
}
// Teď kombinujeme vlastní logiku, externí API a ukládání dat.