package ru.cwcode.tkach.servercomposer.config;

import lombok.Getter;
import ru.cwcode.tkach.servercomposer.data.deploy.Deploy;

import java.util.HashMap;
import java.util.Map;

@Getter
public class DeployConfig {
  Map<String, Deploy> deploys = new HashMap<>();
}
