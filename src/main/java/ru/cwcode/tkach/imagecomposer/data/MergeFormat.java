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

package ru.cwcode.tkach.imagecomposer.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import java.util.function.Supplier;

public enum MergeFormat {
  YML(() -> new ObjectMapper(YAMLFactory.builder()
                                        .disable(YAMLGenerator.Feature.SPLIT_LINES)
                                        .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                                        .build())),
  JSON(ObjectMapper::new),
  PROPERTIES(JavaPropsMapper::new),
  TOML(TomlMapper::new);

  private final Supplier<ObjectMapper> mapperFactory;

  MergeFormat(Supplier<ObjectMapper> mapperFactory) {
    this.mapperFactory = mapperFactory;
  }

  public ObjectMapper newMapper() {
    return mapperFactory.get();
  }
}
