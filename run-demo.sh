#!/bin/bash
# Run the Place Totem GUI Demo
# Usage: ./run-demo.sh [2|3|4|5]
# Default: 3 players

NUM_PLAYERS=${1:-3}

JFX_BASE="$HOME/.m2/repository/org/openjfx"
JACKSON="$HOME/.m2/repository/com/fasterxml/jackson"

MODULE_PATH="target/classes"
MODULE_PATH="$MODULE_PATH:$JFX_BASE/javafx-base/21.0.6/javafx-base-21.0.6-mac-aarch64.jar"
MODULE_PATH="$MODULE_PATH:$JFX_BASE/javafx-controls/21.0.6/javafx-controls-21.0.6-mac-aarch64.jar"
MODULE_PATH="$MODULE_PATH:$JFX_BASE/javafx-graphics/21.0.6/javafx-graphics-21.0.6-mac-aarch64.jar"
MODULE_PATH="$MODULE_PATH:$JFX_BASE/javafx-fxml/21.0.6/javafx-fxml-21.0.6-mac-aarch64.jar"
MODULE_PATH="$MODULE_PATH:$JACKSON/jackson-annotations/2.21/jackson-annotations-2.21.jar"
MODULE_PATH="$MODULE_PATH:$JACKSON/jackson-core/2.21.2/jackson-core-2.21.2.jar"
MODULE_PATH="$MODULE_PATH:$JACKSON/jackson-databind/2.21.2/jackson-databind-2.21.2.jar"
MODULE_PATH="$MODULE_PATH:$HOME/.m2/repository/org/yaml/snakeyaml/2.4/snakeyaml-2.4.jar"

java --module-path "$MODULE_PATH" \
  --add-modules javafx.controls,javafx.fxml,com.fasterxml.jackson.databind \
  -m it.polimi.ingsw.am48/it.polimi.ingsw.am48.view.gui.PlaceTotemGuiDemo "$NUM_PLAYERS"
