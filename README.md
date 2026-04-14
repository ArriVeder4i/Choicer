<p align="center">
  <img src="app/src/main/ic_launcher-playstore.png" alt="Choicer logo" width="128" />
</p>

<h1 align="center">Choicer</h1>

<p align="center">
  Android-приложение для выбора фильмов в одиночку и вместе с друзьями.
  <br />
  Лента, поиск, вишлист, клипы и подбор совпадений через QR, BLE и NFC.
</p>

<p align="center">
  <img alt="Android" src="https://img.shields.io/badge/Android-26%2B-3DDC84?logo=android&logoColor=white" />
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?logo=kotlin&logoColor=white" />
  <img alt="Kinopoisk API" src="https://img.shields.io/badge/API-Kinopoisk-FF6B00" />
  <img alt="Status" src="https://img.shields.io/badge/status-active%20development-2EA44F" />
</p>

## About

`Choicer` помогает быстрее выбрать, что посмотреть сегодня вечером.

Вместо обычного каталога приложение сочетает несколько сценариев сразу:

- вертикальную ленту фильмов для быстрого просмотра
- умный поиск с фильтрами
- персональный вишлист
- случайный выбор фильма из сохраненного списка
- экран совместимости с другом
- обмен вишлистами через QR, Bluetooth Low Energy и NFC

Это проект на Kotlin + Jetpack Compose с локальным хранением данных через Room и интеграцией с Kinopoisk API.

## Why It Feels Different

- **Быстрый выбор, а не просто каталог.** Лента и случайный выбор уменьшают трение при поиске фильма.
- **Социальный сценарий уже встроен.** Можно сравнить вкусы с другом и сразу увидеть общие совпадения.
- **Нативный Android UX.** Приложение собрано на Compose и ориентировано на мобильный сценарий использования.
- **Локальная ценность.** Вишлист хранится на устройстве и работает как персональный список на вечер.

## Feature Highlights

- `Лента` — вертикальный поток фильмов с быстрым переходом в детали
- `Поиск` — поиск по названию и фильтры по жанрам, рейтингу и годам
- `Вишлист` — сохранение фильмов, отметка просмотренных и случайный выбор
- `Топ` — популярные подборки фильмов и сериалов
- `Друзья` — совпадения, объединенный список и nearby discovery
- `Клипы` — воспроизведение клипов для части каталога

## Screenshots

## Tech Stack

- Kotlin
- Jetpack Compose
- Navigation Compose
- Room
- Retrofit + Gson
- Coil
- Media3 ExoPlayer
- CameraX
- ML Kit Barcode Scanning
- BLE / NFC

## Architecture

Основной код находится в `app/src/main/java/com/example/choicer/`:

- `data` — API, репозитории, база, BLE, модели
- `viewmodel` — состояние и бизнес-логика
- `uiu` — Compose-экраны и UI-компоненты

Ключевые точки входа:

- `MainActivity.kt` — старт приложения и инициализация зависимостей
- `MainApp.kt` — навигация между экранами
- `MovieViewModel.kt` — основное состояние приложения

## Requirements

- Android Studio
- Android SDK 36
- JDK, совместимый с Android Studio
- Android 8.0+ (`minSdk = 26`)
- ключ Kinopoisk API

## Quick Start

1. Клонируйте репозиторий.
2. Создайте локальный файл с секретами:

```powershell
Copy-Item .\secrets.properties.example .\secrets.properties
```

3. Укажите ваш ключ:

```properties
KINOPOISK_API_KEY=your_kinopoisk_api_key
```

4. Откройте проект в Android Studio.
5. Дождитесь Gradle Sync.
6. Запустите приложение на устройстве или эмуляторе.

## Secrets

Секреты не хранятся в коде.

- локальные ключи лежат в `secrets.properties`
- шаблон лежит в `secrets.properties.example`
- `secrets.properties` игнорируется через `.gitignore`
- ключ попадает в приложение через `BuildConfig.KINOPOISK_API_KEY`

Если `secrets.properties` отсутствует, Gradle остановит сборку с понятной ошибкой.

## Build

Debug APK:

```powershell
.\gradlew.bat assembleDebug
```

Установка debug-сборки:

```powershell
.\gradlew.bat installDebug
```

Проверка Kotlin-компиляции:

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

Release-сборка:

```powershell
.\gradlew.bat assembleRelease
```

## Permissions

Приложение использует:

- `INTERNET`
- `CAMERA`
- `NFC`
- `BLUETOOTH`
- `BLUETOOTH_SCAN`
- `BLUETOOTH_ADVERTISE`
- `BLUETOOTH_CONNECT`
- `ACCESS_COARSE_LOCATION`
- `ACCESS_FINE_LOCATION`

Часть разрешений нужна только для сценариев поиска устройства и обмена вишлистами с друзьями.


## Notes

- Каталог фильмов загружается через Kinopoisk API
- Вишлист хранится локально через Room
- Проект ориентирован на портретный режим
- Приложение находится в активной доработке
