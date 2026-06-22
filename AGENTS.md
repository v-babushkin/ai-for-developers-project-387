# AGENTS.md

Контекст проекта для AI-агентов и разработчиков. Держать в актуальном
состоянии при изменении архитектуры, стека или соглашений.

## Что за проект

Сервис бронирования встреч — MVP, аналог cal.com. Владелец календаря задаёт
типы событий, гость без регистрации бронирует свободный слот.

### Роли (без аутентификации)

- **Владелец (owner)** — один заранее заданный профиль (сид в БД), по умолчанию
  используется в админской части. Логина нет.
- **Гость (guest)** — бронирует слоты без аккаунта и без входа.

### Функциональность MVP

Владелец:
- Создаёт типы событий: `id`, название, описание, длительность в минутах.
- Просматривает страницу предстоящих встреч — единый список бронирований всех
  типов событий.

Гость:
- Видит список типов брони (название, описание, длительность).
- Выбирает тип события, открывает календарь, выбирает свободный слот в
  ближайшие 14 дней.
- Создаёт бронирование на выбранный слот.

### Бизнес-правила (инварианты)

- **Окно записи:** доступные слоты формируются на **14 дней** начиная с текущей
  даты. Записаться можно только на свободный слот из этого окна.
- **Правило занятости:** на одно и то же время нельзя создать две записи —
  **даже если это разные типы событий**. Пересечение проверяется глобально по
  владельцу.
- **Шаг слотов = длительность типа события** (30-мин тип → 09:00, 09:30…;
  60-мин → 09:00, 10:00…). Слот не должен выходить за конец рабочего окна.
- **Доступность владельца** — фиксированное еженедельное расписание (сид,
  например Пн–Пт 09:00–17:00). Прошедшие слоты текущего дня отсекаются.

## Технологический стек

- **Контракт API:** TypeSpec → OpenAPI 3.0 (источник истины для API).
- **Backend:** Java + Spring Boot, сборка Gradle, Spring Data JPA.
- **БД:** PostgreSQL, миграции через Flyway.
- **Frontend:** React + Vite + TypeScript, UI на **shadcn/ui** (Tailwind),
  роутинг — React Router, data-fetching — TanStack Query.
- **API-клиент фронта:** **openapi-typescript** (типы из контракта) +
  **openapi-fetch** (typesafe-клиент).
- **Mock API в разработке:** **Prism** (mock-сервер из `openapi.yaml`).
- **Release automation:** **release-please** (GitHub Actions) — авто-релизы по Conventional Commits.
- **Аутентификация:** отсутствует.
- **Внешние календари / уведомления:** не входят в MVP.

### Архитектура (decoupled)

Фронтенд — **отдельное приложение**. Получает данные и выполняет действия
**только через API по контракту** (`openapi.yaml`). Должен корректно работать с
**отдельно запущенным бэкендом**. Прямого доступа к БД/коду бэкенда у фронта нет.

- В разработке фронт ходит на **Prism** (mock, `http://localhost:4010`).
- В интеграции фронт ходит на **реальный бэкенд** (`http://localhost:8080`).
- Переключение — через переменную окружения Vite `VITE_API_BASE_URL`.

### Окружение

- **Node.js:** v24.16.0 (LTS), npm 11.13.0. TypeSpec 1.13.0 требует Node `>=22`.
- **ОС разработки:** Windows, PowerShell.

## Структура репозитория

```
.
├── api-spec/                 контракт API на TypeSpec (ГОТОВО)
│   ├── main.tsp              модели + операции
│   ├── tspconfig.yaml        эмиттер @typespec/openapi3 -> openapi.yaml
│   ├── package.json          зависимости TypeSpec 1.13.0
│   └── tsp-output/
│       └── openapi.yaml      сгенерированный контракт (артефакт, коммитится)
├── frontend/                 React + Vite + TS, shadcn/ui (ГОТОВО)
│   ├── src/
│   │   ├── api/              client.ts (openapi-fetch), hooks.ts (TanStack Query), schema.d.ts (openapi-typescript), types.ts
│   │   ├── app/              router.tsx (React Router), RootLayout.tsx
│   │   ├── components/ui/    shadcn/ui: button, card, input, label, table, sonner, badge, skeleton, textarea
│   │   ├── lib/              utils.ts (cn), datetime.ts (форматирование ru-RU)
│   │   └── pages/            EventTypesPage, BookingPage, BookingConfirmPage, admin/AdminEventTypesPage, admin/AdminBookingsPage, NotFoundPage
│   ├── .env.development      VITE_API_BASE_URL=http://localhost:4010 (Prism)
│   └── .env.production       VITE_API_BASE_URL=http://localhost:8080 (backend)
├── backend/                  Spring Boot (ГОТОВО)
│   └── gradle.properties     версия проекта (обновляется release-please)
├── release-please-config.json        конфиг release-please (single-package)
├── .release-please-manifest.json     манифест текущей версии
├── AGENTS.md
└── README.md
```

## Frontend (frontend) — готово

Отдельное SPA на Vite + React + TS. Все запросы — через typesafe-клиент,
сгенерированный из контракта. Переключение mock/real через `VITE_API_BASE_URL`.

Особенность на Windows: Vite привязывается к `127.0.0.1` (IPv4) — см.
`host: '127.0.0.1'` в `vite.config.ts`. Без этого браузер не достучится,
если localhost резолвится в IPv4.

### Команды (выполнять в `frontend/`)

```
npm install        # установка зависимостей
npm run gen:api    # openapi-typescript из ../api-spec/tsp-output/openapi.yaml -> src/api/schema.d.ts
npm run mock       # Prism mock на http://127.0.0.1:4010
npm run dev        # Vite dev-сервер (:5173)
npm run dev:mock   # параллельно: mock + dev (run-p)
npm run build      # production-сборка
```

### Структура

- `src/api/schema.d.ts` — сгенерированные типы (openapi-typescript).
- `src/api/client.ts` — openapi-fetch, `baseUrl` из `VITE_API_BASE_URL`.
- `src/api/hooks.ts` — TanStack Query хуки (`useEventTypes`, `useSlots`, `useCreateBooking` и др.).
- `src/components/ui/` — компоненты shadcn/ui (button, card, input, label, table, sonner, badge, skeleton, textarea).
- `src/pages/` — экраны: список типов, бронирование, подтверждение, админка.
- `src/lib/datetime.ts` — форматирование дат/времени (ru-RU), группировка слотов по дням.
- `.env.development` → Prism (`:4010`), `.env.production` → backend (`:8080`).

## Backend (backend) — в работе

Spring Boot 3.5 + Gradle, **in-memory** хранилище (без БД). Все операции —
только через Gradle.

### Переменные окружения

Перед любой Gradle-командой обязательно устанавливать:

```
$env:JAVA_HOME="C:\Users\Vitalii\.jdks\corretto-17.0.5"
```

### Команды (выполнять в `backend/`)

```
gradle build          # сборка проекта (bootJar + тесты)
gradle bootRun        # запуск dev-сервера на :8080
gradle bootJar        # сборка исполняемого JAR
gradle test           # запуск тестов
```

Требуется локально установленный Gradle (в системе найден **Gradle 9.1.0**).

### Особенности

- Хранилище — `ConcurrentHashMap` в сервисах, данные сбрасываются при рестарте.
- Расписание владельца (доступность) — хардкод Пн–Пт 09:00–17:00 UTC.
- Владелец — один, id=1, жёстко задан.
- CORS настроен на `http://localhost:5173` (Vite dev).
- При пересечении броней возвращается `409` с `Error.code = "SLOT_TAKEN"`.
- **Нюанс PowerShell + curl:** JSON в `-d` через одинарные кавычки ломает экранирование.
  Всегда передавать тело запроса через файл:
  ```powershell
  '{"title":"test"}' | Out-File -Encoding ascii body.json
  curl.exe -d "@body.json" -H "Content-Type: application/json" ...
  ```

### Структура

- `src/main/java/com/calbooking/model/` — доменные модели (EventType, Booking, Slot, BookingStatus)
- `src/main/java/com/calbooking/dto/` — Request/Response DTO
- `src/main/java/com/calbooking/repository/` — in-memory репозитории
- `src/main/java/com/calbooking/service/` — бизнес-логика (EventTypeService, SlotService, BookingService)
- `src/main/java/com/calbooking/controller/` — REST-контроллеры (guest + admin)
- `src/main/java/com/calbooking/exception/` — исключения + @ControllerAdvice
- `src/main/java/com/calbooking/config/` — CORS-конфигурация

## Контракт API (api-spec)

`main.tsp` — источник истины. После правок **обязательно** пересобрать OpenAPI.

### Команды (выполнять в `api-spec/`)

```
npm install        # установка зависимостей
npm run build      # tsp compile .  -> tsp-output/openapi.yaml
npm run watch      # пересборка в режиме наблюдения
npm run clean      # проверка без эмита
```

### Модели

`EventType`, `EventTypeCreate`, `Slot`, `Booking`, `BookingCreate`,
`BookingStatus` (CONFIRMED / CANCELLED), `Error`.

### Эндпоинты

Гость:
- `GET  /api/event-types` — активные типы событий
- `GET  /api/event-types/{id}` — детали типа (404)
- `GET  /api/event-types/{id}/slots` — свободные слоты на 14 дней (404)
- `POST /api/bookings` — создать бронь (201 / 400 / 404 / **409** при пересечении)
- `GET  /api/bookings/{id}` — детали брони (404)

Владелец (админка, без auth):
- `GET  /api/admin/event-types` — все типы, включая неактивные
- `POST /api/admin/event-types` — создать тип события (201 / 400)
- `GET  /api/admin/bookings` — единый список встреч всех типов, по времени

### Соглашения контракта

- Время — `utcDateTime` (ISO-8601, UTC). Интервал слота полуоткрытый `[start, end)`.
- Занятый слот → ответ `409` с `Error.code = "SLOT_TAKEN"`.
- Версия TypeSpec 1.x: object value через `#{ ... }` (`@service(#{...})`,
  `@info(#{...})`). Пакет `@typespec/rest` не используется.

## Доменная модель (PostgreSQL) — план backend

- **owner** — `id`, `name`, `email`, `timezone` (1 запись, сид).
- **event_type** — `id`, `owner_id`, `title`, `description`, `duration_minutes`,
  `active`.
- **availability_rule** — `id`, `owner_id`, `day_of_week`, `start_time`,
  `end_time` (сид: Пн–Пт 09:00–17:00).
- **booking** — `id`, `event_type_id`, `guest_name`, `guest_email`,
  `start_time`, `end_time`, `status`, `created_at`.

Защита инварианта занятости: транзакционная проверка пересечений + БД-уровень
`EXCLUDE USING gist` по `tstzrange(start_time, end_time)` для броней владельца.

## Соглашения по коду и процессу

- **Контракт-первый подход:** изменения API начинаются с `main.tsp`, затем
  пересборка OpenAPI, затем синхронизация backend и frontend.
- **Git / коммиты:** Conventional Commits, сообщения на русском
  (например: `feat(api-spec): ...`, `chore: ...`).
  Коммитить/пушить только по явному запросу. Все изменения — только в
  отдельных feature-ветках. Никаких коммитов и пушей напрямую в `main`.
- **Версионирование:** автоматическое через **release-please** (GitHub Actions).
  Единая версия для всего репозитория (single-package). При push в `main`
  release-please создаёт Release PR с обновлением версий в файлах:
  `api-spec/package.json`, `frontend/package.json`, `backend/gradle.properties`.
  После merge Release PR создаётся GitHub Release с тегом `v*`.
- **Игнорируется git'ом:** `.idea/`, `node_modules/`, `package-lock.json`
  (см. корневой `.gitignore`).
- **Файловые операции на Windows/PowerShell:** пути со спецсимволами заключать в
  кавычки.

## Порядок реализации (roadmap)

Контракт-первый подход. Фронтенд разрабатывается раньше бэкенда против Prism
(mock из контракта), затем добавляется бэкенд и выполняется стыковка.

1. ✅ TypeSpec-контракт → OpenAPI (`api-spec/`).
2. ✅ Frontend init: Vite + React + TS, Tailwind + shadcn/ui, React Router,
   TanStack Query.
3. ✅ API-слой фронта: `gen:api` (openapi-typescript) + `client.ts`
   (openapi-fetch) + env-переключение + скрипт Prism `mock`.
4. ✅ Экраны фронта (список типов, бронирование, подтверждение, админка) —
   разработка против Prism (`:4010`).
5. ✅ Backend init: Spring Boot (Gradle) + in-memory хранилище (без БД).
   — Model, DTO, Repository, Service, Controller, CORS, error handling.
6. ⬜ PostgreSQL + Flyway: схема и сид владельца/расписания
   (замена in-memory на постоянное хранение).
7. ⬜ JPA-сущности и репозитории (вместо ConcurrentHashMap).
8. ⬜ Интеграция фронта с реальным бэкендом (`VITE_API_BASE_URL` → `:8080`),
    сквозная проверка инварианта занятости.
9. ✅ Обновить README (запуск api-spec / frontend / prism / backend).
10. ✅ Release-please: GitHub Actions workflow + конфиг + манифест.
