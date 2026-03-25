package cz.tul.stin.paveltyl.quoteapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//  Teď si vytvoříme objekt, který budeme vracet jako JSON.
// Lombok generuje gettery/settery a zjednodušuje modely, používáme ho pro pohodlí, ale je dobré rozumět i klasickému Java kódu.
@Data // = getter + setter + toString
@AllArgsConstructor // = konstruktor
@NoArgsConstructor // = konstruktor
public class Quote {

    private Long id;
    private String text;
    private String author;
}

// Spring automaticky převede tento objekt na JSON.