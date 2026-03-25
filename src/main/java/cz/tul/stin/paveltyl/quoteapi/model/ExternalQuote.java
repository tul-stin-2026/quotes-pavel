package cz.tul.stin.paveltyl.quoteapi.model;

import lombok.Data;

// externí API má jiný JSON než náš model
@Data
public class ExternalQuote {
    private String content;
    private String author;
}
