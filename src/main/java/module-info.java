module it.polimi.ingsw.am48 {
    requires javafx.controls;
    requires javafx.fxml;

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
}