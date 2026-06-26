# Image Composer

Программная сборка Docker-образов через Jib Core по YAML-конфигурациям. Работает без Docker daemon и Dockerfile.

## Что делает

Берёт базовый образ, накладывает файлы, настраивает окружение и публикует в:

- Docker Registry
- Локальный Docker daemon
- TAR-архив


## Быстрый старт

### 1. Создать конфиги


**images.yml**

```yaml
images:
  my-app:latest:
    image: eclipse-temurin:17-jre
    components: [app] # из components.yml
    entrypoint: ["java", "-jar", "/app/app.jar"]
    expose: ["8080/tcp"]
    env:
      env-key: env-value
    deploy: local # из deploy.yml
```

**components.yml**

```yaml
components:
  app:
    items:
      - from: build/app.jar
        to: /app/app.jar
        order: 1
# если нет, то просто components: {}
```

**deploy.yml**

```yaml
deploys:
  # Локальная сборка
  local: !<daemon> {}
    
  # В реестр  
  registry: !<repository>
    username: "${REG_USER}"
    password: "${REG_PASS}"
    
  # В TAR
  tar: !<tar>
    path: "./dist"
```

**credentials.yml** (для приватных базовых образов)

```yaml
credentials:
  "registry.example.com/":
    username: "user"
    password: "pass"
# если нет, то просто credentials: {}
```

**last_build.yml** (для обновления образов)

```yaml
last_build: {}
```


### 2. Запуск

Собрать все образы:
```bash
java -jar image-composer.jar -d working_directory build-all
```
Собрать обновлённые образы:
```bash
java -jar image-composer.jar -d working_directory build-updated
```
Собрать образ `someImage`:
```bash
java -jar image-composer.jar -d working_directory build someImage
```

## Сценарии

### Локальная сборка

```yaml
# images.yml
images:
  my-app:latest:
    image: eclipse-temurin:17-jre
    components: [app]
    entrypoint: ["java", "-jar", "/app/app.jar"]
    expose: ["8080/tcp"]
    deploy: local
```


### Сборка в реестр

```yaml
# images.yml  
images:
  some.registry.io/my-app:latest:
    image: eclipse-temurin:17-jre
    components: [app]
    entrypoint: ["java", "-jar", "/app/app.jar"]
    expose: ["8080/tcp"]
    deploy: registry
```


### Сборка в TAR

```yaml
# images.yml
images:
  my-app:latest:
    image: eclipse-temurin:17-jre
    components: [app]
    entrypoint: ["java", "-jar", "/app/app.jar"]
    expose: ["8080/tcp"]
    deploy: tar
```

Загрузка: `docker load -i dist/my-app`

## Компоненты с зависимостями

```yaml
components:
  base-config:
    items:
      - from: config/app.yml
        to: /etc/app/config.yml
        order: 1
        
  app:
    dependencies: [base-config]
    items:
      - from: build/app.jar
        to: /app/app.jar
        order: 10
```

Зависимости разрешаются рекурсивно. `order` (опционально) - порядок добавления файла, где чем меньше - тем первее будет добавлен файл в образ. 

## Патчи конфигов (merge)

По умолчанию item копирует файл целиком, и более приоритетный item просто перезаписывает файл с тем же путём. Чтобы вместо перезаписи **слить** содержимое (изменить одну переменную, не трогая остальной файл), у item есть поле `merge` с форматом: `YML`, `JSON`, `PROPERTIES` или `TOML`.

Все item-ы, ведущие в один и тот же итоговый файл, объединяются в один слой:
- item без `merge` задаёт **базу** (полный файл);
- item с `merge` накладывается поверх как **патч**: ключи объектов сливаются рекурсивно, а скаляры и **списки заменяются целиком**;
- порядок наложения патчей определяется `order` (база применяется раньше патчей).

Базой может быть как отдельный файловый item, так и файл из **копии каталога** (`from: "configs/foo/"`): композер сам вынимает нужный файл из директорной копии под патч — отдельно исключать его (`exclude`) и переподключать не нужно.

Патч можно задать как из файла (`from`), так и **inline-текстом** через `content`. При использовании `content` поле `to` — это **полный путь к файлу** (вместе с именем); `content` всегда требует указания `merge`.

```yaml
components:
  resource_world:                  # даёт базовый файл
    items:
      - from: configs/spigot.yml
        to: /app/config            # каталог -> /app/config/spigot.yml
        order: 1

  spigot_patched_prod:             # компонент-патч
    items:
      - to: /app/config/spigot.yml # полный путь (inline content)
        merge: YML
        content: |
          settings:
            bungeecord: true
      - to: /app/config/server.properties
        merge: PROPERTIES
        from: configs/prod/extra.properties   # патч из файла
```

> Ограничение: merge накладывается поверх базового файла, предоставленного каким-либо компонентом (или заданного inline). Патчить файл, уже запечённый в базовый Docker-образ, нельзя — Jib не отдаёт его содержимое на этапе сборки.

### Патчи на уровне образа

Отдельного механизма для образа нет — патч на уровне образа делается обычным **компонентом-патчем**, который добавляется в `components` нужного образа. Так патч переиспользует профили и резолвинг зависимостей.

## Runtime-препроцессинг конфигов (`runtime`)

Иногда приложение не умеет читать переменные окружения само, а конфиг должен зависеть от окружения (один образ — много стендов). Для таких файлов есть поле `runtime: true` у item. Помеченные файлы обрабатываются **внутри контейнера при первом запуске**: плейсхолдеры в них резолвятся из env уже запущенного контейнера, после чего стартует исходный `entrypoint`.

```yaml
components:
  app_config:
    items:
      - from: configs/app.conf
        to: /app/config
        runtime: true
```

Синтаксис плейсхолдеров в самих файлах конфигов:
- `${VAR}` — значение переменной окружения (если не задана — пустая строка);
- `${VAR:default}` — значение переменной, либо `default`, если она не задана или пуста.

Поведение и ограничения:
- Подстановка выполняется **только при первом старте** контейнера. Создаётся marker-файл `/imagecomposer/.initialized`; при перезапусках конфиги не перегенерируются, поэтому ручные правки внутри контейнера сохраняются.
- Подстановку делает самодостаточный POSIX-`sh` скрипт — зависимостей вроде `envsubst`/`gettext` в базовом образе не требуется.
- Для образа с runtime-item обязателен заданный `entrypoint` (скрипт оборачивает его: `["/bin/sh", "/imagecomposer/init.sh", <исходный entrypoint...>]`).
- Служебные файлы кладутся в `/imagecomposer/` (`init.sh`, `runtime-files.list`, `.initialized`).
- `runtime` можно комбинировать с `merge`: сначала на этапе сборки файл собирается из патчей, затем при старте в собранном файле резолвятся плейсхолдеры.
- Файл должен заканчиваться переводом строки: обработка идёт построчно, и если в исходнике финального `\n` не было, он будет добавлен. Для конфигов это безопасно.

> **Важно — только внешние файлы.** Runtime-плейсхолдеры работают лишь в файлах, подключённых через `from`. Плейсхолдеры в inline-`content` (а также в любых `${...}` внутри собственных конфигов ImageComposer) резолвятся **на этапе сборки** загрузчиком конфигов и до контейнера не доживают. Если нужен runtime-плейсхолдер в патче — выносите патч в отдельный файл и подключайте через `from`.


Создан на базе [Jib Core API](https://github.com/GoogleContainerTools/jib).
