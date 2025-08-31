package ru.cwcode.tkach.servercomposer.data;

import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
public class Server {
  String image;
  Set<String> components = Set.of();
  Map<String, String> env = Map.of();
  List<String> entrypoint;
  List<String> expose = List.of();
  String deploy;
}
