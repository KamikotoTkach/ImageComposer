package ru.cwcode.tkach.servercomposer.data.deploy;

import com.google.cloud.tools.jib.api.Containerizer;
import com.google.cloud.tools.jib.api.TarImage;
import lombok.SneakyThrows;

import java.nio.file.Path;

public class TarDeploy implements Deploy {
  String path;
  
  @Override
  @SneakyThrows
  public Containerizer containerizer(String image) {
    return Containerizer.to(TarImage.at(Path.of(path, image)).named(image));
  }
}
