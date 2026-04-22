package cz.tul.stin.paveltyl.quoteapi.repository;

// Importujeme model Quote, protože repository bude pracovat s citáty.
import cz.tul.stin.paveltyl.quoteapi.model.Quote;

// List budeme vracet při načtení všech citátů.
import java.util.List;

// Toto je rozhraní.
// Rozhraní říká, jaké operace chceme umět,
// ale zatím neřeší, jak přesně jsou uvnitř implementované.
public interface QuoteRepository {

    // Vrátí všechny uložené citáty.
    List<Quote> findAll();

    // Uloží jeden citát a vrátí ho zpět.
    Quote save(Quote quote);

    // Najde citát podle id.
    // Vracíme přímo Quote nebo null?
    // Zatím to necháme jednoduché a vrátíme přímo Quote.
    // Později bychom mohli použít Optional<Quote>.
    Quote findById(Long id);
}
