package space.typro.typicallauncher.controllers.scenes.subscenes;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import space.typro.typicallauncher.ResourceHelper;
import space.typro.typicallauncher.controllers.NewsArticleController;
import space.typro.typicallauncher.models.NewsArticle;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;

public class NewsController {
    @FXML
    private VBox newsContainer;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @FXML
    public void initialize() {
        List<NewsArticle> articles = List.of(
                new NewsArticle(
                        "TyPro Network Update 2.1",
                        "TyPro Site",
                        LocalDate.of(2024, 1, 15),
                        "Major updates to the TyPro network featuring enhanced security and improved performance.",
                        List.of(
                                "Enhanced anti-cheat protection",
                                "Improved server stability",
                                "New player verification system",
                                "Updated launcher interface"
                        )
                ),
                new NewsArticle(
                        "TyLauncher v3.2 Release",
                        "TyLauncher",
                        LocalDate.of(2024, 1, 12),
                        "New launcher version with improved mod management and server selection.",
                        List.of(
                                "Automatic mod installation",
                                "Server quick-connect",
                                "Update management",
                                "Profile synchronization"
                        )
                ),
                new NewsArticle(
                        "TyMMO Server Expansion",
                        "TyMMO Server",
                        LocalDate.of(2024, 1, 10),
                        "Massive multiplayer online experience gets new content and features.",
                        List.of(
                                "New dungeon content",
                                "Enhanced PvP systems",
                                "Guild improvements",
                                "Performance optimizations"
                        )
                )
        );
        articles.forEach(this::addArticle);
    }

    public void addArticle(NewsArticle article) {
        try {
            FXMLLoader loader = new FXMLLoader(ResourceHelper.getResourceUrlByType(ResourceHelper.ResourceFolder.SCENES, "news-article.fxml"));
            Node articleNode = loader.load();

            NewsArticleController controller = loader.getController();
            controller.setArticleData(article);

            newsContainer.getChildren().add(articleNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addNewArticle(String title, String tag, LocalDate date, String description, List<String> changes) {
        NewsArticle newArticle = new NewsArticle(title, tag, date, description, changes);
        addArticle(newArticle);
    }
}
