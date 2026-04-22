package cz.tul.stin.paveltyl.quoteapi.service;

// Importujeme model ExternalQuote, protože si v testu vytvoříme falešnou odpověď z externího API.
import cz.tul.stin.paveltyl.quoteapi.model.ExternalQuote;

// Importujeme model Quote, protože testovaná metoda vrací právě Quote.
import cz.tul.stin.paveltyl.quoteapi.model.Quote;

// JUnit anotace @Test označuje testovací metodu.
import cz.tul.stin.paveltyl.quoteapi.repository.QuoteRepository;
import org.junit.jupiter.api.Test;

// Tohle propojí JUnit 5 a Mockito.
// Díky tomu budou fungovat anotace @Mock a @InjectMocks.
import org.junit.jupiter.api.extension.ExtendWith;

// MockitoExtension je rozšíření pro JUnit 5.
// Stará se o vytvoření mocků před spuštěním testu.
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

// @Mock vytvoří falešný objekt místo skutečné závislosti.
import org.mockito.Mock;

// @InjectMocks vytvoří testovanou třídu a vloží do ní mocky.
import org.mockito.InjectMocks;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

// RestOperations je závislost QuoteService, kterou budeme mockovat.
import org.springframework.web.client.RestOperations;

// Statické importy zjednoduší zápis v testu.
// assertEquals porovná očekávanou a skutečnou hodnotu.
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// when a eq jsou z Mockito.
// when(...).thenReturn(...) říká, co má mock vrátit.
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

// Tím říkáme: tento test bude používat Mockito v rámci JUnit 5.
@ExtendWith(MockitoExtension.class)
class QuoteServiceTest {

    // Tohle je mock závislosti.
    // Nevznikne skutečný RestTemplate / RestOperations, ale falešný objekt.
    @Mock
    private RestOperations restOperations;

    // NOVĚ:
    // Tohle je druhá mockovaná závislost.
    // Místo skutečného repository použijeme falešný objekt.
    @Mock
    private QuoteRepository quoteRepository;

    // Tohle je testovaná třída.
    // Mockito ji vytvoří a do konstruktoru jí vloží mock restOperations.
    @InjectMocks
    private QuoteService quoteService;

    // 1. ukázkový test.
    @Test
    void getRandomQuote_returnsMappedQuote() {
        // Připravíme si falešný objekt, který bude simulovat odpověď z externího API.
        ExternalQuote externalQuote = new ExternalQuote();
        // Nastavíme text citátu.
        externalQuote.setQ("Tohle je testovací citát.");
        // Nastavíme autora citátu.
        externalQuote.setA("Testovací autor");

        // Tady nastavujeme chování mocku.
        // Když někdo zavolá getForObject s touto URL a s typem ExternalQuote[].class,
        // tak mock nevolá internet, ale vrátí pole s jedním objektem externalQuote.
        when(restOperations.getForObject(
                eq("https://zenquotes.io/api/random"),
                eq(ExternalQuote[].class)
        )).thenReturn(new ExternalQuote[]{externalQuote});

        // Tady skutečně voláme metodu, kterou chceme otestovat.
        Quote result = quoteService.getRandomQuote();

        // 4. tady neověřujeme výsledek, ale chování.
        // Říkáme, že se na mocku restOperations zavolala metoda getForObject se stejnou URL a stejným typem.
        // Ověř, že na mocku restOperations byla opravdu zavolána metoda getForObject(...) s touto URL a s tímto typem.
        verify(restOperations).getForObject(
                eq("https://zenquotes.io/api/random"),
                eq(ExternalQuote[].class)
        );

        // Ověříme, že text v převedeném Quote odpovídá textu z ExternalQuote.
        assertEquals("Tohle je testovací citát.", result.getText());
        // Ověříme, že autor v převedeném Quote odpovídá autorovi z ExternalQuote.
        assertEquals("Testovací autor", result.getAuthor());

        // Ověříme, že ID je null.
        // To je správně, protože getRandomQuote pouze vytvoří nový objekt,
        // ale ještě ho neukládá do seznamu.
        assertNull(result.getId()); // assertEquals(null, result.getId());
    }

    // 2. netestujeme jen šťastnou cestu, ale i to, co se stane, když externí API vrátí špatná data
    @Test
    void getRandomQuote_throwsExceptionWhenApiReturnsEmptyArray() {
        // Tady nastavíme chování mocku pro chybový stav.
        // Říkáme: když se zavolá getForObject se stejnou URL a stejným typem, mock vrátí prázdné pole.
        when(restOperations.getForObject(
                eq("https://zenquotes.io/api/random"),
                eq(ExternalQuote[].class)
        )).thenReturn(new ExternalQuote[0]);

        // assertThrows slouží k testování výjimky.
        // Očekáváme, že metoda getRandomQuote() vyhodí RuntimeException.
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> quoteService.getRandomQuote()
        );

        // Tady ještě ověříme text chybové hlášky.
        // Tím si potvrdíme, že výjimka vznikla opravdu z důvodu,
        // který očekáváme.
        assertEquals("External API returned no data.", exception.getMessage());
    }

    // 3.
    @Test
    void getRandomQuote_throwsExceptionWhenApiReturnsNull() {
        // Mock vrátí null místo pole.
        when(restOperations.getForObject(
                eq("https://zenquotes.io/api/random"),
                eq(ExternalQuote[].class)
        )).thenReturn(null);

        // Očekáváme RuntimeException i v případě null odpovědi.
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> quoteService.getRandomQuote()
        );

        // Ověříme text chybové hlášky.
        assertEquals("External API returned no data.", exception.getMessage());
    }

    @Test
    void saveRandomQuote_returnsSavedQuote() {

        // Připravíme falešnou odpověď z externího API.
        ExternalQuote externalQuote = new ExternalQuote();
        externalQuote.setQ("Uložený testovací citát.");
        externalQuote.setA("Testovací autor");

        // Když service zavolá externí API,
        // vrátí se pole s jedním objektem externalQuote.
        when(restOperations.getForObject(
                eq("https://zenquotes.io/api/random"),
                eq(ExternalQuote[].class)
        )).thenReturn(new ExternalQuote[]{externalQuote});

        // DŘÍVE:
        // Mock repository vracel pořád ten samý předem připravený objekt přes thenReturn(...).

        // NYNÍ:
        // Mock repository bude reagovat na to, jaký objekt do něj service skutečně pošle.
        // To je realističtější, protože při ukládání často vzniká výsledek podle vstupu.
        when(quoteRepository.save(any(Quote.class))).thenAnswer(invocation -> {

            // Z invocation si vytáhneme první argument,
            // tedy objekt Quote, který service poslala do repository.save(...).
            Quote quoteToSave = invocation.getArgument(0);

            // Vytvoříme nový objekt, který bude představovat "uložený" quote.
            Quote savedQuote = new Quote();

            // Simulujeme, že při uložení repository přidělí id.
            savedQuote.setId(100L);

            // Zachováme text z objektu, který service poslala k uložení.
            savedQuote.setText(quoteToSave.getText());

            // Zachováme autora z objektu, který service poslala k uložení.
            savedQuote.setAuthor(quoteToSave.getAuthor());

            // Vrátíme objekt tak, jako by byl právě uložen do databáze nebo jiného úložiště.
            return savedQuote;
        });

        // Zavoláme testovanou metodu.
        Quote result = quoteService.saveRandomQuote();

        // Ověříme, že service opravdu zavolala externí API.
        verify(restOperations).getForObject(
                eq("https://zenquotes.io/api/random"),
                eq(ExternalQuote[].class)
        );

        // 6.
        // Vytvoříme "zachytávač" argumentu typu Quote.
        // Díky tomu se podíváme, jaký objekt service skutečně poslala do repository.
        ArgumentCaptor<Quote> quoteCaptor = ArgumentCaptor.forClass(Quote.class);

        // Ověříme, že se opravdu volalo repository.save(...) a zároveň si zachytíme předaný objekt.
        verify(quoteRepository).save(quoteCaptor.capture());

        // Tady získáme skutečný objekt, který service chtěla uložit.
        Quote capturedQuote = quoteCaptor.getValue();

        // Ověříme, že service správně převedla ExternalQuote na Quote.
        assertEquals("Uložený testovací citát.", capturedQuote.getText());
        assertEquals("Testovací autor", capturedQuote.getAuthor());

        // Před uložením ještě očekáváme null id.
        // Id se má doplnit až při ukládání v repository.
        assertNull(capturedQuote.getId());

        // Ověříme návratovou hodnotu celé metody.
        // To už je objekt vrácený z repository po "uložení".
        assertEquals(100L, result.getId());
        assertEquals("Uložený testovací citát.", result.getText());
        assertEquals("Testovací autor", result.getAuthor());
    }

    /*
    // 7.
    @Test
    void getQuote_returnsQuote_whenIdExists() {

        // DŘÍVE:
        // Testovali jsme hlavně getRandomQuote() a saveRandomQuote().

        // NYNÍ:
        // Testujeme metodu getQuote(Long id), tedy hledání konkrétního citátu podle id.

        // PROČ:
        // Chceme pokrýt i běžné čtení dat ze service, nejen práci s externím API a ukládáním.

        // Připravíme nový citát, který si nejdřív uložíme do interního seznamu service.
        Quote quote = new Quote();
        quote.setText("Hledaný citát");
        quote.setAuthor("JUnit");

        // addQuote(...) přidá citát do interního seznamu a zároveň mu přidělí id.
        Quote savedQuote = quoteService.addQuote(quote);

        // Teď zkusíme ten samý citát načíst podle jeho id.
        Quote foundQuote = quoteService.getQuote(savedQuote.getId());

        // Ověříme, že se vrátil správný objekt.
        assertEquals(savedQuote.getId(), foundQuote.getId());
        assertEquals("Hledaný citát", foundQuote.getText());
        assertEquals("JUnit", foundQuote.getAuthor());
    }
     */

    // 12.
    @Test
    void getQuote_returnsQuote_whenIdExists() {

        // DŘÍVE:
        // Service hledala v interním seznamu quotes.

        // NYNÍ:
        // Service hledá přes repository.findById(...).

        // PROČ:
        // Hledání dat chceme přesunout do repository vrstvy.
        Quote storedQuote = new Quote();
        storedQuote.setId(5L);
        storedQuote.setText("Hledaný citát");
        storedQuote.setAuthor("JUnit");

        when(quoteRepository.findById(5L)).thenReturn(storedQuote);

        Quote foundQuote = quoteService.getQuote(5L);

        verify(quoteRepository).findById(5L);

        assertEquals(5L, foundQuote.getId());
        assertEquals("Hledaný citát", foundQuote.getText());
        assertEquals("JUnit", foundQuote.getAuthor());
    }

    // 8.
    /*
    @Test
    void getQuote_throwsException_whenIdDoesNotExist() {

        // DŘÍVE:
        // Ověřovali jsme hlavně úspěšné scénáře.

        // NYNÍ:
        // Testujeme i opačný případ, kdy hledané id v seznamu neexistuje.

        // PROČ:
        // Coverage má pokrýt i chybový stav, ne jen "šťastnou cestu".

        // Zkusíme hledat id, které v interním seznamu není.
        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> quoteService.getQuote(999L)
        );

        // Ověříme i text výjimky.
        // Díky tomu máme jistotu, že výjimka vznikla přesně v očekávaném místě.
        assertEquals("Quote with id 999 not found.", exception.getMessage());
    }
     */

    // 14.
    @Test
    void getQuote_throwsException_whenIdDoesNotExist() {

        // DŘÍVE:
        // Service nenašla objekt v interním seznamu.

        // NYNÍ:
        // Repository vrátí null a service to převede na NoSuchElementException.

        // PROČ:
        // Chceme zachovat stejné chování navenek, ale změnit vnitřní způsob získání dat.
        when(quoteRepository.findById(999L)).thenReturn(null);

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> quoteService.getQuote(999L)
        );

        verify(quoteRepository).findById(999L);

        assertEquals("Quote with id 999 not found.", exception.getMessage());
    }

    // 9.
    /*
    @Test
    void addQuote_assignsId_addsQuoteToList_andReturnsIt() {

        // DŘÍVE:
        // Testovali jsme hlavně getRandomQuote(), saveRandomQuote() a getQuote(...).

        // NYNÍ:
        // Testujeme přímo addQuote(...), tedy metodu, která přidává nový citát do interního seznamu service.

        // PROČ:
        // Chceme pokrýt základní "ruční" přidání citátu, nejen přidání přes externí API.

        // Uložíme si počet citátů před přidáním nového.
        int sizeBefore = quoteService.getQuotes().size();

        // Připravíme nový citát bez id.
        // To odpovídá situaci, kdy uživatel pošle nový citát,
        // ale id se má doplnit až uvnitř service.
        Quote quote = new Quote();
        quote.setText("Nový citát přidaný ručně");
        quote.setAuthor("JUnit");

        // Zavoláme testovanou metodu.
        Quote savedQuote = quoteService.addQuote(quote);

        // Ověříme, že metoda přidělila id.
        // Protože addQuote(...) nastavuje id podle velikosti seznamu + 1,
        // očekáváme, že nové id bude právě sizeBefore + 1.
        assertEquals((long) (sizeBefore + 1), savedQuote.getId());

        // Ověříme, že text zůstal správný.
        assertEquals("Nový citát přidaný ručně", savedQuote.getText());

        // Ověříme, že autor zůstal správný.
        assertEquals("JUnit", savedQuote.getAuthor());

        // Ověříme, že se seznam opravdu zvětšil o 1.
        assertEquals(sizeBefore + 1, quoteService.getQuotes().size());

        // Vezmeme poslední prvek seznamu.
        // To by měl být právě nově přidaný citát.
        Quote lastQuote = quoteService.getQuotes().get(quoteService.getQuotes().size() - 1);

        // Ověříme, že poslední citát v seznamu odpovídá tomu, co jsme přidali.
        assertEquals((long) (sizeBefore + 1), lastQuote.getId());
        assertEquals("Nový citát přidaný ručně", lastQuote.getText());
        assertEquals("JUnit", lastQuote.getAuthor());
    }
     */

    // 11.
    @Test
    void addQuote_delegatesToRepository_andReturnsSavedQuote() {

        // DŘÍVE:
        // addQuote(...) přidělovala id sama
        // a ukládala objekt do interního seznamu service.

        // NYNÍ:
        // addQuote(...) předává ukládání do repository.

        // PROČ:
        // Chceme testovat nové rozdělení odpovědností:
        // service koordinuje, repository ukládá.
        Quote inputQuote = new Quote();
        inputQuote.setText("Nový citát přidaný ručně");
        inputQuote.setAuthor("JUnit");

        when(quoteRepository.save(any(Quote.class))).thenAnswer(invocation -> {

            // Vezmeme objekt, který service poslala do repository.
            Quote quoteToSave = invocation.getArgument(0);

            // Vytvoříme objekt, který bude představovat uložený výsledek.
            Quote savedQuote = new Quote();
            savedQuote.setId(1L);
            savedQuote.setText(quoteToSave.getText());
            savedQuote.setAuthor(quoteToSave.getAuthor());

            return savedQuote;
        });

        Quote result = quoteService.addQuote(inputQuote);

        // Ověříme, že service opravdu zavolala repository.save(...).
        verify(quoteRepository).save(any(Quote.class));

        // Ověříme návratovou hodnotu.
        assertEquals(1L, result.getId());
        assertEquals("Nový citát přidaný ručně", result.getText());
        assertEquals("JUnit", result.getAuthor());
    }

    // 10.
    /*
    @Test
    void getQuotes_returnsAllStoredQuotes() {

        // DŘÍVE:
        // Testovali jsme jednotlivé operace: getRandomQuote(), saveRandomQuote(), getQuote(), addQuote().

        // NYNÍ:
        // Testujeme metodu getQuotes(), tedy vrácení celého seznamu citátů.

        // PROČ:
        // Chceme pokrýt i čtení všech dat najednou, ne jen práci s jedním konkrétním citátem.

        // Přidáme první citát do interního seznamu service.
        Quote firstQuote = new Quote();
        firstQuote.setText("První citát");
        firstQuote.setAuthor("Autor 1");
        quoteService.addQuote(firstQuote);

        // Přidáme druhý citát do interního seznamu service.
        Quote secondQuote = new Quote();
        secondQuote.setText("Druhý citát");
        secondQuote.setAuthor("Autor 2");
        quoteService.addQuote(secondQuote);

        // Zavoláme testovanou metodu.
        List<Quote> result = quoteService.getQuotes();

        // Ověříme, že seznam obsahuje právě 2 položky.
        assertEquals(2, result.size());

        // Ověříme obsah prvního citátu.
        assertEquals("První citát", result.get(0).getText());
        assertEquals("Autor 1", result.get(0).getAuthor());

        // Ověříme obsah druhého citátu.
        assertEquals("Druhý citát", result.get(1).getText());
        assertEquals("Autor 2", result.get(1).getAuthor());
    }
     */

    // 15.
    @Test
    void getQuotes_returnsAllQuotes_fromRepository() {

        // DŘÍVE:
        // Service vracela interní seznam quotes.

        // NYNÍ:
        // Service vrací to, co dostane z repository.findAll().

        // PROČ:
        // Po refaktoru chceme mít čtení dat soustředěné v repository vrstvě.
        Quote firstQuote = new Quote();
        firstQuote.setId(1L);
        firstQuote.setText("První quote");
        firstQuote.setAuthor("Autor 1");

        Quote secondQuote = new Quote();
        secondQuote.setId(2L);
        secondQuote.setText("Druhý quote");
        secondQuote.setAuthor("Autor 2");

        when(quoteRepository.findAll()).thenReturn(List.of(firstQuote, secondQuote));

        List<Quote> result = quoteService.getQuotes();

        // Ověříme, že service opravdu delegovala čtení na repository.
        verify(quoteRepository).findAll();

        assertEquals(2, result.size());

        assertEquals("První quote", result.get(0).getText());
        assertEquals("Autor 1", result.get(0).getAuthor());

        assertEquals("Druhý quote", result.get(1).getText());
        assertEquals("Autor 2", result.get(1).getAuthor());
    }
}
/*
1. test uakzuje, že:
- service se dá testovat bez Springu
- místo skutečného API používáš mock
- when(...).thenReturn(...) znamená:
    - když nastane toto volání,
    - vrať tento připravený výsledek
Co si při tom všimnout v hlavě?
DŘÍVE
Kdyby logika byla přímo v controlleru, testovalo by se to hůř.
NYNÍ
Logika je v QuoteService, takže ji můžeme testovat samostatně.
PROČ
Protože service má jasné závislosti:
    - RestOperations
    - interní logiku převodu ExternalQuote -> Quote
A závislost umíme nahradit mockem.
 */

/*
2. říká, že když API vrátí prázdné pole, metoda getRandomQuote() má vyhodit výjimku RuntimeException s konkrétní zprávou.
DŘÍVE
Testovali jsme jen případ, kdy všechno funguje.
NYNÍ
Testujeme i situaci, kdy externí API vrátí špatná data.
PROČ
Když testujeme jen správný průchod, nevíme, jak se kód zachová při chybě.
 */

/*
3. test je hezký právě proto, že je skoro stejný jako předchozí.
DŘÍVE
Testovali jsme:
    - správný výsledek
    - prázdné pole
NYNÍ
Testujeme i případ:
    - odpověď je null
PROČ
Externí API nemusí vracet jen špatný obsah, ale někdy i žádný objekt vůbec.
 */

/*
4. neověřujeme výsledek, ale chování.
DŘÍVE
Kontrolovali jsme jen:
    - co metoda vrátila
To znamená:
    - výstup
NYNÍ
Kontrolujeme i:
    - co metoda udělala uvnitř
To znamená:
    - jestli opravdu zavolala svého pomocníka / závislost
PROČ
Když testujeme metodu, někdy nás nezajímá jen výsledek, ale i to:
    - jestli se volala správná závislost
    - jestli se volala správným způsobem
    - jestli se nevolalo něco, co nemělo
 */

/*
5.
DŘÍVE:
thenReturn(...) = mock vrátí vždy stejnou připravenou hodnotu.
NYNÍ:
thenAnswer(...) = mock umí vrátit výsledek podle toho, co do metody skutečně přišlo.
PROČ:
To se hodí, když chceme simulovat chování ukládání, které mění nebo doplňuje data podle vstupu.
 */

/*
6.
DŘÍVE:
Ověřovali jsme jen, že se repository.save(...) zavolalo.
NYNÍ:
ověřujeme i to, jaký konkrétní objekt service do repository poslala.
PROČ:
Někdy nestačí vědět, že se metoda zavolala.
Chceme vědět i s jakými daty byla zavolána.
 */

/*
7. a 8.
DŘÍVE:
Testovali jsme získání random quote a ukládání quote.
NYNÍ:
Testujeme i vyhledání konkrétního quote podle id.
PROČ:
Potřebujeme pokrýt jak úspěšné nalezení, tak i situaci, kdy požadovaný záznam neexistuje.
 */

/*
9.
DŘÍVE:
Testovali jsme hlavně získání a ukládání random quote.
NYNÍ:
Testujeme i ruční přidání citátu přes addQuote(...).
PROČ:
Chceme pokrýt i běžnou lokální práci se seznamem citátů, nejen scénáře napojené na externí API.
 */

/*
10.
DŘÍVE:
Testovali jsme práci s jedním konkrétním quote.
NYNÍ:
Testujeme vrácení celého seznamu quoteů.
PROČ:
Chceme mít pokrytou i metodu, která vrací všechna uložená data.
 */