module space.typro.common {
    requires transitive java.desktop;
    requires transitive java.management;
    requires transitive org.slf4j;

    requires static lombok;
    requires jdk.management;

    opens space.typro.common to org.slf4j;

    exports space.typro.common;
}