package space.typro.typicallauncher.models;

import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class NewsArticle {
    // Getters
    private final String title;
    private final String tag;
    private final LocalDate date;
    private final String description;
    private final List<String> changes;

    public NewsArticle(String title, String tag, LocalDate date, String description, List<String> changes) {
        this.title = title;
        this.tag = tag;
        this.date = date;
        this.description = description;
        this.changes = changes;
    }

}
