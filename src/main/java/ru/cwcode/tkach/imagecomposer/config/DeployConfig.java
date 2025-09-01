package ru.cwcode.tkach.imagecomposer.config;

import lombok.Getter;
import ru.cwcode.tkach.imagecomposer.data.deploy.Deploy;

import java.util.HashMap;
import java.util.Map;

@Getter
public class DeployConfig {
  Map<String, Deploy> deploys = new HashMap<>();
}
