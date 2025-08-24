module space.typro.packetmanager {
    requires transitive java.net.http;  // для HTTP загрузок
    requires transitive java.sql;       // на будущее для метаданных

    requires static lombok;

    // Наши внутренние зависимости
    requires transitive space.typro.common;
    requires transitive space.typro.directorymanager;
    requires com.google.gson;

    // Экспортируем все пакеты
    exports space.typro.Download;
    exports space.typro.packetmanager;
}