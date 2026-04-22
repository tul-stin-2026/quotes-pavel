package cz.tul.stin.paveltyl.quoteapi.service;

import cz.tul.stin.paveltyl.quoteapi.model.ExternalQuote;
import cz.tul.stin.paveltyl.quoteapi.model.Quote;

// NOVĚ: importujeme repository, protože service už nebude pracovat sama,
// ale využije vrstvu pro ukládání dat.
import cz.tul.stin.paveltyl.quoteapi.repository.QuoteRepository;

import lombok.Getter;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class QuoteService {

    private static final String RANDOM_QUOTE_URL = "https://zenquotes.io/api/random";

    // @Getter
    // private final List<Quote> quotes = new ArrayList<>();
    private final RestOperations restOperations;

    // NOVĚ:
    // Druhá závislost slouží pro práci s uloženými citáty.
    private final QuoteRepository quoteRepository;

    // Konstruktor injection: Spring při vytvoření service dodá obě potřebné závislosti.
    public QuoteService(RestOperations restOperations, QuoteRepository quoteRepository) {
        this.restOperations = restOperations;
        this.quoteRepository = quoteRepository;
        // quotes.add(new Quote(1L, "Komu se nelení, tomu se zelení.", "Neznámá babička"));
    }

    /*
    public Quote addQuote(Quote quote) {
        // jednoduché generování ID
        quote.setId((long) (quotes.size() + 1));
        quotes.add(quote);
        return quote;
    }
     */

    public List<Quote> getQuotes() {

        // DŘÍVE:
        // Service vracela svůj vlastní interní seznam quotes.

        // NYNÍ:
        // Service si o celý seznam řekne repository.

        // PROČ:
        // Po refaktoru chceme mít čtení i zápis dat přes repository, ne přes starý interní seznam.
        return quoteRepository.findAll();
    }

    public Quote addQuote(Quote quote) {

        // DŘÍVE:
        // Service sama přidělovala id a sama ukládala citát do interního seznamu quotes.

        // NYNÍ:
        // Service už nebude sama řešit ukládání.
        // Předá objekt Quote do repository a repository rozhodne, jak se uložení provede.

        // PROČ:
        // Chceme oddělit logiku service od práce s daty.
        return quoteRepository.save(quote);
    }

    public Quote getRandomQuote() {
        ExternalQuote[] response =
                restOperations.getForObject(RANDOM_QUOTE_URL, ExternalQuote[].class);

        if (response == null || response.length == 0) {
            throw new RuntimeException("External API returned no data.");
        }

        ExternalQuote external = response[0];

        return new Quote(null, external.getQ(), external.getA());
    }

    public Quote saveRandomQuote() {
        // KROK 1:
        // Nejprve získáme náhodný citát z externího API.
        // Tato část už je vyřešená v metodě getRandomQuote().
        Quote quote = getRandomQuote();

        // KROK 2:
        // NYNÍ už citát neukládáme přímo přes addQuote(...), ale předáme ho repository.
        // Service tím říká jen "ulož tento citát", ale neřeší, JAK přesně se ukládání provádí.
        // Delegujeme ukládání na QuoteRepository a tím oddělíme logiku service od ukládání dat.
        return quoteRepository.save(quote);
        // return addQuote(getRandomQuote());
    }

    /*
    public Quote getQuote(Long id) {

        // Najdi v seznamu první citát, který má dané ID. Pokud existuje, vrať ho. Pokud ne, vyhoď chybu.
        return quotes.stream() // Vezmeme seznam a začneme ho zpracovávat jako stream, procházíme zjednodušeně všechny prvky
                .filter(q -> q.getId().equals(id)) // Vybereme jen ty citáty, které mají stejné ID.
                // Pro každý prvek q zkontroluj, jestli jeho ID odpovídá hledanému ID.
                .findFirst()                             // Vezmeme první nalezený výsledek.
                // Buď najde a vrátí hodnotu nebo nenajde a je tam prázdno.
                .orElseThrow(() -> new NoSuchElementException("Quote with id " + id + " not found."));                          // Pokud nic nenajdeme, vyhodíme výjimku.
    }
     */

    public Quote getQuote(Long id) {

        // DŘÍVE:
        // Service sama procházela interní seznam quotes a sama hledala odpovídající objekt.

        // NYNÍ:
        // Service deleguje hledání na repository.

        // PROČ:
        // Vyhledávání dat patří do repository vrstvy, ne přímo do service.
        Quote quote = quoteRepository.findById(id);

        // Repository v aktuální jednoduché implementaci vrací null, když objekt nenajde.
        // Proto zde stále převádíme "nenalezeno" na výjimku, kterou očekává zbytek aplikace.
        if (quote == null) {
            throw new NoSuchElementException("Quote with id " + id + " not found.");
        }

        return quote;
    }
}
/*
DŘÍVE
Service uměla získat random quote, ale neuměla ho ukládat přes samostatnou vrstvu.
NYNÍ
Service používá další závislost: QuoteRepository.
PROČ
Díky tomu:
    - service neřeší detaily ukládání
    - ukládání můžeme později snadno mockovat v testu
*/
