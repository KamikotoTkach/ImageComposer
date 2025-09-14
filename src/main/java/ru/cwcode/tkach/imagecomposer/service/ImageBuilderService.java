/*
 * Docker image composer, 'ImageComposer'
 * Copyright (c) 2025. Danil Tkachenko
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * E-mail tkach@cwcode.ru
 *
 */

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

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Log
@RequiredArgsConstructor
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
      Path path = Path.of(basedir, file.getFrom());
      if (!path.toFile().exists()) {
        throw new FileNotFoundException("Cannot find file " + path);
      }
      
      builder.addLayer(List.of(path), file.getTo());
    }
    
    Deploy deploy = deployConfig.getDeploys().get(image.getDeploy());
    
    JibContainer container = builder.containerize(deploy.containerizer(targetImage)
                                                        .addEventHandler(LogEvent.class, logEvent -> System.out.println(logEvent.getLevel() + ": " + logEvent.getMessage())));
    
    log.info("Image %s built".formatted(targetImage));
  }
}
