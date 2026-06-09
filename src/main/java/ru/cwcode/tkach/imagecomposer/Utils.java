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

package ru.cwcode.tkach.imagecomposer;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.stream.Stream;

public class Utils {
  public static long getPathSize(Path path) {
    try {
      if (!Files.isDirectory(path)) {
        return Files.size(path);
      }
      
      try (Stream<Path> walk = Files.walk(path)) {
        return walk
          .filter(Files::isRegularFile)
          .mapToLong(p -> {
            try {
              return Files.size(p);
            } catch (IOException e) {
              return 0L;
            }
          })
          .sum();
      }
    } catch (IOException e) {
      return 0L;
    }
  }
  
  public static class PathFilter {
    private final List<PathMatcher> include;
    private final List<PathMatcher> exclude;
    
    private PathFilter(List<PathMatcher> include, List<PathMatcher> exclude) {
      this.include = include;
      this.exclude = exclude;
    }
    
    public static PathFilter of(List<String> include, List<String> exclude) {
      return new PathFilter(toMatchers(include), toMatchers(exclude));
    }
    
    public boolean isEmpty() {
      return include.isEmpty() && exclude.isEmpty();
    }
    
    public boolean test(Path path) {
      boolean included = include.isEmpty() || include.stream().anyMatch(matcher -> matcher.matches(path));
      boolean excluded = exclude.stream().anyMatch(matcher -> matcher.matches(path));
      
      return included && !excluded;
    }
    
    private static List<PathMatcher> toMatchers(List<String> patterns) {
      if (patterns == null || patterns.isEmpty()) return List.of();
      
      return patterns.stream()
                     .map(PathFilter::toMatcher)
                     .toList();
    }
    
    private static PathMatcher toMatcher(String pattern) {
      if (pattern.startsWith("glob:") || pattern.startsWith("regex:")) {
        return FileSystems.getDefault().getPathMatcher(pattern);
      }
      
      return FileSystems.getDefault().getPathMatcher("glob:" + pattern);
    }
  }
}
