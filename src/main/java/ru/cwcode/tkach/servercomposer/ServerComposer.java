package ru.cwcode.tkach.servercomposer;

import lombok.extern.java.Log;
import ru.cwcode.tkach.servercomposer.config.ComponentConfig;
import ru.cwcode.tkach.servercomposer.config.CredentialsConfig;
import ru.cwcode.tkach.servercomposer.config.DeployConfig;
import ru.cwcode.tkach.servercomposer.config.ServersConfig;
import ru.cwcode.tkach.servercomposer.service.*;

import java.nio.file.Path;

@Log
public class ServerComposer {
  public static void main(String[] args) {
    String basedir = "";
    if (args.length == 1) basedir = args[0];
    
    log.info("Working dir: " + Path.of(basedir).toAbsolutePath());
    
    ConfigLoaderService configLoaderService = new ConfigLoaderService(basedir);
    
    ComponentConfig componentConfig = configLoaderService.getComponentConfig();
    ServersConfig serversConfig = configLoaderService.getServersConfig();
    DeployConfig deployConfig = configLoaderService.getDeployConfig();
    CredentialsConfig credentialsConfig = configLoaderService.getCredentialsConfig();
    
    CredentialsService credentialsService = new CredentialsService(credentialsConfig);
    DependencyResolverService dependencyResolverService = new DependencyResolverService(componentConfig);
    ImageBuilderService imageBuilderService = new ImageBuilderService(deployConfig, dependencyResolverService, credentialsService, basedir);
    ServerBuilderService serverBuilderService = new ServerBuilderService(serversConfig, imageBuilderService);
    
    serverBuilderService.build();
  }
}
