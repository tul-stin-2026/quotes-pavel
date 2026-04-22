package cz.tul.stin.paveltyl.quoteapi.repository;

// Budeme pracovat s modelem Quote.
import cz.tul.stin.paveltyl.quoteapi.model.Quote;

// Repository bude spravovat seznam citátů v paměti.
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

// Tato anotace říká Springu, že jde o vrstvu pro práci s daty.
// Spring tuto třídu vytvoří jako bean.
@Repository
public class InMemoryQuoteRepository implements QuoteRepository {

    // Tady si ukládáme data jen do obyčejného seznamu.
    // To není "skutečná databáze", ale na výuku to stačí.
    private final List<Quote> quotes = new ArrayList<>();

    // Počítadlo pro jednoduché generování id.
    private Long nextId = 1L;

    // Konstruktor se zavolá při vytvoření repository.
    // Můžeme si sem přidat jeden ukázkový citát,
    // aby aplikace po spuštění nebyla úplně prázdná.
    public InMemoryQuoteRepository() {
        Quote firstQuote = new Quote();
        firstQuote.setId(nextId++);
        firstQuote.setText("První uložený citát.");
        firstQuote.setAuthor("Systém");
        quotes.add(firstQuote);
    }

    @Override
    public List<Quote> findAll() {
        // Vracíme celý seznam citátů.
        return quotes;
    }

    @Override
    public Quote save(Quote quote) {
        // Pokud citát ještě nemá id, přidělíme mu nové.
        if (quote.getId() == null) {
            quote.setId(nextId++);
        }

        // Uložíme ho do seznamu.
        quotes.add(quote);

        // Vrátíme uložený objekt.
        return quote;
    }

    @Override
    public Quote findById(Long id) {
        // Projdeme všechny citáty a hledáme shodu podle id.
        for (Quote quote : quotes) {
            if (quote.getId().equals(id)) {
                return quote;
            }
        }

        // Když nic nenajdeme, vrátíme null.
        return null;
    }
}
