#!/bin/sh
# ImageComposer runtime config preprocessor.
# Resolves ${VAR} and ${VAR:default} placeholders in marked config files
# using the container environment, on first container start only.
# Generated/injected by ImageComposer. Do not edit inside the image.

set -e

IC_DIR=/imagecomposer
MARKER="$IC_DIR/.initialized"
LIST="$IC_DIR/runtime-files.list"

# Already initialized — skip preprocessing, run the original entrypoint.
if [ -f "$MARKER" ]; then
  exec "$@"
fi

# Replace ${VAR} and ${VAR:default} in a single string using the environment.
substitute() {
  s=$1; out=''
  while case "$s" in *'${'*) true;; *) false;; esac; do
    pre=${s%%'${'*}; rest=${s#*'${'}
    expr=${rest%%'}'*}; s=${rest#*'}'}
    name=${expr%%:*}
    if [ "$name" != "$expr" ]; then def=${expr#*:}; else def=''; fi
    eval "val=\${$name:-}"
    [ -z "$val" ] && val=$def
    out=$out$pre$val
  done
  printf '%s' "$out$s"
}

if [ -f "$LIST" ]; then
  while IFS= read -r file || [ -n "$file" ]; do
    [ -z "$file" ] && continue
    if [ ! -f "$file" ]; then
      echo "imagecomposer: runtime target not found, skipping: $file" >&2
      continue
    fi
    tmp="$file.imagecomposer.tmp"
    : > "$tmp"
    while IFS= read -r line || [ -n "$line" ]; do
      substitute "$line" >> "$tmp"
      printf '\n' >> "$tmp"
    done < "$file"
    mv "$tmp" "$file"
  done < "$LIST"
fi

mkdir -p "$IC_DIR"
: > "$MARKER"

exec "$@"
