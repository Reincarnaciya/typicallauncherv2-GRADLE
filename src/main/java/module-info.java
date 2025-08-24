module space.typro.typicallauncher {
    // JavaFX модули
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.media;
    requires javafx.web;

    // Внешние библиотеки
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.fasterxml.jackson.databind;
    requires com.google.gson;
    requires org.jetbrains.annotations;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;

    // Статические зависимости (только для компиляции)
    requires static lombok;

    // Наши внутренние модули
    requires space.typro.common;
    requires space.typro.directorymanager;
    requires space.typro.packetmanager;
    requires java.security.sasl;
    requires org.slf4j;

    // Экспорты для FXML и reflection
    opens space.typro.typicallauncher to javafx.fxml;
    opens space.typro.typicallauncher.controllers to javafx.fxml;
    opens space.typro.typicallauncher.controllers.scenes to javafx.fxml;
    opens space.typro.typicallauncher.controllers.scenes.subscenes to javafx.fxml;
    opens space.typro.typicallauncher.models to javafx.fxml;
    opens space.typro.typicallauncher.events to javafx.fxml;

    // Экспорты наших API
    exports space.typro.typicallauncher;
    exports space.typro.typicallauncher.controllers;
    exports space.typro.typicallauncher.controllers.scenes;
    exports space.typro.typicallauncher.controllers.scenes.subscenes;
    exports space.typro.typicallauncher.models;
    exports space.typro.typicallauncher.events;
    exports space.typro.typicallauncher.utils;
    exports space.typro.typicallauncher.managers;
}