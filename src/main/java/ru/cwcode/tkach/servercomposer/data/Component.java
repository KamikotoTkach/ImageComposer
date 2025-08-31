package ru.cwcode.tkach.servercomposer.data;

import lombok.Getter;

import java.util.List;
import java.util.Set;

@Getter
public class Component {
  List<ComponentItem> items = List.of();
  Set<String> dependencies = Set.of();
}
