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

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DependencyResolverService {
  final ComponentConfig componentConfig;
  
  public Set<Component> resolve(Set<Component> dependencies) {
    Set<Component> resolvedDependencies = dependencies.stream()
                                                      .flatMap(d -> d.getDependencies().stream())
                                                      .map(e -> componentConfig.getComponents().get(e))
                                                      .collect(Collectors.toCollection(HashSet::new));
    resolvedDependencies.addAll(dependencies);
    
    if (dependencies.containsAll(resolvedDependencies)) return resolvedDependencies;
    
    return resolve(resolvedDependencies);
  }
  
  public Set<Component> resolve(Image image) {
    return resolve(image.getComponents().stream().map(e -> componentConfig.getComponents().get(e)).collect(Collectors.toSet()));
  }
}
