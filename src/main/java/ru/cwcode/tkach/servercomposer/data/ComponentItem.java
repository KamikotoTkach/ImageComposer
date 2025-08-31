package ru.cwcode.tkach.servercomposer.data;

import lombok.Getter;

@Getter
public class ComponentItem {
  String from;
  String to;
  int order = 1;
}
