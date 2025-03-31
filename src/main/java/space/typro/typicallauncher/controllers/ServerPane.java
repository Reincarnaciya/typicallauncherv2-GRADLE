package space.typro.typicallauncher.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import space.typro.typicallauncher.ResourceHelper;
import space.typro.typicallauncher.models.Server;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class ServerPane extends BaseController {
    @FXML private ImageView server_image;
    @FXML private Button play;
    @FXML private Button settings;

    @Setter
    private String serverName;
    @Setter
    private Server.ServerVersion serverVersion;
    private Image serverImage;

    public ServerPane(String serverName, Server.ServerVersion serverVersion, Image serverImage) {
        this.serverName = serverName;
        this.serverVersion = serverVersion;
        this.serverImage = serverImage;

        FXMLLoader loader = new FXMLLoader(
                ResourceHelper.getResourceUrlByType(ResourceHelper.ResourceFolder.SCENES, "server-pane.fxml")
        );
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            log.error("Failed to load FXML file", e);
            throw new RuntimeException("Failed to load FXML file", e);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (serverImage != null) {
            server_image.setImage(serverImage);
        }
    }

    public void setServerImage(Image serverImage) {
        this.serverImage = serverImage;
        if (server_image != null) {
            server_image.setImage(serverImage);
        }
    }


}