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

@RequiredArgsConstructor
@Log
public class BuilderService {
  final ImagesConfig imagesConfig;
  final ImageBuilderService imageBuilderService;
  final UpdateCheckerService updateCheckerService;
  
  public void buildAll() {
    log.info("Building images");
    
    imagesConfig.getImages().forEach((targetImage, image) -> {
      try {
        imageBuilderService.build(targetImage, image);
        updateCheckerService.updateBuildTime(targetImage);
      } catch (Exception e) {
        log.warning("Exception during image %s build: %s".formatted(targetImage, e.getMessage()));
        e.printStackTrace();
      }
    });
  }
  
  public void build(String name) {
    Image image = imagesConfig.getImages().get(name);
    
    imageBuilderService.build(name, image);
    
    updateCheckerService.updateBuildTime(name);
  }
  
  public void buildUpdated() {
    imagesConfig.getImages().forEach((targetImage, image) -> {
      try {
        if (updateCheckerService.isUpdated(targetImage, image)) {
          imageBuilderService.build(targetImage, image);
          updateCheckerService.updateBuildTime(targetImage);
        }
      } catch (Exception e) {
        log.warning("Exception during image %s build: %s".formatted(targetImage, e.getMessage()));
        e.printStackTrace();
      }
    });
  }
}
