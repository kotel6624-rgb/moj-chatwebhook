package pl.chatwebhook;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatWebhookMod implements ClientModInitializer {

    private static final String WEBHOOK_URL =
            "https://discord.com/api/webhooks/1553719063468048480/8R5jSVxpopiOOJbvOXonqfKhN9mHdw0Im41ZDphrSiVsn2u4dU9MKrvddWeZw2H0FL-E";

    private static final boolean INCLUDE_COMMANDS = false;

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "chatwebhook-sender");
        t.setDaemon(true);
        return t;
    });

    @Override
    public void onInitializeClient() {
        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            sendToDiscord(message);
            return true;
        });

        if (INCLUDE_COMMANDS) {
            ClientSendMessageEvents.ALLOW_COMMAND.register(command -> {
                sendToDiscord("/" + command);
                return true;
            });
        }
    }

    private void sendToDiscord(String message) {
        if (WEBHOOK_URL == null || WEBHOOK_URL.isBlank() || WEBHOOK_URL.contains("TU_WKLEJ")) {
            return;
        }
        EXECUTOR.submit(() -> {
            try {
                String json = "{\"content\":\"" + escapeJson(message) + "\"}";
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(WEBHOOK_URL))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .timeout(Duration.ofSeconds(10))
                        .build();
                HTTP.send(request, HttpResponse.BodyHandlers.discarding());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static String escapeJson(String s) {
        if (s.length() > 1900) {
            s = s.substring(0, 1900) + "...";
        }
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
