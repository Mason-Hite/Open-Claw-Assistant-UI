package OpenClawUI.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import OpenClawUI.ai.OpenClawClient;
import OpenClawUI.storage.ChatHistoryManager;
import java.util.prefs.Preferences;
import java.util.Stack;

/**
 * ChatPanel - The left side of the main UI.
 * Handles the chat history ListView, user input TextArea,
 * Connect button, and Clear Chat button.
 * 
 * Also manages live updates when the bot name changes in Settings.
 */
public class ChatPanel extends VBox {

    private final ListView<String> chatHistory = new ListView<>();
    private final ChatHistoryManager historyManager;
    private final Stack<String> undoStack = new Stack<>(); // ← Stack for Undo Last Message
    private Label titleLabel;
    private TextArea inputArea;
    private String currentBotName;

    /**
     * Creates the chat panel and loads any saved chat history.
     * 
     * @param stage  the main application stage (used for dialogs)
     * @param client the OpenClawClient for sending messages to the bot
     */
    public ChatPanel(Stage stage, OpenClawClient client) {
        this.historyManager = new ChatHistoryManager();

        Preferences prefs = Preferences.userNodeForPackage(MainWindow.class);
        this.currentBotName = prefs.get("botName", "Claw Bot");

        setPadding(new Insets(20));
        setSpacing(18);
        setStyle("""
                -fx-background-color: linear-gradient(to bottom right, #14102a, #0b0f22);
                -fx-background-radius: 16;
                -fx-effect: dropshadow(three-pass-box, rgba(100, 150, 255, 0.2), 15, 0, 0, 8);
                """);

        titleLabel = new Label("🤖 " + currentBotName);
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #a0d8ff;");

        Button connectBtn = new Button("🔗 Connect to Bot");
        Button clearBtn = new Button("🗑 Clear Chat");
        Button undoBtn = new Button("↩ Undo Last");

        connectBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #00d4ff, #0099ff); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 9999; -fx-min-width: 160px;");
        clearBtn.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 9999;");
        undoBtn.setStyle(
                "-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 9999;");

        connectBtn.setOnAction(e -> {
            new SettingsDialog(stage).showAndWait();
            client.reloadSettings(); // ← THIS FIXES THE CACHE STICK
        });
        clearBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Clear Chat");
            alert.setHeaderText("Delete entire conversation?");
            alert.setContentText("This cannot be undone.");
            if (alert.showAndWait().get() == ButtonType.OK) {
                chatHistory.getItems().clear();
                undoStack.clear(); // ← Clear undo stack too
                chatHistory.getItems().add("👋 Chat cleared — ready to start fresh");
                historyManager.clear();
            }
        });

        // Undo last message using the Stack
        undoBtn.setOnAction(e -> {
            if (!undoStack.isEmpty()) {
                String lastYouMessage = undoStack.pop();
                // Remove the "You:" line and the bot's reply that followed it
                for (int i = chatHistory.getItems().size() - 1; i >= 0; i--) {
                    if (chatHistory.getItems().get(i).equals(lastYouMessage)) {
                        chatHistory.getItems().remove(i);
                        if (i < chatHistory.getItems().size()) {
                            chatHistory.getItems().remove(i);
                        }
                        break;
                    }
                }
                historyManager.save(chatHistory);
            }
        });

        HBox buttonBox = new HBox(10, connectBtn, undoBtn, clearBtn);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        // fixes horizontal line issue
        chatHistory.setStyle("-fx-background-color: #121212; -fx-background-radius: 12;");
        chatHistory.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                }
            }
        });

        chatHistory.getStylesheets().add("data:text/css," + """
                    .list-cell {
                        -fx-background-color: #121212;
                        -fx-border-width: 0;
                        -fx-padding: 8px;
                    }
                    .list-view {
                        -fx-control-inner-background: #121212;
                    }
                """);

        historyManager.load(chatHistory);
        if (chatHistory.getItems().isEmpty()) {
            chatHistory.getItems().add("👋 Ready — type below to chat with " + currentBotName);
        }

        inputArea = new TextArea();
        inputArea.setPromptText("Message " + currentBotName + "... (Ctrl + Enter to send)");
        inputArea.setPrefRowCount(5);
        inputArea.setWrapText(true);
        inputArea.setStyle("""
                -fx-background-color: #1e1e1e !important;
                -fx-text-fill: #e8e8e8 !important;
                -fx-prompt-text-fill: #888888 !important;
                -fx-control-inner-background: #1e1e1e !important;
                -fx-border-color: #333333;
                -fx-border-radius: 12;
                -fx-background-radius: 12;
                -fx-font-size: 15px;
                -fx-caret-color: #00d4ff;
                """);

        Button sendBtn = new Button("➤ Send");
        sendBtn.setStyle("""
                -fx-background-color: linear-gradient(to right, #00d4ff, #0099ff);
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-background-radius: 9999;
                -fx-min-width: 90px;
                """);

        HBox inputBox = new HBox(12, inputArea, sendBtn);
        inputBox.setAlignment(Pos.BOTTOM_CENTER);
        HBox.setHgrow(inputArea, Priority.ALWAYS);

        getChildren().addAll(titleLabel, buttonBox, chatHistory, inputBox);

        // Send logic, auto-save, and push to undo stack
        Runnable send = () -> {
            String msg = inputArea.getText().trim();
            if (msg.isEmpty())
                return;
            String youLine = "You: " + msg;
            chatHistory.getItems().add(youLine);
            undoStack.push(youLine); // push to Stack

            int idx = chatHistory.getItems().size();
            chatHistory.getItems().add(currentBotName + ": Thinking...");
            inputArea.clear();

            client.sendMessage(msg, reply -> {
                chatHistory.getItems().remove(idx);
                chatHistory.getItems().add(currentBotName + ": " + reply);
                historyManager.save(chatHistory);
            });
        };

        sendBtn.setOnAction(e -> send.run());
        inputArea.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER && e.isControlDown()) {
                e.consume();
                send.run();
            }
        });
    }

    /**
     * Updates the bot name everywhere in the panel instantly
     * when the user changes it in the Settings dialog.
     * 
     * ISSUE:
     * Doesn't update instantly, but when the site is refreshed / reopened
     * after name change is made.
     * 
     * @param newName the new name the user entered
     */
    public void updateBotName(String newName) {
        this.currentBotName = newName;
        titleLabel.setText("🤖 " + newName);
        inputArea.setPromptText("Message " + newName + "... (Ctrl + Enter to send)");
    }
}