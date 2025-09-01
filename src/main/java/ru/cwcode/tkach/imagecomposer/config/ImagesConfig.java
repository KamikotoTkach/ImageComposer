package ru.cwcode.tkach.imagecomposer.config;

import lombok.Getter;
import ru.cwcode.tkach.imagecomposer.data.Image;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ImagesConfig {
  Map<String, Image> images = new HashMap<>();
}
