package cz.tul.stin.paveltyl.quoteapi.controller;

// Budeme používat model Quote, protože service nám ho vrací.
import cz.tul.stin.paveltyl.quoteapi.model.Quote;

// Controller závisí na QuoteService, kterou v testu nahradíme mockem.
import cz.tul.stin.paveltyl.quoteapi.service.QuoteService;

// JUnit 5.
import org.junit.jupiter.api.Test;

// Spring MVC test anotace pro Boot 4.
// Tato anotace načte jen webovou vrstvu a konkrétní controller.
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

// MockMvc umí "předstírat" HTTP požadavky bez spuštění skutečného serveru.
import org.springframework.test.web.servlet.MockMvc;

// Přes @Autowired si necháme MockMvc dodat ze Spring test contextu.
import org.springframework.beans.factory.annotation.Autowired;

// V Boot 4 / Spring Framework 6.2 se místo @MockBean používá @MockitoBean.
import org.springframework.test.context.bean.override.mockito.MockitoBean;

// Statické importy pro MockMvc testy.
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


// 1.
@WebMvcTest(QuoteController.class)
class QuoteControllerTest {

    // MockMvc simuluje HTTP volání na controller.
    @Autowired
    private MockMvc mockMvc;

    // DŘÍVE:
    // V QuoteServiceTest jsme mockovali závislosti service pomocí @Mock.

    // NYNÍ:
    // Tady mockujeme QuoteService jako bean uvnitř Spring test contextu.

    // PROČ:
    // Chceme testovat controller, ne skutečnou implementaci service.
    @MockitoBean
    private QuoteService quoteService;

    @Test
    void getRandomQuote_returnsQuoteAsJson() throws Exception {

        // Připravíme objekt, který má service vrátit.
        Quote quote = new Quote();
        quote.setId(null);
        quote.setText("Controller test quote");
        quote.setAuthor("Mocked service");

        // Když controller zavolá quoteService.getRandomQuote(),
        // vrátí se tento připravený objekt.
        when(quoteService.getRandomQuote()).thenReturn(quote);

        // DŘÍVE:
        // V unit testech service jsme volali metodu přímo v Javě.

        // NYNÍ:
        // Tady simulujeme skutečný HTTP GET požadavek na endpoint /quotes/random.

        // PROČ:
        // Chceme ověřit, že controller správně:
        // - přijme request,
        // - zavolá service,
        // - vrátí JSON odpověď.
        mockMvc.perform(get("/quotes/random"))

                // Očekáváme HTTP 200 OK.
                .andExpect(status().isOk())

                // Ověříme JSON pole "text".
                .andExpect(jsonPath("$.text").value("Controller test quote"))

                // Ověříme JSON pole "author".
                .andExpect(jsonPath("$.author").value("Mocked service"));
    }

    // 2.
    @Test
    void saveRandomQuote_returnsSavedQuoteAsJson() throws Exception {

        // DŘÍVE:
        // Testovali jsme GET /quotes/random, tedy endpoint, který jen vrátí náhodný citát.

        // NYNÍ:
        // Testujeme POST /quotes/random, tedy endpoint, který má citát nejen získat, ale i "uložit" přes service.

        // PROČ:
        // Chceme pokrýt i druhý endpoint controlleru a ověřit, že vrací správný JSON.

        // Připravíme objekt, který bude service vracet.
        // Simulujeme tím uložený citát.
        Quote quote = new Quote();
        quote.setId(100L);
        quote.setText("Saved controller test quote");
        quote.setAuthor("Mocked service");

        // Když controller zavolá quoteService.saveRandomQuote(),
        // vrátí se tento připravený objekt.
        when(quoteService.saveRandomQuote()).thenReturn(quote);

        // Simulujeme HTTP POST požadavek na endpoint /quotes/random.
        mockMvc.perform(post("/quotes/random"))

                // Očekáváme HTTP 200 OK.
                .andExpect(status().isOk())

                // Ověříme JSON pole "id".
                .andExpect(jsonPath("$.id").value(100))

                // Ověříme JSON pole "text".
                .andExpect(jsonPath("$.text").value("Saved controller test quote"))

                // Ověříme JSON pole "author".
                .andExpect(jsonPath("$.author").value("Mocked service"));
    }

    // 3.
    @Test
    void getQuote_returnsQuoteByIdAsJson() throws Exception {

        // DŘÍVE:
        // Testovali jsme endpointy bez path variable:
        // GET /quotes/random a POST /quotes/random.

        // NYNÍ:
        // Testujeme endpoint GET /quotes/{id}, tedy načtení jednoho konkrétního citátu podle id.

        // PROČ:
        // Chceme pokrýt i controller metodu, která pracuje s hodnotou z URL.

        // Připravíme objekt, který má mockovaná service vrátit.
        Quote quote = new Quote();
        quote.setId(5L);
        quote.setText("Quote loaded by id");
        quote.setAuthor("Mocked service");

        // Když controller zavolá quoteService.getQuote(5L),
        // vrátí se tento připravený objekt.
        when(quoteService.getQuote(5L)).thenReturn(quote);

        // Simulujeme HTTP GET požadavek na endpoint /quotes/5.
        mockMvc.perform(get("/quotes/5"))

                // Očekáváme HTTP 200 OK.
                .andExpect(status().isOk())

                // Ověříme JSON pole "id".
                .andExpect(jsonPath("$.id").value(5))

                // Ověříme JSON pole "text".
                .andExpect(jsonPath("$.text").value("Quote loaded by id"))

                // Ověříme JSON pole "author".
                .andExpect(jsonPath("$.author").value("Mocked service"));

        // 10.
        // NOVĚ:
        // Ověříme, že controller opravdu předal id z URL do service vrstvy.
        // Tady už nekontrolujeme JSON odpověď, ale chování controlleru uvnitř.
        verify(quoteService).getQuote(5L);
    }

    // 4.
    /*
    @Test
    void getQuote_throwsServletException_whenQuoteDoesNotExist() {

        // DŘÍVE:
        // Chtěli jsme ověřovat HTTP 500 přes status().

        // NYNÍ:
        // V našem aktuálním stavu výjimka propadne ven z controlleru a MockMvc ji zabalí do ServletException.

        // PROČ:
        // Zatím nemáme @ExceptionHandler ani @ControllerAdvice, takže Spring nemá vlastní pravidlo, jak tuto výjimku převést na hezkou HTTP odpověď.
        when(quoteService.getQuote(999L))
                .thenThrow(new NoSuchElementException("Quote with id 999 not found."));

        ServletException exception = assertThrows(
                ServletException.class,
                () -> mockMvc.perform(get("/quotes/999"))
        );

        // Ověříme, že skutečnou příčinou byla naše NoSuchElementException.
        assertEquals(NoSuchElementException.class, exception.getCause().getClass());

        // Ověříme i text původní výjimky.
        assertEquals("Quote with id 999 not found.", exception.getCause().getMessage());
    }
    */

    // 5.
    @Test
    void getQuote_returnsNotFound_whenQuoteDoesNotExist() throws Exception {

        // DŘÍVE:
        // Bez @ControllerAdvice výjimka propadla ven a test jsme psali přes assertThrows(ServletException.class).

        // NYNÍ:
        // Máme vlastní zpracování výjimky, takže očekáváme normální HTTP odpověď 404.

        // PROČ:
        // Controller teď vrací hezčí a čitelnější REST odpověď.
        when(quoteService.getQuote(999L))
                .thenThrow(new NoSuchElementException("Quote with id 999 not found."));

        mockMvc.perform(get("/quotes/999"))

                // Očekáváme HTTP 404 Not Found.
                .andExpect(status().isNotFound())

                // Ověříme JSON pole "message".
                .andExpect(jsonPath("$.message").value("Quote with id 999 not found."));
    }

    // 6.
    @Test
    void getQuotes_returnsAllQuotesAsJson() throws Exception {

        // DŘÍVE:
        // Testovali jsme endpointy, které vracely jeden objekt Quote.

        // NYNÍ:
        // Testujeme endpoint GET /quotes, který má vrátit celý seznam citátů.

        // PROČ:
        // Chceme pokrýt i případ, kdy controller vrací kolekci objektů, ne jen jeden konkrétní Quote.

        // Připravíme první citát, který má service vrátit.
        Quote firstQuote = new Quote();
        firstQuote.setId(1L);
        firstQuote.setText("První quote");
        firstQuote.setAuthor("Autor 1");

        // Připravíme druhý citát.
        Quote secondQuote = new Quote();
        secondQuote.setId(2L);
        secondQuote.setText("Druhý quote");
        secondQuote.setAuthor("Autor 2");

        // Mockovaná service vrátí seznam dvou citátů.
        when(quoteService.getQuotes()).thenReturn(List.of(firstQuote, secondQuote));

        // Simulujeme HTTP GET požadavek na endpoint /quotes.
        mockMvc.perform(get("/quotes"))

                // Očekáváme HTTP 200 OK.
                .andExpect(status().isOk())

                // Ověříme, že JSON pole má velikost 2.
                .andExpect(jsonPath("$.length()").value(2))

                // Ověříme první prvek v poli.
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].text").value("První quote"))
                .andExpect(jsonPath("$[0].author").value("Autor 1"))

                // Ověříme druhý prvek v poli.
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].text").value("Druhý quote"))
                .andExpect(jsonPath("$[1].author").value("Autor 2"));

        // 11.
        // NOVĚ:
        // Ověříme, že controller opravdu delegoval práci na service metodu getQuotes().
        // Tady už nekontrolujeme obsah JSON, ale vnitřní chování controlleru.
        verify(quoteService).getQuotes();
    }

    // 7.
    @Test
    void addQuote_acceptsRequestBody_andReturnsSavedQuoteAsJson() throws Exception {

        // DŘÍVE:
        // Testovali jsme endpointy, kde jsme jen volali URL a nic jsme neposílali v těle požadavku.

        // NYNÍ:
        // Testujeme POST /quotes s @RequestBody.
        // Tedy controller musí přijmout JSON z requestu, převést ho na objekt Quote a předat ho do service.

        // PROČ:
        // Chceme pokrýt i běžný scénář, kdy klient pošle nový objekt v těle HTTP požadavku.

        // Připravíme objekt, který má service vrátit.
        // Simulujeme tím "uložený" quote po zpracování v service.
        Quote savedQuote = new Quote();
        savedQuote.setId(1L);
        savedQuote.setText("Nový citát");
        savedQuote.setAuthor("Student");

        // Když controller zavolá quoteService.addQuote(...),
        // vrátí se tento připravený objekt.
        when(quoteService.addQuote(any(Quote.class))).thenReturn(savedQuote);

        // Připravíme JSON tělo requestu.
        // To simuluje data, která by poslal klient.
        String requestBody = """
            {
              "text": "Nový citát",
              "author": "Student"
            }
            """;

        // Simulujeme HTTP POST požadavek na /quotes
        // a přikládáme JSON do těla requestu.
        mockMvc.perform(post("/quotes")
                        .contentType("application/json")
                        .content(requestBody))

                // Očekáváme HTTP 200 OK.
                .andExpect(status().isOk())

                // Ověříme JSON pole "id" ve vrácené odpovědi.
                .andExpect(jsonPath("$.id").value(1))

                // Ověříme JSON pole "text".
                .andExpect(jsonPath("$.text").value("Nový citát"))

                // Ověříme JSON pole "author".
                .andExpect(jsonPath("$.author").value("Student"));

        // 8.
        // NOVĚ:
        // Ověříme, že controller opravdu zavolal service metodu addQuote(...).
        // Tím nekontrolujeme výsledek, ale chování controlleru.
        // verify(quoteService).addQuote(any(Quote.class));

        // 9.
        // DŘÍVE:
        // Ověřovali jsme jen to, že controller zavolal service metodu addQuote(...).

        // NYNÍ:
        // Zachytíme si konkrétní objekt Quote, který controller předal do service vrstvy.

        // PROČ:
        // Nechceme ověřit jen "že se něco zavolalo", ale i "s jakými daty se to zavolalo".
        ArgumentCaptor<Quote> quoteCaptor = ArgumentCaptor.forClass(Quote.class);

// Ověříme, že controller zavolal service,
// a zároveň zachytíme předaný objekt.
        verify(quoteService).addQuote(quoteCaptor.capture());

// Získáme skutečný objekt, který controller vytvořil z JSON request body.
        Quote capturedQuote = quoteCaptor.getValue();

// Ověříme, že text z request body byl správně převeden do objektu Quote.
        assertEquals("Nový citát", capturedQuote.getText());

// Ověříme, že autor z request body byl správně převeden do objektu Quote.
        assertEquals("Student", capturedQuote.getAuthor());

// Ověříme, že klient v requestu neposílal id,
// takže před předáním do service je id stále null.
        assertNull(capturedQuote.getId());
    }
}

/*
1.
DŘÍVE:
Testovali jsme service přímo jako obyčejnou třídu bez Springu.
NYNÍ:
Testujeme controller jako součást webové vrstvy Springu.
PROČ:
Chceme ověřit HTTP endpoint a JSON odpověď, ne jen obyčejné volání Java metody.
 */

/*
2.
DŘÍVE:
Testovali jsme GET endpoint controlleru.
NYNÍ:
Testujeme i POST endpoint controlleru.
PROČ:
Chceme pokrýt další HTTP metodu a ověřit, že controller správně deleguje na service.
 */

/*
3.
DŘÍVE:
Testovali jsme endpointy bez parametru v URL.
NYNÍ:
Testujeme endpoint s path variable, tedy /quotes/{id}.
PROČ:
Chceme ověřit, že controller správně přečte hodnotu z URL a předá ji do service.
 */

/*
4.
DŘÍVE:
Testovali jsme úspěšnou odpověď controlleru.
NYNÍ:
Testujeme, že při neexistujícím id výjimka propadne ven.
PROČ:
Protože zatím nemáme vlastní mapování výjimek na HTTP odpovědi.
 */

/*
5.
DŘÍVE:
Výjimka skončila jako technický pád testu / requestu.
NYNÍ:
Výjimku převádíme na HTTP 404 a JSON zprávu.
PROČ:
REST API má vracet čitelné odpovědi i při chybě.
 */

/*
6.
DŘÍVE:
Testovali jsme endpointy vracející jeden objekt.
NYNÍ:
Testujeme endpoint vracející seznam objektů.
 PROČ:
Controller často nevrací jen jeden záznam, ale i celé kolekce dat.
 */

/*
7.
DŘÍVE:
Testovali jsme endpointy bez request body.
NYNÍ:
Testujeme endpoint, který přijímá JSON přes @RequestBody.
PROČ:
Chceme ověřit, že controller správně přijme data z HTTP requestu a vrátí JSON odpověď.
 */

/*
8.
DŘÍVE:
Testovali jsme jen JSON odpověď controlleru.
NYNÍ:
Testujeme i to, že controller opravdu deleguje na service.
PROČ:
Controller nemá sám řešit logiku, ale má předat data dál do service vrstvy.
 */

/*
9.
DŘÍVE:
Testovali jsme JSON odpověď controlleru a pak jen to, že service byla zavolána.
NYNÍ:
Testujeme i to, jaký konkrétní objekt controller poslal do service.
PROČ:
U @RequestBody je důležité ověřit, že se JSON správně převedl na Java objekt.
 */

/*
10.
DŘÍVE:
Testovali jsme jen výstup controlleru.
NYNÍ:
Testujeme i to, že controller správně deleguje id do service.
PROČ:
U path variable chceme ověřit nejen JSON odpověď, ale i správné předání hodnoty z URL dál do aplikace.
 */

/*
11.
DŘÍVE:
Testovali jsme hlavně JSON odpověď controlleru.
NYNÍ:
Testujeme i to, že controller skutečně deleguje na service.getQuotes().
PROČ:
Controller má být tenký.
Nemá si data vyrábět sám, ale má je získat ze service vrstvy.
 */