package cz.tul.stin.paveltyl.quoteapi.repository;

// Testujeme skutečné ukládání do repository.
import cz.tul.stin.paveltyl.quoteapi.model.Quote;

// JUnit 5.
import org.junit.jupiter.api.Test;

// Budeme porovnávat výsledky testu.
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class InMemoryQuoteRepositoryTest {

    // 1.
    @Test
    void save_assignsId_andStoresQuote() {

        // DŘÍVE:
        // U service testů jsme používali mock repository.
        // Tedy žádné skutečné ukládání se neprovádělo.

        // NYNÍ:
        // Testujeme skutečnou implementaci InMemoryQuoteRepository.
        // Tedy opravdu vytvoříme objekt repository a opravdu do něj uložíme data.

        // PROČ:
        // Chceme ověřit, že perzistence sama o sobě funguje.
        InMemoryQuoteRepository repository = new InMemoryQuoteRepository();

        // Připravíme nový citát bez id.
        // To simuluje nový objekt, který ještě nebyl uložen.
        Quote quote = new Quote();
        quote.setText("Repository test quote");
        quote.setAuthor("JUnit");

        // Uložíme citát do repository.
        Quote savedQuote = repository.save(quote);

        // Ověříme, že při ukládání bylo přiděleno id.
        // Pokud by id zůstalo null, znamenalo by to, že ukládání nefunguje správně.
        assertNotNull(savedQuote.getId());

        // Ověříme, že se neztratil text citátu.
        assertEquals("Repository test quote", savedQuote.getText());

        // Ověříme, že se neztratil autor citátu.
        assertEquals("JUnit", savedQuote.getAuthor());

        // Ověříme, že repository nyní obsahuje právě dva uložené citáty.
        assertEquals(2, repository.findAll().size()); // jeden citát máme výchozí v konstruktoru repository
    }

    // 2.
    @Test
    void findById_returnsStoredQuote() {

        // DŘÍVE:
        // Otestovali jsme jen to, že save(...) umí objekt uložit a že mu přidělí id.

        // NYNÍ:
        // Chceme ověřit i druhý krok, že uložený objekt umíme podle id znovu najít.

        // PROČ:
        // Samotné uložení nestačí.
        // Repository má také umět data znovu vracet.

        // Vytvoříme skutečnou instanci repository.
        InMemoryQuoteRepository repository = new InMemoryQuoteRepository();

        // Připravíme nový citát, který budeme ukládat.
        Quote quote = new Quote();
        quote.setText("Quote for findById test");
        quote.setAuthor("JUnit");

        // Uložíme citát do repository.
        // Tím zároveň získáme objekt s přiděleným id.
        Quote savedQuote = repository.save(quote);

        // Teď použijeme id uloženého objektu a zkusíme ho z repository znovu načíst.
        Quote foundQuote = repository.findById(savedQuote.getId());

        // Ověříme, že repository něco opravdu našla.
        assertNotNull(foundQuote);

        // Ověříme, že nalezený objekt má stejné id.
        assertEquals(savedQuote.getId(), foundQuote.getId());

        // Ověříme, že se shoduje text.
        assertEquals("Quote for findById test", foundQuote.getText());

        // Ověříme, že se shoduje autor.
        assertEquals("JUnit", foundQuote.getAuthor());
    }

    // 3.
    @Test
    void findById_returnsNull_whenQuoteDoesNotExist() {

        // DŘÍVE:
        // Otestovali jsme úspěšný scénář:
        // uložený citát podle id najdeme.

        // NYNÍ:
        // Testujeme opačný scénář:
        // hledáme id, které v repository vůbec není.

        // PROČ:
        // Repository musí správně fungovat nejen když data existují,
        // ale i když hledaný záznam neexistuje.
        InMemoryQuoteRepository repository = new InMemoryQuoteRepository();

        // Zkusíme hledat id, které jsme nikdy neuložili.
        Quote foundQuote = repository.findById(999L);

        // Očekáváme null.
        // To odpovídá aktuální implementaci repository,
        // která při nenalezení objektu vrací null.
        assertNull(foundQuote);
    }

    // 4.
    @Test
    void findAll_returnsAllStoredQuotes() {

        // DŘÍVE:
        // Testovali jsme u repository hlavně jednotlivé operace:
        // save(...) a findById(...).

        // NYNÍ:
        // Testujeme findAll(),
        // tedy vrácení celého uloženého seznamu.

        // PROČ:
        // Repository nemá umět jen uložit jeden objekt
        // nebo najít jeden objekt podle id,
        // ale také vrátit všechna uložená data najednou.
        InMemoryQuoteRepository repository = new InMemoryQuoteRepository();

        // Připravíme první nový citát.
        Quote firstQuote = new Quote();
        firstQuote.setText("První repository quote");
        firstQuote.setAuthor("Autor 1");

        // Připravíme druhý nový citát.
        Quote secondQuote = new Quote();
        secondQuote.setText("Druhý repository quote");
        secondQuote.setAuthor("Autor 2");

        // Oba citáty skutečně uložíme do repository.
        repository.save(firstQuote);
        repository.save(secondQuote);

        // Zavoláme testovanou metodu.
        List<Quote> result = repository.findAll();

        // Repository má v konstruktoru už jeden výchozí citát.
        // K němu jsme teď přidali další dva.
        // Proto očekáváme celkem 3 položky.
        assertEquals(3, result.size());

        // Ověříme obsah druhé položky.
        // První položka je výchozí citát z konstruktoru repository.
        assertEquals("První repository quote", result.get(1).getText());
        assertEquals("Autor 1", result.get(1).getAuthor());

        // Ověříme obsah třetí položky.
        assertEquals("Druhý repository quote", result.get(2).getText());
        assertEquals("Autor 2", result.get(2).getAuthor());
    }
}
/*
1.
DŘÍVE:
Mock repository jsme jen předstírali přes Mockito.

NYNÍ:
Testujeme skutečnou implementaci repository.

PROČ:
Chceme ověřit, že ukládání opravdu funguje, ne jen že jsme si ho v testu nasimulovali.
 */

/*
2.
DŘÍVE:
Testovali jsme jen zápis do repository.

NYNÍ:
Testujeme i čtení z repository podle id.

PROČ:
Perzistence není jen ukládání.
Musíme umět i uložená data znovu najít.
 */

/*
3.
DŘÍVE:
Testovali jsme jen případ, kdy objekt existuje.

NYNÍ:
Testujeme i případ, kdy objekt neexistuje.

PROČ:
Coverage má pokrýt i "nenalezeno", ne jen úspěšnou cestu.
 */

/*
4.
DŘÍVE:
Testovali jsme jednotlivé uložené objekty.
NYNÍ:
Testujeme vrácení celého seznamu objektů.
PROČ:
Chceme mít pokrytou i metodu, která vrací všechna data najednou.
 */