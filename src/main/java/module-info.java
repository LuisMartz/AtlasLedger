module atlasledger {
    // JavaFX UI Framework
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    
    // Java Standard Library
    requires java.base;
    requires java.sql;
    requires java.net.http;
    
    // Logging
    requires org.slf4j;
    requires ch.qos.logback.classic;

    // Internal exports
    exports atlasledger.app;
    exports atlasledger.model;
    exports atlasledger.service;
    exports atlasledger.repository;
    exports atlasledger.dao;
    exports atlasledger.utils;
    exports atlasledger.ui.login;
    exports atlasledger.ui.dashboard;
    exports atlasledger.ui.ordenes;
    exports atlasledger.ui.productos;
    exports atlasledger.ui.proveedores;
    exports atlasledger.ui.informes;
    exports atlasledger.ui.trading;
    exports atlasledger.ui.packaging;
    exports atlasledger.ui.transport;
    exports atlasledger.simulation;
}
