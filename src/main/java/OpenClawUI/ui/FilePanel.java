package OpenClawUI.ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.File;
import java.awt.Desktop;
import java.util.prefs.Preferences;

/**
 * FilePanel - The right-hand side of the main window.
 * 
 * This panel gives the user a simple but powerful file explorer for their
 * "Working Folder".
 * 
 * Key design choices I made:
 * - Double-click on folders navigates (feels natural like Windows Explorer)
 * - Double-click on files opens them with the default Windows program (super
 * useful for PDFs, images, Word docs, etc.)
 * - "Go Up" button and "Set Root Folder" for easy navigation
 * - Remembers the last folder using Preferences so the user doesn't have to
 * re-select it every time
 * 
 * This panel is meant to be the hub where users keep their training documents,
 * knowledge base files, etc.
 */
public class FilePanel extends VBox {

    private File currentFolder;
    private final ListView<File> fileList = new ListView<>();
    private final Label pathLabel;

    /**
     * Constructs the file explorer panel and restores the user's last used Working
     * Folder.
     *
     * @param stage the main stage (needed for the DirectoryChooser dialog)
     */
    public FilePanel(Stage stage) {
        // Load the saved working folder (or fall back to user's home folder)
        Preferences prefs = Preferences.userNodeForPackage(MainWindow.class);
        String savedPath = prefs.get("workingFolder", System.getProperty("user.home"));
        currentFolder = new File(savedPath);

        setPadding(new Insets(20));
        setStyle("""
                -fx-background-color: linear-gradient(to bottom right, #0b1a17, #0f1f1c);
                -fx-background-radius: 16;
                -fx-effect: dropshadow(three-pass-box, rgba(0, 255, 180, 0.15), 15, 0, 0, 8);
                """);

        Label title = new Label("📁 Working Folder (double-click folders or files)");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #7fffd4;");

        pathLabel = new Label("Current: " + currentFolder.getAbsolutePath());
        pathLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #aaaaaa;");

        Button setBtn = new Button("📂 Set Root Folder");
        Button upBtn = new Button("⬆️ Go Up");
        Button openBtn = new Button("🔍 Open in Explorer");

        // Custom cell factory for nice folder/file icons
        fileList.setCellFactory(l -> new ListCell<>() {
            @Override
            protected void updateItem(File f, boolean empty) {
                super.updateItem(f, empty);
                if (empty || f == null)
                    setText(null);
                else
                    setText(f.isDirectory() ? "📁 " + f.getName() : "📄 " + f.getName());
            }
        });
        fileList.setStyle("-fx-background-color: #121212; -fx-border-color: #222222;");

        // Double-click handling, the main feature of this panel
        fileList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                File selected = fileList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    if (selected.isDirectory()) {
                        currentFolder = selected;
                        pathLabel.setText("Current: " + currentFolder.getAbsolutePath());
                        refresh();
                    } else {
                        // Open the actual file with the user's default program
                        try {
                            Desktop.getDesktop().open(selected);
                        } catch (Exception ex) {
                            // Could happen with unknown file types, just logs it
                            System.err.println("Could not open file: " + selected.getName());
                        }
                    }
                }
            }
        });

        // Button actions
        upBtn.setOnAction(e -> {
            File parent = currentFolder.getParentFile();
            if (parent != null) {
                currentFolder = parent;
                pathLabel.setText("Current: " + currentFolder.getAbsolutePath());
                refresh();
            }
        });

        setBtn.setOnAction(e -> {
            DirectoryChooser dc = new DirectoryChooser();
            dc.setInitialDirectory(currentFolder);
            File sel = dc.showDialog(stage);
            if (sel != null) {
                currentFolder = sel;
                prefs.put("workingFolder", sel.getAbsolutePath());
                pathLabel.setText("Current: " + currentFolder.getAbsolutePath());
                refresh();
            }
        });

        openBtn.setOnAction(e -> {
            try {
                Desktop.getDesktop().open(currentFolder);
            } catch (Exception ignored) {
            }
        });

        getChildren().addAll(title, pathLabel, setBtn, upBtn, openBtn, fileList);
        refresh();
    }

    /**
     * Refreshes the ListView with the current folder's contents.
     * Called after navigation, changing root folder, or going up a level.
     */
    private void refresh() {
        fileList.getItems().clear();
        File[] files = currentFolder.listFiles();
        if (files != null) {
            java.util.Arrays.sort(files);
            fileList.getItems().addAll(files);
        }
    }
}