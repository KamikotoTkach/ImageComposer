package ru.cwcode.tkach.servercomposer.service;

import lombok.RequiredArgsConstructor;
import ru.cwcode.tkach.servercomposer.config.ComponentConfig;
import ru.cwcode.tkach.servercomposer.data.Component;
import ru.cwcode.tkach.servercomposer.data.Server;

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
  
  public Set<Component> resolve(Server server) {
    return resolve(server.getComponents().stream().map(e -> componentConfig.getComponents().get(e)).collect(Collectors.toSet()));
  }
}
