package OpenClawUI.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import OpenClawUI.ai.OpenClawClient;

/**
 * MainWindow - The heart of the application and JavaFX entry point.
 * 
 * This is the only class that extends Application.
 * Its job is to:
 * - Create and wire together ChatPanel + FilePanel
 * - Set up the top menu bar
 * - Manage the main Scene and Stage
 * - Act as the "orchestrator" between all panels
 * 
 * I kept it intentionally small and clean so it's easy to understand.
 */
public class MainWindow extends Application {

    private final OpenClawClient client = new OpenClawClient();
    private ChatPanel chatPanel; // kept reference so we can try to update the bot's name live

    /**
     * JavaFX lifecycle method. Called when the application starts.
     * This is where we put the entire UI.
     * 
     * @param stage the primary stage provided by the JavaFX runtime
     */
    @Override
    public void start(Stage stage) {
        // The two main panels
        chatPanel = new ChatPanel(stage, client);
        FilePanel filePanel = new FilePanel(stage);

        // Root layout using BorderPane, way simpler and flexible than I thought
        BorderPane root = new BorderPane();
        root.setCenter(new javafx.scene.control.SplitPane(chatPanel, filePanel));

        // Top menu bar
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem settingsItem = new MenuItem("Settings...");

        // When user changes settings, immediately update the bot name everywhere (If I
        // can get it too work)
        settingsItem.setOnAction(e -> {
            new SettingsDialog(stage).showAndWait();
            client.reloadSettings(); // ← THIS FIXES THE CACHE STICK
            chatPanel.updateBotName(java.util.prefs.Preferences
                    .userNodeForPackage(MainWindow.class)
                    .get("botName", "Claw Bot"));
        });

        fileMenu.getItems().add(settingsItem);
        menuBar.getMenus().add(fileMenu);
        root.setTop(menuBar);

        // Final scene setup
        Scene scene = new Scene(root, 1280, 820);
        stage.setScene(scene);
        stage.setTitle("OpenClaw UI | Local Claw Bot");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}