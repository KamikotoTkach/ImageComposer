/*
 * Docker image composer, 'ImageComposer'
 * Copyright (c) 2025. Danil Tkachenko
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * E-mail tkach@cwcode.ru
 *
 */

package ru.cwcode.tkach.imagecomposer.data.notify;

import lombok.SneakyThrows;

import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TelegramNotify {
  private boolean enabled = false;
  private String token = "<enter your token>";
  private String chat = "<enter your chat>";
  
  @SneakyThrows
  public void send(List<String> data) {
    if (!enabled) return;
    
    var client = HttpClient.newHttpClient();
    var body = HttpRequest.BodyPublishers.ofString(
      "chat_id=" + chat + "&parse_mode=MarkdownV2&text=" + URLEncoder.encode("```diff\n" + String.join("\n", data) + "\n```", StandardCharsets.UTF_8)
    );
    
    var req = HttpRequest.newBuilder()
                         .uri(java.net.URI.create("https://api.telegram.org/bot" + token + "/sendMessage"))
                         .header("Content-Type", "application/x-www-form-urlencoded")
                         .POST(body)
                         .build();
    
    client.send(req, HttpResponse.BodyHandlers.ofString());
  }
}
