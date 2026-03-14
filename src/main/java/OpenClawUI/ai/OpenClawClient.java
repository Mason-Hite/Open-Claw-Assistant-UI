package OpenClawUI.ai;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.prefs.Preferences;
import javafx.application.Platform;

/**
 * OpenClawClient - Handles all communication with the local OpenClaw AI
 * gateway.
 * Updated to use the modern OpenAI-compatible format + token support.
 */
public class OpenClawClient {

    private final HttpClient client;
    private String apiUrl;

    /**
     * Constructor that loads the saved API URL from user preferences.
     * Creates a reusable HttpClient with a reasonable timeout, incase it just takes
     * a while to respond.
     */
    public OpenClawClient() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        loadApiUrlFromSettings();
    }

    /**
     * Loads (or reloads) the API endpoint URL from saved preferences. Called on
     * start and whenever the user changes settings.
     */
    private void loadApiUrlFromSettings() {
        Preferences prefs = Preferences.userNodeForPackage(OpenClawUI.ui.MainWindow.class);
        this.apiUrl = prefs.get("apiUrl", "http://localhost:18789/v1/chat/completions");
    }

    /**
     * Sends a user message to the local AI bot.
     * 
     * @param message  the text the user typed
     * @param callback called when a reply is received (runs on JavaFX thread)
     */
    public void sendMessage(String message, java.util.function.Consumer<String> callback) {
        new Thread(() -> {
            try {
                String escaped = message.replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r");

                String json = "{\"message\":\"" + escaped + "\"}";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(40))
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                String reply = (response.statusCode() >= 200 && response.statusCode() < 300)
                        ? parseClawResponse(response.body())
                        : "Error: Server returned status " + response.statusCode();

                Platform.runLater(() -> callback.accept(reply));

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> callback.accept("❌ Connection Error:\nCheck HTTP port in 'Connect to Bot' ?"));
            }
        }).start();
    }

    /**
     * Tests if the bot is reachable at the given URL.
     * Used by the button "Test Connection" in the Settings.
     * 
     * @param testUrl the endpoint to test
     * @return true if the bot responds successfully
     */
    public static boolean testConnection(String testUrl) {
        try (HttpClient testClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build()) {

            String testJson = "{\"message\":\"test connection\"}";

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(testUrl))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(8))
                    .POST(HttpRequest.BodyPublishers.ofString(testJson))
                    .build();

            HttpResponse<String> resp = testClient.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() >= 200 && resp.statusCode() < 300;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Simple response parser. Just uses ".trim()" to remove extra spaces at
     * beginning and end
     * 
     * Could make this smarter (JSON parsing, etc.).
     */
    private String parseClawResponse(String rawResponse) {
        return rawResponse != null ? rawResponse.trim() : "No response from Claw Bot.";
    }

    /**
     * Reloads the API URL after the user changes Settings.
     * Called from the MainWindow class after settings closes.
     */
    public void reloadSettings() {
        loadApiUrlFromSettings();
    }
}