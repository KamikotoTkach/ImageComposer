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

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import ru.cwcode.tkach.imagecomposer.config.BuildDataConfig;
import ru.cwcode.tkach.imagecomposer.data.Component;
import ru.cwcode.tkach.imagecomposer.data.ComponentItem;
import ru.cwcode.tkach.imagecomposer.data.Image;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.logging.Level;

@RequiredArgsConstructor
@Log
public class UpdateCheckerService {
  final DependencyResolverService dependencyResolverService;
  final BuildDataConfig buildDataConfig;
  final String workingDirectory;
  final ConfigLoaderService configLoaderService;
  final LogService logService;
  
  @SneakyThrows
  public void updateBuildData(String name, Image image) {
    buildDataConfig.getLastBuild().put(name, System.currentTimeMillis());
    
    String imageChecksum = sha256(configLoaderService.asString(image));
    buildDataConfig.getImagesChecksums().put(name, imageChecksum);
    
    dependencyResolverService.resolve(image).forEach((s, component) -> {
      String componentChecksum = sha256(configLoaderService.asString(component));
      buildDataConfig.getComponentsChecksums().put(s, componentChecksum);
    });
  }
  
  public boolean isImageChecksumMatch(String name, Image image) {
    String checksum = buildDataConfig.getImagesChecksums().getOrDefault(name, "");
    boolean match = checksum.equals(sha256(configLoaderService.asString(image)));
    
    logService.log(Level.INFO, "Image " + name + " checksum " + (match ? "match" : "does not match"));
    
    return match;
  }
  
  public boolean isComponentChecksumMatch(String name, Component component) {
    return buildDataConfig.getComponentsChecksums().getOrDefault(name, "").equals(sha256(configLoaderService.asString(component)));
  }
  
  public boolean isUpdated(String name, Image image) {
    if (!isImageChecksumMatch(name, image)) {
      logService.log(Level.WARNING, "Image " + name + " is updated due to image declaration is updated");
      
      return true;
    }
    
    Long lastUpdate = buildDataConfig.getLastBuild().getOrDefault(name, 0L);
    
    for (var component : dependencyResolverService.resolve(image).entrySet()) {
      if (!isComponentChecksumMatch(component.getKey(), component.getValue())) {
        logService.log(Level.WARNING, "Image " + name + " is updated due to " + component.getKey() + " updated");
        return true;
      }
      
      for (ComponentItem item : component.getValue().getItems()) {
        File file = Path.of(workingDirectory).resolve(item.getFrom()).toFile();
        
        if (getLastModifiedRecursively(file) > lastUpdate) {
          logService.log(Level.WARNING, "Image " + name + " is updated due to " + file + " of " + component.getKey() + " is updated");
          
          return true;
        }
      }
    }
    
    logService.log(Level.FINE, "Image " + name + " is not updated");
    
    return false;
  }
  
  private long getLastModifiedRecursively(File file) {
    long lastModified = file.lastModified();
    
    if (file.isDirectory()) {
      File[] files = file.listFiles();
      if (files == null) return lastModified;
      
      for (File listFile : files) {
        lastModified = Math.max(lastModified, getLastModifiedRecursively(listFile));
      }
    }
    
    return lastModified;
  }
  
  @SneakyThrows
  private static String sha256(String string) {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    byte[] digest = md.digest(string.getBytes(StandardCharsets.UTF_8));
    return HexFormat.of().formatHex(digest);
  }
}
