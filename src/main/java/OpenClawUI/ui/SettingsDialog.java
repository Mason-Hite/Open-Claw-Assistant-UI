package OpenClawUI.ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.util.prefs.Preferences;
import OpenClawUI.ai.OpenClawClient;

/**
 * SettingsDialog - Popup window for configuring the bot connection.
 * 
 * Allows the user to:
 * - Change the HTTP endpoint URL
 * - Set a custom bot name
 * - Test the connection
 * - Save settings permanently using Preferences
 */
public class SettingsDialog extends Dialog<Void> {

    /**
     * Creates and displays the settings dialog.
     * 
     * @param owner the main application window (keeps dialog on top)
     */
    public SettingsDialog(Stage owner) {
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL); // blocks interaction with main window until closed
        setTitle("🔗 Connect to Your Claw Bot");
        setHeaderText("Configure your local AI bot");

        Preferences prefs = Preferences.userNodeForPackage(MainWindow.class);

        // Pre-fill with last used values (super user friendly)
        TextField apiUrlField = new TextField(
                prefs.get("apiUrl_v2", "http://192.168.137.19:18789/v1/chat/completions"));
        apiUrlField.setPrefWidth(450);

        TextField tokenField = new TextField(prefs.get("apiToken_v2", ""));
        tokenField.setPrefWidth(450);
        tokenField.setPromptText("Paste your Bearer token here");

        TextField nameField = new TextField(prefs.get("botName", "Claw Bot"));
        nameField.setPrefWidth(450);

        Button defaultBtn = new Button("Use Default Localhost");
        defaultBtn.setOnAction(e -> apiUrlField.setText("http://localhost:18789/v1/chat/completions"));

        Button testBtn = new Button("Test Connection");
        Label statusLabel = new Label("Click Test to verify connection");

        // Basic beginner button
        testBtn.setOnAction(e -> {
            String url = apiUrlField.getText().trim();
            String token = tokenField.getText().trim(); // ← ADDED
            if (OpenClawClient.testConnection(url, token)) { // ← slight update: now passes token
                statusLabel.setText("✅ Connected successfully!");
                statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            } else {
                statusLabel.setText("❌ Not connected. Is your bot running?");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.add(new Label("Bot HTTP Endpoint:"), 0, 0);
        grid.add(apiUrlField, 1, 0);
        grid.add(new Label("Bearer Token:"), 0, 1); // ← ADDED row
        grid.add(tokenField, 1, 1); // ← ADDED row
        grid.add(new Label("Bot Name:"), 0, 2);
        grid.add(nameField, 1, 2);
        grid.add(defaultBtn, 1, 3);
        grid.add(testBtn, 1, 4);
        grid.add(statusLabel, 1, 5);

        getDialogPane().setContent(grid);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Save settings when user clicks OK
        setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                prefs.put("apiUrl_v2", apiUrlField.getText().trim());
                prefs.put("apiToken_v2", tokenField.getText().trim());
                prefs.put("botName", nameField.getText().trim());
            }
            return null;
        });
    }
}