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
import lombok.extern.java.Log;
import ru.cwcode.tkach.imagecomposer.config.ImagesConfig;
import ru.cwcode.tkach.imagecomposer.data.Image;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

@RequiredArgsConstructor
@Log
public class BuilderService {
  final ImagesConfig imagesConfig;
  final ImageBuilderService imageBuilderService;
  final UpdateCheckerService updateCheckerService;
  final LogService logService;
  
  public void buildAll() {
    buildAll(Set.of());
  }
  
  public void buildAll(Set<String> profiles) {
    logService.log(Level.INFO, "Building images");
    
    imagesConfig.getImages().forEach((targetImage, image) -> {
      try {
        if (!matchesProfiles(image, profiles)) {
          return;
        }
        
        imageBuilderService.build(targetImage, image);
        updateCheckerService.updateBuildData(targetImage, image);
      } catch (Exception e) {
        logService.log(Level.WARNING, "Exception during image %s build: %s".formatted(targetImage, e.getMessage()));
        e.printStackTrace();
      }
    });
  }
  
  public void build(String name) {
    Image image = Optional.ofNullable(imagesConfig.getImages().get(name)).orElseThrow(() -> new RuntimeException("No such image"));
    
    imageBuilderService.build(name, image);
    
    updateCheckerService.updateBuildData(name, image);
  }
  
  public void buildUpdated() {
    buildUpdated(Set.of());
  }
  
  public void buildUpdated(Set<String> profiles) {
    imagesConfig.getImages().forEach((targetImage, image) -> {
      try {
        if (!matchesProfiles(image, profiles)) {
          return;
        }
        
        if (updateCheckerService.isUpdated(targetImage, image)) {
          imageBuilderService.build(targetImage, image);
          updateCheckerService.updateBuildData(targetImage, image);
        }
      } catch (Exception e) {
        logService.log(Level.WARNING, ("Exception during image %s build: %s".formatted(targetImage, e.getMessage())));
        e.printStackTrace();
      }
    });
  }
  
  private boolean matchesProfiles(Image image, Set<String> profiles) {
    if (profiles == null || profiles.isEmpty()) {
      return true;
    }
    
    Set<String> imageProfiles = Optional.ofNullable(image.getProfiles()).orElse(Collections.emptySet());
    return imageProfiles.stream()
                        .filter(Objects::nonNull)
                        .anyMatch(imageProfile -> profiles.stream()
                                                          .filter(Objects::nonNull)
                                                          .anyMatch(imageProfile::equalsIgnoreCase));
  }
}
