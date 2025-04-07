# Netty-Backproxy

Приложение построено на фреймворке **Netty** и реализует клиент-серверную архитектуру с динамической маршрутизацией запросов.  
Сервер запускается и начинает слушать указанный TCP-порт. Как только первый клиент(нода) подключается, дополнительно активируется SOCKS5-порт.  
После этого все запросы, приходящие на этот порт, пересылаются клиенту, который выполняет их и возвращает результат обратно на сервер.  
Сервер логирует полученные ответы.  

---

## 🚀 Ключевые особенности

### 1) **Распределение нагрузки**

- При превышении количества запросов над числом доступных клиентских соединений — сервер равномерно распределяет нагрузку между ними.

### 2) **Привязка клиента к ноде**

- Первый запрос от клиента закрепляет его за конкретной нодой.
- Все последующие запросы с этого IP обрабатываются только этой нодой.

### 3) **TTL (Time To Live)**

- Для каждого клиента устанавливается TTL = `30 секунд`.
- Новый запрос сбрасывает TTL обратно на 30 секунд.
- При отсутствии запросов в течение этого времени — привязка клиента аннулируется.
- Следующий запрос будет направлен на первую доступную ноду.

---

## 🔄 Динамическая маршрутизация запросов

| Аспект              | Реализация                                                                 |
|---------------------|---------------------------------------------------------------------------|
| **Балансировка**    | Равномерное распределение запросов по активным клиентам.                   |
| **Привязка**        | IP клиента закрепляется за нодой при первом запросе.                       |
| **TTL (30s)**       | Привязка сбрасывается при простое, запрос перенаправляется новой ноде.    |

---

## 🛠️ Технологии и библиотеки

Сервер и клиент написаны на языке **Java** с использованием **Netty**.

### Язык программирования:
- Java 21+

### Используемые библиотеки:

| Библиотека | Версия | Описание |
|------------|--------|----------|
| [Netty](https://netty.io/) | `4.1.x` | Асинхронный сетевой фреймворк для высокопроизводительных приложений. |
| [Lombok](https://projectlombok.org/) | `1.18.x` | Упрощает написание Java-кода, избавляя от шаблонных конструкций (геттеры, конструкторы, билдеры и т.п.). |
| [SLF4J + Logback](http://www.slf4j.org/) | `1.7.x` / `1.2.x` | Унифицированный API для логирования с реализацией Logback. |

---

## ⚙️ Как запустить
- Сервер:
```bash
java -jar server-1.0-SNAPSHOT.jar
```
- Клиент:
```bash
java -jar client-1.0-SNAPSHOT.jar serverHostOrIp
```

Также в директории "backproxyTestBuildInDocker" можно найти готовый Dockerfile для сервера и клиента для редактирования и создания Docker образов.

---

## 📌 Примеры использования

После того как вы запустили приложение, можно протестировать его работу с помощью обычного `curl`-запроса через SOCKS5-прокси. Например, через **Git Bash**, **WSL**, **Linux** или **macOS**:

```bash
curl --socks5 127.0.0.1:10800 https://github.com/
```
