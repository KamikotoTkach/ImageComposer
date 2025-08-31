package ru.cwcode.tkach.servercomposer.config;

import lombok.Getter;
import ru.cwcode.tkach.servercomposer.data.Server;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ServersConfig {
  Map<String, Server> servers = new HashMap<>();
}
