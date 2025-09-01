package ru.cwcode.tkach.imagecomposer.service;

import com.google.cloud.tools.jib.api.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import ru.cwcode.tkach.imagecomposer.config.DeployConfig;
import ru.cwcode.tkach.imagecomposer.data.Component;
import ru.cwcode.tkach.imagecomposer.data.ComponentItem;
import ru.cwcode.tkach.imagecomposer.data.Image;
import ru.cwcode.tkach.imagecomposer.data.deploy.Deploy;

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
  public void build(String targetImage, Image image) {
    log.info("Building image " + targetImage);
    
    Set<Component> components = dependencyResolverService.resolve(image);
    
    JibContainerBuilder builder;
    if (image.getImage().startsWith("local/")) {
      builder = Jib.from(DockerDaemonImage.named(image.getImage().substring("local/".length())));
    } else {
      RegistryImage registryImage = RegistryImage.named(ImageReference.parse(image.getImage()));
      
      credentialsService.getCredentialByImage(image.getImage()).ifPresent(credentialData -> {
        registryImage.addCredential(credentialData.getUsername(), credentialData.getPassword());
      });
      
      builder = Jib.from(registryImage);
    }
    
    builder
      .setEnvironment(image.getEnv())
      .setEntrypoint(image.getEntrypoint())
      .setCreationTime(Instant.now())
      .setExposedPorts(Ports.parse(image.getExpose()));
    
    List<ComponentItem> files = components.stream()
                                          .flatMap(x -> x.getItems().stream())
                                          .sorted(Comparator.comparingInt(ComponentItem::getOrder))
                                          .toList();
    
    for (ComponentItem file : files) {
      builder.addLayer(List.of(Path.of(basedir, file.getFrom())), file.getTo());
    }
    
    Deploy deploy = deployConfig.getDeploys().get(image.getDeploy());
    
    JibContainer container = builder.containerize(deploy.containerizer(targetImage)
                                                        .addEventHandler(LogEvent.class, logEvent -> System.out.println(logEvent.getLevel() + ": " + logEvent.getMessage())));
    
    log.info("Image %s built".formatted(targetImage));
  }
}
