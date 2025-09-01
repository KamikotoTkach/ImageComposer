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

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.google.cloud.tools.jib.api.Containerizer;
import com.google.cloud.tools.jib.api.RegistryImage;
import lombok.SneakyThrows;
import ru.cwcode.tkach.imagecomposer.data.CredentialData;

public class RepositoryDeploy implements Deploy {
  @JsonUnwrapped
  CredentialData credentialData;
  
  @SneakyThrows
  @Override
  public Containerizer containerizer(String image) {
    RegistryImage registryImage = RegistryImage.named(image);
    if (credentialData.getUsername() != null && credentialData.getPassword() != null) registryImage.addCredential(credentialData.getUsername(), credentialData.getPassword());
    
    return Containerizer.to(registryImage);
  }
}
