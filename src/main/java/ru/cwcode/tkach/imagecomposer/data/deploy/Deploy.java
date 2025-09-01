package ru.cwcode.tkach.imagecomposer.data.deploy;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.cloud.tools.jib.api.Containerizer;

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = TarDeploy.class, name = "tar"),
  @JsonSubTypes.Type(value = DockerDaemonDeploy.class, name = "daemon"),
  @JsonSubTypes.Type(value = RepositoryDeploy.class, name = "repository"),
})
public interface Deploy {
  Containerizer containerizer(String image);
}
