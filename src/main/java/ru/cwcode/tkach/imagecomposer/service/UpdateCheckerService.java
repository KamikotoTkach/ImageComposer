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
import ru.cwcode.tkach.imagecomposer.config.LastBuildConfig;
import ru.cwcode.tkach.imagecomposer.data.Component;
import ru.cwcode.tkach.imagecomposer.data.ComponentItem;
import ru.cwcode.tkach.imagecomposer.data.Image;

import java.io.File;
import java.nio.file.Path;

@RequiredArgsConstructor
public class UpdateCheckerService {
  final DependencyResolverService dependencyResolverService;
  final LastBuildConfig lastBuildConfig;
  final String workingDirectory;
  
  public void updateBuildTime(String name) {
    lastBuildConfig.getLastBuild().put(name, System.currentTimeMillis());
  }
  
  public boolean isUpdated(String name, Image image) {
    Long lastUpdate = lastBuildConfig.getLastBuild().getOrDefault(name, 0L);
    
    for (Component component : dependencyResolverService.resolve(image)) {
      for (ComponentItem item : component.getItems()) {
        File file = Path.of(workingDirectory).resolve(item.getFrom()).toFile();
        if (file.lastModified() > lastUpdate) return true;
      }
    }
    
    return false;
  }
}
