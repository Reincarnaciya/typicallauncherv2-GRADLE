package space.typro.typicallauncher.models;

import java.time.LocalDate;
import java.util.List;

public class NewsArticle {
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

    // Getters
    public String getTitle() { return title; }
    public String getTag() { return tag; }
    public LocalDate getDate() { return date; }
    public String getDescription() { return description; }
    public List<String> getChanges() { return changes; }
}
