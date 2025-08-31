package ru.cwcode.tkach.servercomposer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import ru.cwcode.tkach.servercomposer.config.ServersConfig;

@RequiredArgsConstructor
@Log
public class ServerBuilderService {
  final ServersConfig serversConfig;
  final ImageBuilderService imageBuilderService;
  
  public void build() {
    log.info("Building servers");
    
    serversConfig.getServers().forEach(imageBuilderService::build);
  }
}
