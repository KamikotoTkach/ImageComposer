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
import ru.cwcode.tkach.imagecomposer.config.ComponentConfig;
import ru.cwcode.tkach.imagecomposer.data.Component;
import ru.cwcode.tkach.imagecomposer.data.Image;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DependencyResolverService {
  final ComponentConfig componentConfig;
  
  public Map<String, Component> resolve(Map<String, Component> dependencies) {
    Map<String, Component> resolvedDependencies =
      dependencies.values().stream()
                  .flatMap(d -> d.getDependencies().stream())
                  .filter(Objects::nonNull)
                  .collect(Collectors.toMap(
                    c -> c,
                    c -> componentConfig.getComponents().get(c),
                    (a, b) -> a,
                    HashMap::new));
    
    resolvedDependencies.putAll(dependencies);
    
    if (dependencies.keySet().containsAll(resolvedDependencies.keySet())) {
      return resolvedDependencies;
    }
    
    return resolve(resolvedDependencies);
  }
  
  public Map<String, Component> resolve(Image image) {
    Map<String, Component> initial =
      image.getComponents().stream()
           .collect(Collectors.toMap(
             c -> c,
             c -> componentConfig.getComponents().get(c),
             (a, b) -> a,
             HashMap::new));
    
    return resolve(initial);
  }
}

