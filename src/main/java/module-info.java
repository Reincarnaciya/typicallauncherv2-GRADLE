module space.typro.typicallauncher {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires static lombok;
    requires jdk.management;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires java.desktop;
    requires java.security.sasl;
    requires ch.qos.logback.classic;
    requires org.slf4j;
    requires ch.qos.logback.core;
    requires org.jetbrains.annotations;

    opens space.typro.typicallauncher to javafx.fxml;
    opens space.typro.typicallauncher.controllers.scenes.subscenes to javafx.fxml;
    exports space.typro.typicallauncher;
    exports space.typro.typicallauncher.events;
    opens space.typro.typicallauncher.events;
    exports space.typro.typicallauncher.controllers.scenes.subscenes;
    exports space.typro.typicallauncher.models;
    exports space.typro.typicallauncher.controllers.scenes;
    opens space.typro.typicallauncher.controllers.scenes to javafx.fxml;
    exports space.typro.typicallauncher.controllers;
    opens space.typro.typicallauncher.controllers to javafx.fxml;
}
