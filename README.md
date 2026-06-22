### Hexlet tests and linter status:
[![Actions Status](https://github.com/v-babushkin/ai-for-developers-project-387/actions/workflows/hexlet-check.yml/badge.svg)](https://github.com/v-babushkin/ai-for-developers-project-387/actions)

---

**Деплой:** https://calbooking-bj3g.onrender.com

Сервис бронирования встреч, аналог cal.com.

## Быстрый старт (Frontend)

### 1. Установка зависимостей

```bash
cd frontend
npm install
```

### 2. Генерация TypeScript-типов из контракта

```bash
npm run gen:api
```

### 3. Запуск (Mock API + Dev Server)

```bash
# Вариант A — оба сервера одной командой
cd frontend
npm run dev:mock

# Вариант B — по отдельности (в разных терминалах)
npm run mock   # Prism mock-сервер на http://localhost:4010
npm run dev    # Vite dev-сервер на http://localhost:5173
```

### 4. Открыть в браузере

- http://localhost:5173/ — список типов встреч (гость)
- http://localhost:5173/book/1 — бронирование (календарь + слоты + форма)
- http://localhost:5173/booking/1 — подтверждение брони
- http://localhost:5173/admin — админка (создание типов)
- http://localhost:5173/admin/bookings — список встреч

### Остановка (Windows / PowerShell)

```powershell
# Найти процессы на портах 4010 (Prism) и 5173 (Vite)
$ports = @(4010, 5173)
foreach ($port in $ports) {
  $conn = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
  if ($conn) {
    $conn.OwningProcess | Select-Object -Unique | ForEach-Object {
      Stop-Process -Id $_ -Force
      Write-Host "Процесс на порту $port остановлен"
    }
  }
}
```

Или закрыть терминал, в котором запущены процессы (Ctrl+C в окне `npm run dev:mock`).

### Использование с реальным бэкендом

```bash
# установить VITE_API_BASE_URL=http://localhost:8080
# (указан в .env.production, подставляется при npm run build -- --mode production)
```

## Структура проекта

```
api-spec/     контракт API на TypeSpec → OpenAPI
frontend/     SPA на React + Vite + TypeScript
backend/      Spring Boot (планируется)
```

## Пересборка контракта (api-spec)

```bash
cd api-spec
npm install
npm run build   # tsp compile . → tsp-output/openapi.yaml
```