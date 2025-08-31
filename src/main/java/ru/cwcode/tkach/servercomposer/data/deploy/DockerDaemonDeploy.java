package ru.cwcode.tkach.servercomposer.data.deploy;

import com.google.cloud.tools.jib.api.Containerizer;
import com.google.cloud.tools.jib.api.DockerDaemonImage;
import lombok.SneakyThrows;

public class DockerDaemonDeploy implements Deploy {
  @Override
  @SneakyThrows
  public Containerizer containerizer(String image) {
    return Containerizer.to(DockerDaemonImage.named(image));
  }
}
