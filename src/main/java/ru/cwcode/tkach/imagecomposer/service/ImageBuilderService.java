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
import com.google.cloud.tools.jib.api.buildplan.AbsoluteUnixPath;
import com.google.cloud.tools.jib.api.buildplan.FileEntriesLayer;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import ru.cwcode.tkach.imagecomposer.Utils;
import ru.cwcode.tkach.imagecomposer.config.DeployConfig;
import ru.cwcode.tkach.imagecomposer.data.Component;
import ru.cwcode.tkach.imagecomposer.data.ComponentItem;
import ru.cwcode.tkach.imagecomposer.data.Image;
import ru.cwcode.tkach.imagecomposer.data.MergeFormat;
import ru.cwcode.tkach.imagecomposer.data.deploy.Deploy;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class ImageBuilderService {
  final DeployConfig deployConfig;
  final DependencyResolverService dependencyResolverService;
  final CredentialsService credentialsService;
  final String basedir;
  final LogService logService;
  final ConfigMergeService configMergeService = new ConfigMergeService();

  @SneakyThrows
  public void build(String targetImage, Image image) {
    String deployImage = image.getName() == null ? targetImage : image.getName();
    logService.log(Level.INFO, "Building image " + deployImage);

    Collection<Component> components = dependencyResolverService.resolve(image).values();

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

    List<ComponentItem> items = components.stream()
                                          .flatMap(c -> c.getItems().stream())
                                          .toList();

    items.forEach(this::validate);

    Map<String, List<ComponentItem>> groups = new LinkedHashMap<>();
    List<ComponentItem> plainItems = new ArrayList<>();

    for (ComponentItem item : items) {
      String target = finalTarget(item);
      if (target == null) {
        plainItems.add(item);
      } else {
        groups.computeIfAbsent(target, k -> new ArrayList<>()).add(item);
      }
    }

    Path tempDir = Files.createTempDirectory("imagecomposer-merge");
    try {
      int merged = 0;
      for (Map.Entry<String, List<ComponentItem>> entry : groups.entrySet()) {
        List<ComponentItem> group = entry.getValue();
        if (group.stream().anyMatch(i -> i.getMerge() != null)) {
          addMergedLayer(builder, entry.getKey(), group, tempDir.resolve("merged-" + (merged++)));
        } else {
          plainItems.addAll(group);
        }
      }

      addPlainLayers(builder, plainItems);

      Deploy deploy = deployConfig.getDeploys().get(image.getDeploy());

      JibContainer container = builder.containerize(deploy.containerizer(deployImage)
                                                          .setAlwaysCacheBaseImage(true)
                                                          .addEventHandler(LogEvent.class, logEvent -> {
                                                            if (logEvent.getLevel().ordinal() <= LogEvent.Level.LIFECYCLE.ordinal()) {
                                                              System.out.println(logEvent.getLevel() + ": " + logEvent.getMessage());
                                                            }
                                                          }));
    } finally {
      deleteRecursively(tempDir);
    }

    logService.log(Level.FINE, "Image %s built".formatted(deployImage));
  }

  private void validate(ComponentItem item) {
    boolean hasFrom = item.getFrom() != null;
    boolean hasContent = item.getContent() != null;

    if (hasFrom == hasContent) {
      throw new IllegalArgumentException("ComponentItem (to=" + item.getTo() + ") must define exactly one of 'from' or 'content'");
    }
    if (hasContent && item.getMerge() == null) {
      throw new IllegalArgumentException("Inline 'content' (to=" + item.getTo() + ") requires a 'merge' format");
    }
  }

  private String finalTarget(ComponentItem item) throws IOException {
    if (item.getContent() != null) {
      return AbsoluteUnixPath.get(item.getTo()).toString();
    }

    Path path = Path.of(basedir, item.getFrom());
    if (Files.isDirectory(path)) {
      if (item.getMerge() != null) {
        throw new IllegalArgumentException("merge is not supported for directory 'from': " + item.getFrom());
      }
      return null;
    }
    if (!Files.exists(path)) {
      if (item.getMerge() != null) {
        throw new FileNotFoundException("Cannot find file " + path);
      }
      return null;
    }

    return AbsoluteUnixPath.get(item.getTo()).resolve(path.getFileName().toString()).toString();
  }

  private void addMergedLayer(JibContainerBuilder builder, String target, List<ComponentItem> group, Path tempFile) throws IOException {
    Set<MergeFormat> formats = group.stream()
                                    .map(ComponentItem::getMerge)
                                    .filter(java.util.Objects::nonNull)
                                    .collect(Collectors.toSet());
    if (formats.size() > 1) {
      throw new IllegalArgumentException("Conflicting merge formats " + formats + " for target " + target);
    }
    MergeFormat format = formats.iterator().next();

    List<ComponentItem> sorted = group.stream()
                                      .sorted(Comparator.comparingInt(ComponentItem::getOrder)
                                                        .thenComparingInt(i -> i.getMerge() == null ? 0 : 1))
                                      .toList();

    byte[] content = configMergeService.merge(sorted, format, basedir);
    Files.write(tempFile, content);

    builder.addFileEntriesLayer(FileEntriesLayer.builder()
                                                .addEntry(tempFile, AbsoluteUnixPath.get(target))
                                                .build());
  }

  private void addPlainLayers(JibContainerBuilder builder, List<ComponentItem> plainItems) throws IOException {
    Comparator<ComponentItem> sortCmp = Comparator.comparingInt(ComponentItem::getOrder)
                                                  .thenComparingLong(e -> Utils.getPathSize(Path.of(basedir, e.getFrom())));

    Comparator<ComponentItem> pickCmp = Comparator.comparingInt(ComponentItem::getOrder);

    Map<String, ComponentItem> bestByTag = plainItems.stream()
                                                     .filter(i -> i.getTag() != null)
                                                     .collect(Collectors.toMap(
                                                       ComponentItem::getTag,
                                                       Function.identity(),
                                                       BinaryOperator.maxBy(pickCmp)
                                                     ));

    List<ComponentItem> files = Stream.concat(plainItems.stream().filter(i -> i.getTag() == null), bestByTag.values().stream())
                                      .sorted(sortCmp)
                                      .toList();

    for (ComponentItem file : files) {
      Path path = Path.of(basedir, file.getFrom());
      if (!path.toFile().exists()) {
        throw new FileNotFoundException("Cannot find file " + path);
      }

      addLayer(builder, file, path);
    }
  }

  private void deleteRecursively(Path dir) throws IOException {
    if (Files.notExists(dir)) return;
    try (Stream<Path> walk = Files.walk(dir)) {
      walk.sorted(Comparator.reverseOrder()).forEach(p -> {
        try {
          Files.deleteIfExists(p);
        } catch (IOException ignored) {
        }
      });
    }
  }

  private void addLayer(JibContainerBuilder builder, ComponentItem file, Path path) throws IOException {
    Utils.PathFilter pathFilter = Utils.PathFilter.of(file.getInclude(), file.getExclude());
    if (pathFilter.isEmpty()) {
      builder.addLayer(List.of(path), file.getTo());
      return;
    }
    
    FileEntriesLayer.Builder layerBuilder = FileEntriesLayer.builder();
    AbsoluteUnixPath targetPath = AbsoluteUnixPath.get(file.getTo()).resolve(path.getFileName());
    int entries = 0;
    
    if (Files.isDirectory(path)) {
      try (Stream<Path> walk = Files.walk(path)) {
        for (Path sourcePath : walk.filter(Files::isRegularFile).toList()) {
          Path relativePath = path.relativize(sourcePath);
          if (pathFilter.test(relativePath)) {
            layerBuilder.addEntry(sourcePath, targetPath.resolve(relativePath));
            entries++;
          }
        }
      }
    } else if (pathFilter.test(path.getFileName())) {
      layerBuilder.addEntry(path, targetPath.resolve(path.getFileName()));
      entries++;
    }
    
    if (entries > 0) {
      builder.addFileEntriesLayer(layerBuilder.build());
    }
  }
}
