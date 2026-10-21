# Запуск и проверка StockFlow

Все команды выполняются из корня клонированного репозитория.

## Требования

- JDK 21 для Maven и IDE.
- Интернет при первом скачивании Maven, зависимостей и Docker-образов.
- Docker Engine с Linux-контейнерами и Compose v2 для приложения и интеграционных тестов. Скрипт `dev.ps1 up` использует `docker compose up --wait`.

В IDE импортировать корневой `pom.xml` вместе с модулями `core` и `app`, выбрать JDK 21 и Maven Wrapper. Код использует Lombok; в IDE должна работать обработка его аннотаций.

## Тесты моделей и сервисов

```powershell
.\mvnw.cmd -B -ntp -pl core test
```

Без Docker и без запуска Spring-контекста. Для одного класса:

```powershell
.\mvnw.cmd -B -ntp -pl core '-Dtest=TestTicketOrderServices' test
```

В Linux/macOS вместо `mvnw.cmd` используется `./mvnw`; перед первым запуском `chmod +x mvnw`.

## Полная проверка

Сначала запустить Docker и дождаться доступного Linux Engine:

```powershell
docker info
.\mvnw.cmd -B -ntp -Pintegration verify
```

Профиль `integration` подключает Failsafe и класс `InfrastructureIT`. Testcontainers создаёт отдельный PostgreSQL с динамическим портом; проверяются миграция, повторный запуск Liquibase и HTTP readiness приложения. Это отдельная БД, не volume из Compose.

`test` и `verify` без профиля `integration` не запускают класс `InfrastructureIT`. Недоступность Docker является ошибкой инфраструктурной проверки и не должна скрываться отключением тестов.

## Конфигурация

Шаблон `.env.example` содержит только параметры локальной разработки. Создать собственный `.env`:

```powershell
Copy-Item .env.example .env
```

На Linux/macOS: `cp .env.example .env`. Скрипт `dev.ps1` также создаёт `.env` из шаблона, только если файла ещё нет. Существующий файл не перезаписывается. `.env` исключён из Git.

| Параметр | По умолчанию | Назначение |
| --- | --- | --- |
| `DB_HOST` | `127.0.0.1` | БД при запуске Java на хосте |
| `DB_PORT` | `55432` | Опубликованный порт PostgreSQL |
| `DB_NAME` | `stockflow` | Имя локальной БД |
| `DB_USER` | `stockflow` | Локальный пользователь БД |
| `DB_PASSWORD` | `stockflow_local` | Демонстрационное локальное значение |
| `APP_PORT` | `18080` | Опубликованный порт приложения |

Compose задаёт приложению внутренние адрес `postgres:5432` и порт HTTP `8080`; с хоста используются значения таблицы. Порты Compose опубликованы только на `127.0.0.1`.

## Запуск через Docker Compose

PowerShell:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\dev.ps1 up
```

Или напрямую, в том числе Linux/macOS:

```bash
docker compose up -d --build --wait --wait-timeout 180
docker compose ps
```

Приложение доступно на `http://127.0.0.1:18080`. Проверка готовности:

```powershell
Invoke-RestMethod http://127.0.0.1:18080/actuator/health/readiness
```

Ожидается `status: UP` при работающей БД. `/` возвращает 404: бизнесовый HTTP API и пользовательский интерфейс в текущем срезе отсутствуют.

## Запуск Java на хосте

Поднять только БД и собрать приложение:

```powershell
docker compose stop app
docker compose up -d --wait postgres
.\mvnw.cmd -B -ntp package
java -jar app/target/stockflow.jar
```

В PowerShell эти действия также доступны через `dev.ps1 run`. Рабочий каталог Java-процесса — корень репозитория, чтобы Spring мог прочитать `.env`.

## Остановка и диагностика

```powershell
docker compose logs --tail 80 app postgres
docker compose down
```

`down` сохраняет volume PostgreSQL. Его удаление для обычного завершения работы не требуется.

| Симптом | Что проверить |
| --- | --- |
| `release 21 not supported` | JDK в `java -version` и настройках Maven runner |
| Docker daemon недоступен | Запущен ли Docker и выбран ли Linux Engine |
| Порт занят | `DB_PORT` или `APP_PORT` в локальном `.env` |
| Ошибка аутентификации PostgreSQL | Значения `.env` и ранее созданный volume: смена переменных не меняет существующего пользователя БД |
| Liquibase checksum mismatch | Применённые миграции и историю изменений; исправление оформляется новым changeset |
| `/` возвращает 404 | Использовать readiness endpoint; бизнесовых маршрутов пока нет |

Схемой управляет Liquibase. Hibernate настроен на `validate`; существующие миграции после применения не переписываются. Текущая схема содержит только технический маркер, бизнесовые модели ещё хранятся в памяти.
