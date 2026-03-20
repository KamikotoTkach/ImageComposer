package ru.cwcode.tkach.imagecomposer.data.notify;

import lombok.SneakyThrows;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

public class TelegramNotify {
  private boolean enabled = false;
  private String token = "<enter your token>";
  private String chat = "<enter your chat>";
  
  private boolean proxyEnabled = false;
  private String proxyHost = "127.0.0.1";
  private int proxyPort = 8080;
  
  @SneakyThrows
  public void send(List<String> data) {
    if (!enabled) return;
    
    HttpClient client = proxyEnabled
      ? HttpClient.newBuilder()
                  .proxy(ProxySelector.of(new InetSocketAddress(proxyHost, proxyPort)))
                  .connectTimeout(Duration.ofMillis(3000))
                  .build()
      : HttpClient.newHttpClient();
    
    var body = HttpRequest.BodyPublishers.ofString(
      "chat_id=" + chat +
      "&parse_mode=MarkdownV2&text=" +
      URLEncoder.encode("```diff\n" + String.join("\n", data) + "\n```", StandardCharsets.UTF_8)
    );
    
    var req = HttpRequest.newBuilder()
                         .uri(java.net.URI.create("https://api.telegram.org/bot" + token + "/sendMessage"))
                         .header("Content-Type", "application/x-www-form-urlencoded")
                         .POST(body)
                         .timeout(Duration.ofMillis(10000))
                         .build();
    
    client.send(req, HttpResponse.BodyHandlers.ofString());
  }
}
