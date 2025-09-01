package ru.cwcode.tkach.imagecomposer.config;

import lombok.Getter;
import ru.cwcode.tkach.imagecomposer.data.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ComponentConfig {
  Map<String, Component> components = new HashMap<>();
}
