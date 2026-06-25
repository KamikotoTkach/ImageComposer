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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.cwcode.tkach.imagecomposer.data.ComponentItem;
import ru.cwcode.tkach.imagecomposer.data.MergeFormat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

public class ConfigMergeService {

  public byte[] merge(List<ComponentItem> sortedItems, MergeFormat format, String basedir) throws IOException {
    ObjectMapper mapper = format.newMapper();
    JsonNode accumulator = null;

    for (ComponentItem item : sortedItems) {
      JsonNode node = readNode(mapper, item, basedir);

      if (item.getMerge() == null) {
        accumulator = node;
      } else {
        accumulator = accumulator == null ? node : deepMerge(accumulator, node);
      }
    }

    if (accumulator == null || accumulator.isMissingNode()) {
      accumulator = mapper.createObjectNode();
    }

    return mapper.writeValueAsBytes(accumulator);
  }

  public JsonNode deepMerge(JsonNode base, JsonNode patch) {
    if (base == null || base.isMissingNode() || base.isNull()) return patch;
    if (patch == null || patch.isMissingNode()) return base;

    if (base.isObject() && patch.isObject()) {
      ObjectNode baseObject = (ObjectNode) base;
      Iterator<String> fields = patch.fieldNames();

      while (fields.hasNext()) {
        String field = fields.next();
        baseObject.set(field, deepMerge(baseObject.get(field), patch.get(field)));
      }

      return baseObject;
    }

    return patch;
  }

  private JsonNode readNode(ObjectMapper mapper, ComponentItem item, String basedir) throws IOException {
    String text = item.getContent() != null
      ? item.getContent()
      : Files.readString(Path.of(basedir, item.getFrom()));

    return mapper.readTree(text);
  }
}
