package ru.cwcode.tkach.imagecomposer.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import lombok.SneakyThrows;
import org.yaml.snakeyaml.LoaderOptions;
import ru.cwcode.tkach.imagecomposer.config.ComponentConfig;
import ru.cwcode.tkach.imagecomposer.config.CredentialsConfig;
import ru.cwcode.tkach.imagecomposer.config.DeployConfig;
import ru.cwcode.tkach.imagecomposer.config.ImagesConfig;

import java.nio.file.Path;

public class ConfigLoaderService {
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
  public ComponentConfig getComponentConfig() {
    return mapper.readValue(Path.of(basedir, "components.yml").toFile(), ComponentConfig.class);
  }
  
  @SneakyThrows
  public DeployConfig getDeployConfig() {
    return mapper.readValue(Path.of(basedir, "deploy.yml").toFile(), DeployConfig.class);
  }
  
  @SneakyThrows
  public CredentialsConfig getCredentialsConfig() {
    return mapper.readValue(Path.of(basedir, "credentials.yml").toFile(), CredentialsConfig.class);
  }
  
  @SneakyThrows
  public ImagesConfig getImagesConfig() {
    return mapper.readValue(Path.of(basedir, "images.yml").toFile(), ImagesConfig.class);
  }
}
