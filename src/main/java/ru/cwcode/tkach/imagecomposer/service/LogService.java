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

package ru.cwcode.tkach.imagecomposer.service;

import lombok.RequiredArgsConstructor;
import ru.cwcode.tkach.imagecomposer.config.NotifyConfig;
import ru.cwcode.tkach.imagecomposer.data.notify.TelegramNotify;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class LogService {
  final Logger logger = Logger.getLogger("ImageComposer");
  final NotifyConfig notifyConfig;
  Map<Level, String> prefix = Map.of(Level.INFO, " ",
                                     Level.WARNING, "-",
                                     Level.FINE, "+");
  
  List<String> logs = new ArrayList<>();
  
  public void log(Level level, String log) {
    logger.log(level, log);
    logs.add(prefix.getOrDefault(level, " ") + log);
  }
  
  public void send() {
    TelegramNotify notify = notifyConfig.getNotify();
    if (notify == null) return;
    try {
      notify.send(logs);
    } catch (Exception e) {
    
    }
  }
}
