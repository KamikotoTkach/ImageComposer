package ru.cwcode.tkach.servercomposer.service;

import com.google.cloud.tools.jib.api.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import ru.cwcode.tkach.servercomposer.config.CredentialsConfig;
import ru.cwcode.tkach.servercomposer.config.DeployConfig;
import ru.cwcode.tkach.servercomposer.data.Component;
import ru.cwcode.tkach.servercomposer.data.ComponentItem;
import ru.cwcode.tkach.servercomposer.data.Server;
import ru.cwcode.tkach.servercomposer.data.deploy.Deploy;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Log
public class ImageBuilderService {
  final DeployConfig deployConfig;
  final DependencyResolverService dependencyResolverService;
  final CredentialsService credentialsService;
  final String basedir;
  
  @SneakyThrows
  public void build(String image, Server server) {
    log.info("Building server " + image);
    
    Set<Component> components = dependencyResolverService.resolve(server);
    
    JibContainerBuilder builder;
    if (server.getImage().startsWith("local/")) {
      builder = Jib.from(DockerDaemonImage.named(server.getImage().substring("local/".length())));
    } else {
      RegistryImage registryImage = RegistryImage.named(ImageReference.parse(server.getImage()));
      
      credentialsService.getCredentialByImage(server.getImage()).ifPresent(credentialData -> {
        registryImage.addCredential(credentialData.getUsername(), credentialData.getPassword());
      });
      
      builder = Jib.from(registryImage);
    }
    
    builder
      .setEnvironment(server.getEnv())
      .setEntrypoint(server.getEntrypoint())
      .setCreationTime(Instant.now())
      .setExposedPorts(Ports.parse(server.getExpose()));
    
    List<ComponentItem> files = components.stream()
                                          .flatMap(x -> x.getItems().stream())
                                          .sorted(Comparator.comparingInt(ComponentItem::getOrder))
                                          .toList();
    
    for (ComponentItem file : files) {
      builder.addLayer(List.of(Path.of(basedir, file.getFrom())), file.getTo());
    }
    
    Deploy deploy = deployConfig.getDeploys().get(server.getDeploy());
    
    JibContainer container = builder.containerize(deploy.containerizer(image)
                                                        .addEventHandler(LogEvent.class, logEvent -> System.out.println(logEvent.getLevel() + ": " + logEvent.getMessage())));
    
    if (container.isImagePushed()) {
      log.info("Server %s built".formatted(image));
    } else {
      log.warning("Cant built server %s".formatted(image));
    }
  }
}
