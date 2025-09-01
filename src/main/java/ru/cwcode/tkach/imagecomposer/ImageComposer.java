package ru.cwcode.tkach.imagecomposer;

import lombok.extern.java.Log;
import ru.cwcode.tkach.imagecomposer.config.ComponentConfig;
import ru.cwcode.tkach.imagecomposer.config.CredentialsConfig;
import ru.cwcode.tkach.imagecomposer.config.DeployConfig;
import ru.cwcode.tkach.imagecomposer.config.ImagesConfig;
import ru.cwcode.tkach.imagecomposer.service.*;

import java.nio.file.Path;

@Log
public class ImageComposer {
  public static void main(String[] args) {
    String basedir = "";
    if (args.length == 1) basedir = args[0];
    
    log.info("Working dir: " + Path.of(basedir).toAbsolutePath());
    
    ConfigLoaderService configLoaderService = new ConfigLoaderService(basedir);
    
    ComponentConfig componentConfig = configLoaderService.getComponentConfig();
    ImagesConfig imagesConfig = configLoaderService.getImagesConfig();
    DeployConfig deployConfig = configLoaderService.getDeployConfig();
    CredentialsConfig credentialsConfig = configLoaderService.getCredentialsConfig();
    
    CredentialsService credentialsService = new CredentialsService(credentialsConfig);
    DependencyResolverService dependencyResolverService = new DependencyResolverService(componentConfig);
    ImageBuilderService imageBuilderService = new ImageBuilderService(deployConfig, dependencyResolverService, credentialsService, basedir);
    BuilderService builderService = new BuilderService(imagesConfig, imageBuilderService);
    
    builderService.build();
  }
}
