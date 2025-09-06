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


Создан на базе [Jib Core API](https://github.com/GoogleContainerTools/jib).
