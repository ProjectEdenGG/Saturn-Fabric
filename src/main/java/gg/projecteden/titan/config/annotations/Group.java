package gg.projecteden.titan.config.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Group {
    String value();

    String Saturn = "Saturn";
    String Utilities = "Utilities";
    String Backpacks = "Backpacks";

}
