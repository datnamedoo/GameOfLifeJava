module com.datnamedoo.www {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires javafx.base;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    opens com.datnamedoo.www to javafx.fxml;
    exports com.datnamedoo.www;
    exports com.datnamedoo.www.mainmenu;

}
