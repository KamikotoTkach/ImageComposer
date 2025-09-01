package ru.cwcode.tkach.imagecomposer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import ru.cwcode.tkach.imagecomposer.config.ImagesConfig;

@RequiredArgsConstructor
@Log
public class BuilderService {
  final ImagesConfig imagesConfig;
  final ImageBuilderService imageBuilderService;
  
  public void build() {
    log.info("Building images");
    
    imagesConfig.getImages().forEach((targetImage, image) -> {
      try {
        imageBuilderService.build(targetImage, image);
      } catch (Exception e) {
        log.warning("Exception during image %s build: %s".formatted(targetImage, e.getMessage()));
        e.printStackTrace();
      }
    });
  }
}
