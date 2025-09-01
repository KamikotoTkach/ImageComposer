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
import ru.cwcode.tkach.imagecomposer.config.CredentialsConfig;
import ru.cwcode.tkach.imagecomposer.data.CredentialData;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class CredentialsService {
  final CredentialsConfig credentialsConfig;
  
  public Optional<CredentialData> getCredentialByImage(String image) {
    for (Map.Entry<String, CredentialData> entry : credentialsConfig.getCredentials().entrySet()) {
      if (image.startsWith(entry.getKey())) return Optional.ofNullable(entry.getValue());
    }
    
    return Optional.empty();
  }
}
