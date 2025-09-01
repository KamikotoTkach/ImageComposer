package ru.cwcode.tkach.imagecomposer.service;

import lombok.RequiredArgsConstructor;
import ru.cwcode.tkach.imagecomposer.config.CredentialsConfig;
import ru.cwcode.tkach.imagecomposer.data.CredentialData;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class CredentialsService {
  final CredentialsConfig credentialsConfig;
  
  public Optional<CredentialData> getCredentialByImage(String image) {
    for (Map.Entry<String, CredentialData> entry : credentialsConfig.getCredentials().entrySet()) {
      if (image.startsWith(entry.getKey())) return Optional.ofNullable(entry.getValue());
    }
    
    return Optional.empty();
  }
}
