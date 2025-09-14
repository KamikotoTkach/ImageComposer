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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.yaml.snakeyaml.LoaderOptions;
import ru.cwcode.tkach.imagecomposer.config.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log
public class ConfigLoaderService {
  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([\\w.]+)}");
  protected final ObjectMapper mapper;
  protected final String basedir;
  
  public ConfigLoaderService(String basedir) {
    this.basedir = basedir;
    
    LoaderOptions loaderOptions = new LoaderOptions();
    loaderOptions.setCodePointLimit(100 * 1024 * 1024);
    
    mapper = new ObjectMapper(YAMLFactory.builder()
                                         .disable(YAMLGenerator.Feature.SPLIT_LINES)
                                         .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                                         .loaderOptions(loaderOptions)
                                         .build());
    
    mapper.findAndRegisterModules();
    mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
    mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
    mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
  }
  
  @SneakyThrows
  public <T> T loadConfig(String fileName, Class<T> type) {
    Path path = Path.of(basedir, fileName);
    
    if (Files.notExists(path)) {
      Files.createDirectories(path.getParent());
      String defaultValue = mapper.writeValueAsString(type.getConstructor().newInstance());
      Files.writeString(path, defaultValue, StandardOpenOption.CREATE_NEW);
    }
    
    String raw = Files.readString(path);
    
    Matcher matcher = PLACEHOLDER_PATTERN.matcher(raw);
    StringBuilder result = new StringBuilder();
    
    while (matcher.find()) {
      String envKey = matcher.group(1);
      String envValue = System.getenv(envKey);
      
      matcher.appendReplacement(result, envValue != null ? Matcher.quoteReplacement(envValue) : matcher.group(0));
    }
    
    matcher.appendTail(result);
    
    return mapper.readValue(result.toString(), type);
  }
  
  @SneakyThrows
  public ComponentConfig getComponentConfig() {
    return loadConfig("components.yml", ComponentConfig.class);
  }
  
  @SneakyThrows
  public LastBuildConfig getLastBuildConfig() {
    return loadConfig("last_build.yml", LastBuildConfig.class);
  }
  
  @SneakyThrows
  public DeployConfig getDeployConfig() {
    return loadConfig("deploy.yml", DeployConfig.class);
  }
  
  @SneakyThrows
  public CredentialsConfig getCredentialsConfig() {
    return loadConfig("credentials.yml", CredentialsConfig.class);
  }
  
  @SneakyThrows
  public ImagesConfig getImagesConfig() {
    return loadConfig("images.yml", ImagesConfig.class);
  }
  
  public void setLastBuildConfig(LastBuildConfig lastBuildConfig) {
    try {
      Files.write(Path.of(basedir, "last_build.yml"), mapper.writeValueAsString(lastBuildConfig).getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING);
    } catch (IOException e) {
      log.warning("Cannot save last build config: " + e.getMessage());
    }
  }
}
