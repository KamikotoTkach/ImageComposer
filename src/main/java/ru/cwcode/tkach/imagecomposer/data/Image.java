package ru.cwcode.tkach.imagecomposer.data;

import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
public class Image {
  String image;
  Set<String> components = Set.of();
  Map<String, String> env = Map.of();
  List<String> entrypoint;
  List<String> expose = List.of();
  String deploy;
}
