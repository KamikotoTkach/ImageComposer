package ru.cwcode.tkach.servercomposer.config;

import lombok.Getter;
import ru.cwcode.tkach.servercomposer.data.CredentialData;

import java.util.HashMap;
import java.util.Map;

@Getter
public class CredentialsConfig {
  Map<String, CredentialData> credentials = new HashMap<>();
}
