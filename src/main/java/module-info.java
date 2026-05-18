module it.polimi.ingsw.am48 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires java.rmi;
    requires java.desktop;
    requires com.zaxxer.hikari;
    requires java.sql;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires javafx.graphics;
    requires javafx.media;
    exports it.polimi.ingsw.am48;
    exports it.polimi.ingsw.am48.model.game;
    exports it.polimi.ingsw.am48.model.player;
    exports it.polimi.ingsw.am48.model.card;
    exports it.polimi.ingsw.am48.model.board;
    exports it.polimi.ingsw.am48.model.phase;
    exports it.polimi.ingsw.am48.model.strategy;
    exports it.polimi.ingsw.am48.model.notificator;
    exports it.polimi.ingsw.am48.model.enums;
    exports it.polimi.ingsw.am48.model.delta;
    exports it.polimi.ingsw.am48.model.snapshot;
    exports it.polimi.ingsw.am48.dto;
    exports it.polimi.ingsw.am48.exception;
    exports it.polimi.ingsw.am48.repository;
    exports it.polimi.ingsw.am48.controller;
    exports it.polimi.ingsw.am48.view.gui;

    exports it.polimi.ingsw.am48.network to java.rmi;
    exports it.polimi.ingsw.am48.network.server to java.rmi;
    exports it.polimi.ingsw.am48.network.client to java.rmi;

    opens it.polimi.ingsw.am48.model.delta        to com.fasterxml.jackson.databind;
    opens it.polimi.ingsw.am48.model.snapshot     to com.fasterxml.jackson.databind;
    opens it.polimi.ingsw.am48.network.messages.notifications to com.fasterxml.jackson.databind;
    opens it.polimi.ingsw.am48.network.messages.commands to com.fasterxml.jackson.databind;
    opens it.polimi.ingsw.am48.view.gui to javafx.graphics, javafx.fxml;
}