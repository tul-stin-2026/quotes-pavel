package cz.tul.stin.paveltyl.quoteapi.model;

import lombok.Data;

// externí API má jiný JSON než náš model
@Data
public class ExternalQuote {
    private String q;  // text
    private String a;  // author
}
// Externí API nejsou pod naší kontrolou. Někdy se mění nebo přestanou fungovat.
// Reálný problém:
// - API zmizí,
// - změní formát API,
// - API začne vracet chyby.
// Backend se musí přizpůsobit.