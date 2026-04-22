package cz.tul.stin.paveltyl.quoteapi.controller;

import lombok.Getter;
import lombok.Setter;

// Tato jednoduchá třída bude sloužit jako JSON odpověď při chybě.
// DŘÍVE:
// Výjimka propadla ven a test skončil přes ServletException.
//
// NYNÍ:
// Budeme chtít vracet hezký JSON objekt s chybovou zprávou.
//
// PROČ:
// API je pak čitelnější pro klienta i pro testy.
@Setter
@Getter
public class ErrorResponse {

    // Setter nastavuje text chyby.
    // Getter vrací text chyby.
    // Sem uložíme text chyby.
    private String message;

    // Bezparametrický konstruktor se hodí pro práci Springu/Jacksonu.
    public ErrorResponse() {
    }

    // Tento konstruktor použijeme při rychlém vytvoření odpovědi s textem chyby.
    public ErrorResponse(String message) {
        this.message = message;
    }

}