package cz.tul.stin.paveltyl.quoteapi.controller;

import cz.tul.stin.paveltyl.quoteapi.model.ExternalQuote;
import cz.tul.stin.paveltyl.quoteapi.model.Quote;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController // Anotace - Tato třída je REST controller (přijímá HTTP requesty, vrací JSON)
@RequestMapping("/quotes") // Anotace - Všechny endpointy v této třídě začínají /quotes.
public class QuoteController {

    private final List<Quote> quotes = new ArrayList<>(); // 1
    @Autowired // Anotace - Spring sem má automaticky dodat objekt.
    private RestTemplate restTemplate;

    public QuoteController() {
        quotes.add(new Quote(1L, "Komu se nelení, tomu se zelení.", "Neznámá babička"));
    } // 1

    @GetMapping
    public List<Quote> getQuotes() { // 1

        return quotes;
    }
    // Teď už vracíme JSON – tohle je skutečné REST API.

    @PostMapping // Endpoint pro odesílání dat // 2
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
    @GetMapping("/{id}") // Tahle metoda reaguje na GET request s parametrem v URL.
    public Quote getQuote(@PathVariable Long id) { // Metoda vrací jeden objekt Quote.
                                                   // Hodnota z URL (/quotes/1) se uloží do proměnné id.

        // Najdi v seznamu první citát, který má dané ID. Pokud existuje, vrať ho. Pokud ne, vyhoď chybu.
        return quotes.stream() // Vezmeme seznam a začneme ho zpracovávat jako stream, procházíme zjednodušeně všechny prvky
                .filter(q -> q.getId().equals(id)) // Vybereme jen ty citáty, které mají stejné ID.
                                                         // Pro každý prvek q zkontroluj, jestli jeho ID odpovídá hledanému ID.
                .findFirst()                             // Vezmeme první nalezený výsledek.
                                                         // Buď najde a vrátí hodnotu nebo nenajde a je tam prázdno.
                .orElseThrow();                          // Pokud nic nenajdeme, vyhodíme výjimku.
    }

    @GetMapping("/random")
    public Quote getRandomQuote() {

        String url = "https://zenquotes.io/api/random";

        ExternalQuote[] response =
                restTemplate.getForObject(url, ExternalQuote[].class);

        if (response == null || response.length == 0) {
            throw new RuntimeException("External API returned no data");
        }

        ExternalQuote external = response[0];

        return new Quote(
                null,
                external.getQ(),
                external.getA()
        );
    }

    @PostMapping("/random")
    public Quote saveRandomQuote() {

        Quote quote = getRandomQuote();

        quote.setId((long) (quotes.size() + 1));
        quotes.add(quote);

        return quote;
    }
}
// Teď kombinujeme vlastní logiku, externí API a ukládání dat.