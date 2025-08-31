package ru.cwcode.tkach.servercomposer.data.deploy;

import com.google.cloud.tools.jib.api.Containerizer;
import com.google.cloud.tools.jib.api.RegistryImage;
import lombok.SneakyThrows;

public class RepositoryDeploy implements Deploy {
  String username;
  String password;
  
  @SneakyThrows
  @Override
  public Containerizer containerizer(String image) {
    RegistryImage registryImage = RegistryImage.named(image);
    if (username != null && password != null) registryImage.addCredential(username, password);
    
    return Containerizer.to(registryImage);
  }
}
