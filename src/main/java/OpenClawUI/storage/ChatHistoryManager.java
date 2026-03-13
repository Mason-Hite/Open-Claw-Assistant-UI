package OpenClawUI.storage;

import javafx.scene.control.ListView;
import java.io.*;

/**
 * Handles saving and loading chat history.
 * 
 * I used a simple text file in the user's AppData folder instead of
 * putting it in the working Folder. This way the history belongs to the app
 * itself, not to any specific project folder.
 * 
 * This is a lightweight, simple solution. Could add multi-convo history or a
 * way to have multiple convos in the future.
 */
public class ChatHistoryManager {

    private final File historyFile;

    /**
     * Creates the app's private storage folder in AppData\Roaming\OpenClawUI and
     * sets up the chat-history.txt file.
     * 
     * This folder is "owned" by the application so chat history stays even if the
     * user changes their Working Folder.
     */
    public ChatHistoryManager() {
        String appData = System.getenv("APPDATA");
        if (appData == null)
            appData = System.getProperty("user.home") + "\\AppData\\Roaming";
        File appFolder = new File(appData, "OpenClawUI");
        appFolder.mkdirs(); // create folder if it doesn't exist yet

        this.historyFile = new File(appFolder, "chat-history.txt");
    }

    /**
     * Saves the current chat history to disk.
     * Called automatically after every message.
     * 
     * @param chatHistory the ListView containing the full conversation
     */
    public void save(ListView<String> chatHistory) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(historyFile))) {
            for (String line : chatHistory.getItems())
                writer.println(line);
        } catch (IOException e) {
        }
    }

    /**
     * Loads previously saved chat history when the app starts.
     * 
     * @param chatHistory the ListView to put saved messages
     */
    public void load(ListView<String> chatHistory) {
        if (!historyFile.exists())
            return;
        try (BufferedReader reader = new BufferedReader(new FileReader(historyFile))) {
            chatHistory.getItems().clear();
            String line;
            while ((line = reader.readLine()) != null)
                chatHistory.getItems().add(line);
        } catch (IOException e) {
        }
    }

    /**
     * Deletes the saved chat history file.
     * Called when the user clicks "Clear Chat".
     */
    public void clear() {
        if (historyFile.exists())
            historyFile.delete();
    }
}