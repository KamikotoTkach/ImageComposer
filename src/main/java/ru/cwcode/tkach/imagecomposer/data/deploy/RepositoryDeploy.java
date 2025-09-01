package ru.cwcode.tkach.imagecomposer.data.deploy;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.google.cloud.tools.jib.api.Containerizer;
import com.google.cloud.tools.jib.api.RegistryImage;
import lombok.SneakyThrows;
import ru.cwcode.tkach.imagecomposer.data.CredentialData;

public class RepositoryDeploy implements Deploy {
  @JsonUnwrapped
  CredentialData credentialData;
  
  @SneakyThrows
  @Override
  public Containerizer containerizer(String image) {
    RegistryImage registryImage = RegistryImage.named(image);
    if (credentialData.getUsername() != null && credentialData.getPassword() != null) registryImage.addCredential(credentialData.getUsername(), credentialData.getPassword());
    
    return Containerizer.to(registryImage);
  }
}
