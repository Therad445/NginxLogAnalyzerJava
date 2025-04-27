# Nginx Log Analyzer

Анализатор и отчётный сервис для логов Nginx:  
– сбор метрик и выявление аномалий,  
– детекция подозрительных IP,  
– генерация отчётов в формате PDF и Markdown,  
– визуализация временных рядов.

---

## Содержание

- [Описание](#описание)
- [Ключевые возможности](#ключевые-возможности)
- [Архитектура](#архитектура)
- [Технологии](#технологии)
- [Установка и запуск](#установка-и-запуск)
- [Конфигурация](#конфигурация)
- [REST API](#rest-api)
- [Примеры использования](#примеры-использования)
- [Тестирование](#тестирование)
- [Contributing](#contributing)
- [Лицензия](#лицензия)

---

## Описание

Проект реализует конвейер обработки логов Nginx:

1. **Чтение**  
   – локального файла или из Kafka (batch/stream).
2. **Парсинг**  
   – получение домена, HTTP-метода, URL, кода ответа, размера и времени.
3. **Агрегация**  
   – разбиение на окна фиксированной длительности, вычисление метрик.
4. **Аномалия-детект**  
   – Z-score и EWMA-анализ по размеру и количеству запросов.
5. **Подозрительные IP**  
   – выявление IP с непривычно высоким трафиком.
6. **Отчёты**  
   – REST-сервис: JSON, PDF-генератор, Markdown-финал.
7. **Визуализация**  
   – временные ряды запросов и error-rate в виде PNG-графика.

---

## Ключевые возможности

- Поддержка batch- и streaming-режимов чтения логов
- Парсинг формата Nginx Combined Log
- Расчёт:
    - общего количества запросов
    - среднего размера ответа
    - 95-го перцентиля
- Две методики детекции аномалий: Z-score и EWMA
- Детекция подозрительных IP по частоте
- Генерация:
    - **JSON** результата
    - **PDF** отчёта с таблицей и графиком
    - **Markdown** отчёта
- Лёгкий Spring Boot API с тестовым ping-endpoint

---

## Архитектура

```
┌────────────┐      ┌───────────┐     ┌───────────────┐
│  Reader    │─────▶│  Parser   │────▶│ Aggregator    │
│(File/Kafka)│      │ (NginxLog)│     │ (Metrics)     │
└────────────┘      └───────────┘     └───────────────┘
       │                                 │
       ▼                                 ▼
┌───────────┐      ┌───────────────┐   ┌───────────┐
│ Anomaly   │◀─────│ AnomalySvc    │   │ Suspicious│
│ Detectors │      │ (ZScore+EWMA) │   │ IpDetector│
└───────────┘      └───────────────┘   └───────────┘
       │                     │
       └──────────┬──────────┘
                  ▼
            ┌────────────┐
            │  Report    │
            │ Generator  │
            │ PDF/MD/JSON│
            └────────────┘
```

---

## Технологии

- Java 12
- Spring Boot 3
- Apache Kafka (Reader)
- XChart (PNG-график)
- OpenPDF (iText 2.1.7 API)
- JUnit 5 + AssertJ + MockMVC
- Maven

---

## Установка и запуск

1. Клонировать репозиторий
   ```bash
   git clone https://github.com/Therad445/NginxLogAnalyzerJava.git
   cd nginx-log-analyzer
   ```
2. Собрать проект
   ```bash
   mvn clean package
   ```
3. Запустить приложение
   ```bash
   java -jar target/nginx-log-analyzer-1.0.0.jar
   ```
   или
   ```bash
   mvn spring-boot:run
   ```
4. По умолчанию REST-сервис слушает порт **8080**

---

## Конфигурация

В `application.yml` (или через API-конфиг `new Config()`) можно задать:

```yaml
app:
  source: file         # или kafka
  streaming-mode: false
  path: /path/to/log
  format: json         # json | markdown
  kafka:
    bootstrap-servers: 127.0.0.1:9092
    group-id: gid
    topic: nginx-logs
aggregation:
  window-seconds: 60
  chart-windows: 5
anomaly:
  z-threshold: 3.0
  ewma:
    alpha: 0.3
    k: 3.0
suspicious:
  threshold-per-window: 10
```

---

## REST API

### `GET /analyze/ping`

Проверка доступности.  
Ответ:
```
✅ NginxLogAnalyzer API работает
```

---

### `POST /analyze`

- **Параметр**: `file` – multipart-upload лог-файла
- **Возвращает**: `200 OK` + JSON → `LogResult`
```json
{
  "totalRequests": 1234,
  "averageResponseSize": 456.78,
  "resourceCounts": { "/": 400, "/api": 300, … },
  "statusCodeCounts": { "200": 1100, "404": 100, … },
  "percentile": 1024.0,
  "anomalies": { "reqsPerWindow": [ … ], "errorRate": [ … ] },
  "suspiciousIps": [ "1.2.3.4", "5.6.7.8" ]
}
```

---
### `POST /analyze/pdf`

- **Параметр**: `file`
- **Возвращает**: `application/pdf` + бинарный PDF отчёт
- **Заголовки**:
    - `Content-Disposition: attachment; filename="report.pdf"`

---
### `POST /analyze/markdown`

- **Параметр**: `file`
- **Возвращает**: `text/markdown` + Markdown-отчёт

---

## Примеры использования

```bash
# JSON-отчёт в терминал
curl -F file=@path/to/example.log http://localhost:8080/analyze

# Сохранить PDF-отчёт
curl -F file=@path/to/example.log http://localhost:8080/analyze/pdf > report.pdf

# Сохранить Markdown-отчёт
curl -F file=@path/to/example.log http://localhost:8080/analyze/markdown > report.md
```

---

## Тестирование

```bash
mvn test
```



---

## Лицензия

MIT License  
См. файл [LICENSE](LICENSE).
