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

    public OpenClawClient() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        loadApiUrlFromSettings();
    }

    private void loadApiUrlFromSettings() {
        Preferences prefs = Preferences.userNodeForPackage(OpenClawUI.ui.MainWindow.class);
        this.apiUrl = prefs.get("apiUrl", "http://127.0.0.1:18789/v1/chat/completions");
    }

    public void reloadSettings() {
        loadApiUrlFromSettings();
    }

    public void sendMessage(String message, java.util.function.Consumer<String> callback) {
        new Thread(() -> {
            try {
                String json = """
                        {
                          "messages": [
                            {"role": "user", "content": "%s"}
                          ]
                        }
                        """.formatted(message.replace("\"", "\\\""));

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(30))
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                String reply = (response.statusCode() >= 200 && response.statusCode() < 300)
                        ? parseClawResponse(response.body())
                        : "Error: Server returned status " + response.statusCode();

                Platform.runLater(() -> callback.accept(reply));

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> callback.accept("❌ Connection Error: Check token and endpoint in Settings"));
            }
        }).start();
    }

    public static boolean testConnection(String testUrl) {
        try (HttpClient testClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()) {
            String json = """
                    {
                      "messages": [
                        {"role": "user", "content": "test"}
                      ]
                    }
                    """;

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(testUrl))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(8))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> resp = testClient.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() >= 200 && resp.statusCode() < 300;
        } catch (Exception e) {
            return false;
        }
    }

    private String parseClawResponse(String rawResponse) {
        return rawResponse != null ? rawResponse.trim() : "No response from Claw Bot.";
    }
}