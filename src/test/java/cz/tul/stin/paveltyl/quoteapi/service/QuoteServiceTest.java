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
// Starará se o vytvoření mocků před spuštěním testu.
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

        // Tady říkáme:
        // když service zavolá externí API,
        // vrátí se pole s jedním objektem externalQuote.
        when(restOperations.getForObject(
                eq("https://zenquotes.io/api/random"),
                eq(ExternalQuote[].class)
        )).thenReturn(new ExternalQuote[]{externalQuote});

        // Tady si připravíme objekt, který bude "uložený".
        // Simulujeme tím situaci, že repository při ukládání doplní id.
        Quote savedQuote = new Quote();
        savedQuote.setId(100L);
        savedQuote.setText("Uložený testovací citát.");
        savedQuote.setAuthor("Testovací autor");

        // Tady nastavujeme druhý mock.
        // Když repository dostane nějaký Quote k uložení,
        // vrátí prepared objekt savedQuote.
        when(quoteRepository.save(any(Quote.class))).thenReturn(savedQuote);

        // Zavoláme testovanou metodu.
        Quote result = quoteService.saveRandomQuote();

        // Ověříme, že service opravdu zavolala externí API.
        verify(restOperations).getForObject(
                eq("https://zenquotes.io/api/random"),
                eq(ExternalQuote[].class)
        );

        // Ověříme, že service opravdu zavolala repository.save(...).
        verify(quoteRepository).save(any(Quote.class));

        // Ověříme, že výsledek odpovídá tomu, co "vrátilo repository".
        assertEquals(100L, result.getId());
        assertEquals("Uložený testovací citát.", result.getText());
        assertEquals("Testovací autor", result.getAuthor());
    }
}
/*
Co ukazuje 1. test?
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