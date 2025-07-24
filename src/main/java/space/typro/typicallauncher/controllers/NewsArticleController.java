package space.typro.typicallauncher.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import space.typro.typicallauncher.models.NewsArticle;

import java.time.format.DateTimeFormatter;

public class NewsArticleController {
    @FXML
    private Label titleLabel;
    @FXML
    private Label tagLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private VBox changesContainer;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public void setArticleData(NewsArticle article) {
        titleLabel.setText(article.getTitle());
        tagLabel.setText(article.getTag());
        dateLabel.setText(article.getDate().format(dateFormatter));
        descriptionLabel.setText(article.getDescription());

        changesContainer.getChildren().clear();

        article.getChanges().forEach(change -> {
            Label changeLabel = new Label("• " + change);
            changeLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");
            changesContainer.getChildren().add(changeLabel);
        });
    }
}
