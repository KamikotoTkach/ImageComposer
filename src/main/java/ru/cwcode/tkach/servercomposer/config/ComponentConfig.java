package ru.cwcode.tkach.servercomposer.config;

import lombok.Getter;
import ru.cwcode.tkach.servercomposer.data.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ComponentConfig {
  Map<String, Component> components = new HashMap<>();
}
