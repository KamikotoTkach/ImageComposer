package ru.cwcode.tkach.imagecomposer.config;

import lombok.Getter;
import ru.cwcode.tkach.imagecomposer.data.CredentialData;

import java.util.HashMap;
import java.util.Map;

@Getter
public class CredentialsConfig {
  Map<String, CredentialData> credentials = new HashMap<>();
}
