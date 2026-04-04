module it.polimi.ingsw.am48 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires it.polimi.ingsw.am48;

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
    // exports it.polimi.ingsw.am48.model.factory;
    exports it.polimi.ingsw.am48.dto;
    exports it.polimi.ingsw.am48.exception;
    exports it.polimi.ingsw.am48.repository;
    exports it.polimi.ingsw.am48.controller;

    opens it.polimi.ingsw.am48 to com.fasterxml.jackson.databind;
}