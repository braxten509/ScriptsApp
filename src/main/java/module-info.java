module org.bchenay.doterraapp20 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires java.scripting;

    opens org.bchenay.doterraapp20 to javafx.fxml;
    exports org.bchenay.doterraapp20;
    
    opens com.doterra.app to javafx.fxml;
    opens com.doterra.app.view to javafx.fxml;
    opens com.doterra.app.controller to javafx.fxml;
    exports com.doterra.app;
    exports com.doterra.app.view;
    exports com.doterra.app.controller;
    exports com.doterra.app.model;
    exports com.doterra.app.util;
}