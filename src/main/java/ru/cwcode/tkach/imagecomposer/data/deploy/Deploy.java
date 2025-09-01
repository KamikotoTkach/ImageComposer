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
