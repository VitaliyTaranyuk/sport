# WorkoutCounter - Приложение для автоматического отслеживания тренировок

## 🎯 Обзор

WorkoutCounter - это Android приложение, которое использует компьютерное зрение (MediaPipe Pose) для автоматического распознавания упражнений, подсчета повторений и управления тренировками без необходимости нажимать кнопки.

### Основные возможности

✅ **Автоматическое распознавание 4 типов упражнений:**
- Подтягивания (Pull-ups)
- Отжимания (Push-ups)
- Приседания (Squats)
- Упражнения на пресс (Ab exercises)

✅ **Режим свободной руки:**
- Автоматическое определение начала упражнения
- Автоматический подсчет повторений
- Автоматическое определение конца подхода
- Автоматический таймер отдыха между подходами

✅ **Умное управление тренировками:**
- Отслеживание всех подходов и повторений
- История тренировок
- Статистика по упражнениям
- Обнаружение ошибок с локализованными сообщениями

## 🏗️ Архитектура

### Слои приложения

```
┌─────────────────────────────────────────────────────┐
│          UI Layer (Jetpack Compose)                  │
├─────────────────────────────────────────────────────┤
│          ViewModel & Navigation                      │
├─────────────────────────────────────────────────────┤
│       WorkoutSessionManager (Vision Orchestration)   │
├─────────────────────────────────────────────────────┤
│                  Vision Module                       │
│  PoseDetector | Classifier | RepCounter | ErrorH.   │
├─────────────────────────────────────────────────────┤
│          Repository Layer (Data Management)          │
├─────────────────────────────────────────────────────┤
│          Room Database & Entities                    │
└─────────────────────────────────────────────────────┘
```

### Ключевые компоненты

#### 1. Vision Module (`vision/`)
- **PoseDetector.kt** - Интеграция с MediaPipe Pose, определение 33 ключевых точек тела
- **ExerciseClassifier.kt** - Классификация упражнений на основе геометрии позы
- **RepetitionCounter.kt** - Автоматический подсчет повторений через анализ фаз движения
- **WorkoutSessionManager.kt** - Координация всех компонентов, управление состоянием сессии
- **ErrorHandler.kt** - Обработка ошибок с дружественными сообщениями

#### 2. Data Layer (`data/`)
- **AppDatabase.kt** - Определение Room базы данных
- **Entities** - Модели данных для упражнений, тренировок, подходов, повторений
- **DAOs** - Интерфейсы доступа к данным
- **Repositories** - Реализация бизнес-логики

#### 3. Domain Layer (`domain/`)
- **Models.kt** - Доменные модели (Exercise, Workout, Set, Rep)
- **Repositories** - Интерфейсы для работы с данными

## 📊 Тестирование

Проект имеет полное покрытие тестами на разных уровнях:

### Unit Tests (29 тестов)

```
test/java/com/example/workoutcounter/
├── vision/
│   ├── ExerciseClassifierTest.kt (7 тестов)
│   │   - testClassifyPullUpsPose
│   │   - testClassifyPushUpsPose
│   │   - testClassifySquatsPose
│   │   - testClassifyAbExercisesPose
│   │   - testIsStandingPosition
│   │   - testIsNotStandingWhenExercising
│   │   - testInsufficientLandmarksReturnsNull
│   │
│   ├── RepetitionCounterTest.kt (6 тестов)
│   │   - testInitialRepCountIsZero
│   │   - testDetectsSingleRepForPushUps
│   │   - testDetectsMultipleReps
│   │   - testResetClearsRepCount
│   │   - testSquatRepCounting
│   │   - testPullUpRepCounting
│   │   - testAbExerciseRepCounting
│   │
│   ├── WorkoutSessionManagerTest.kt (7 тестов)
│   │   - testSessionStartsInIdleState
│   │   - testStartSessionChangesState
│   │   - testEndSessionReturnsToIdle
│   │   - testSetCountIncrementsOnSetStart
│   │   - testExerciseTypeDetectedCorrectly
│   │   - testRepCountIncrementsOnRepCompletion
│   │   - testSetEndsWhenPersonStandsUp
│   │   - testNewSetStartsWithDifferentExercise
│   │   - testGetCurrentSetInfo
│   │   - testRestTimerStartsAfterSetEnd
│   │
│   └── error/ErrorHandlerTest.kt (7 тестов)
│       - testInitialStateIsNull
│       - testHandleCameraAccessDeniedError
│       - testHandleCameraNotAvailableError
│       - testHandleInvalidPoseDetection
│       - testHandleLowConfidencePose
│       - testAcceptHighConfidencePose
│       - testClearError
│       - testGetErrorMessage
│
└── data/repository/
    └── ExerciseRepositoryTest.kt (2 теста)
        - testGetAllExercises
        - testInsertExercise
```

### Integration Tests (5 тестов)

```
test/java/com/example/workoutcounter/integration/
└── WorkoutTrackingIntegrationTest.kt
    - testCompleteWorkoutSession
    - testMultipleExercisesInSession
    - testRestPeriodDetection
    - testExerciseClassificationAccuracy
    - testRepCountingAccuracy
```

### E2E Tests (5 тестов)

```
androidTest/java/com/example/workoutcounter/e2e/
└── WorkoutFlowE2ETest.kt
    - testStartWorkoutFlow
    - testExerciseDetectionUI
    - testRepCountingUI
    - testRestTimerUI
    - testWorkoutHistoryUI
```

## 🚀 Запуск проекта

### Требования

- **Java 11 или выше** (установить JDK)
- **Android Studio Arctic Fox** или выше
- **Android SDK** версии 34
- **Минимальная версия Android**: API 21

### Установка

1. Клонируйте репозиторий
   ```bash
   git clone https://github.com/VitaliyTaranyuk/sport.git
   cd sport
   ```

2. Откройте проект в Android Studio
   ```bash
   # Или используйте командную строку
   ./gradlew sync
   ```

### Запуск тестов

#### Unit Tests
```bash
# Запустить все unit-тесты
./gradlew test

# Запустить тесты для конкретного класса
./gradlew test --tests ExerciseClassifierTest

# С подробным выводом
./gradlew test --info
```

#### Integration Tests
```bash
./gradlew test --tests "*IntegrationTest"
```

#### E2E Tests (требует подключенного устройства/эмулятора)
```bash
./gradlew connectedAndroidTest

# Или только E2E тесты
./gradlew connectedAndroidTest --tests "*E2ETest"
```

#### Все тесты вместе
```bash
# Unit + Integration
./gradlew test

# Unit + Integration + E2E
./gradlew test connectedAndroidTest
```

### Запуск приложения

```bash
# На подключенном устройстве
./gradlew installDebug

# Или открыть в Android Studio и нажать Run
```

## 🔄 Процесс работы с кодом

### Перед каждым коммитом

1. **Запустить тесты**
   ```bash
   ./gradlew test
   ```

2. **Проверить покрытие**
   ```bash
   ./gradlew testDebugUnitTest --tests "*" -Dkover.enabled=true
   ```

3. **Запустить lint**
   ```bash
   ./gradlew lint
   ```

### Создание Pull Request

1. Создайте новую ветку
   ```bash
   git checkout -b feature/your-feature
   ```

2. Сделайте изменения и коммиты
   ```bash
   git add .
   git commit -m "feat: описание функции"
   ```

3. Запустите все тесты
   ```bash
   ./gradlew test connectedAndroidTest
   ```

4. Убедитесь, что все тесты проходят (100%)

5. Создайте Pull Request на GitHub

## 📈 Алгоритмы

### Классификация упражнений

Система использует MediaPipe Pose для получения 33 точек тела и анализирует их геометрию:

```
PULL_UPS:
- Условие: Запястья выше плеч + Тело вертикально
- Ключевые точки: Запястья (9, 10), Плечи (11, 12)

PUSH_UPS:
- Условие: Тело горизонтально + Руки под туловищем
- Ключевые точки: Нос (0), Запястья (9, 10), Локти (7, 8)

SQUATS:
- Условие: Колени согнуты < 120° + Ноги работают
- Ключевые точки: Бедра (23, 24), Колени (25, 26), Лодыжки (27, 28)

AB_EXERCISES:
- Условие: Тело лежит + Расстояние грудь-бедро минимально
- Ключевые точки: Нос (0), Плечи (11, 12), Бедра (23, 24)
```

### Подсчет повторений

Алгоритм отслеживает фазы движения:

```
REST → ASCENDING → TOP_POSITION → DESCENDING → BOTTOM_POSITION → REST

1 повторение = BOTTOM ↔ TOP ↔ BOTTOM

Фильтрация шума: 3+ последовательных фрейма для подтверждения смены фазы
```

### Определение конца подхода

```
Условия завершения подхода:
1. Пользователь встает (классификация STANDING)
2. Обнаружено новое упражнение
3. Низкая уверенность определения позы (< 50%)

Результат: Переход в режим отдыха с таймером (60 сек)
```

## 🐛 Обработка ошибок

Приложение обрабатывает следующие ошибки:

| Ошибка | Код | Решение |
|--------|-----|---------|
| Camera Not Available | CAM_001 | Проверить наличие камеры на устройстве |
| Camera Access Denied | CAM_002 | Разрешить доступ в настройках |
| Invalid Pose | VISION_001 | Убедиться, что вы видны в камере |
| Low Confidence | VISION_002 | Посмотреть в камеру прямо |
| Exercise Detection Failed | VISION_003 | Выполнять четкие движения |

## 📚 Документация

- [ARCHITECTURE.md](ARCHITECTURE.md) - Подробная архитектура приложения
- [build.gradle.kts](app/build.gradle.kts) - Конфигурация зависимостей
- [Исходный код](app/src) - Полный исходный код с комментариями

## 🚢 Развертывание

### Подготовка к release

```bash
# Обновить версию
./gradlew assembleRelease

# Подписать APK
# (требует keystore файл)

# Создать Bundle для Google Play
./gradlew bundleRelease
```

## 🤝 Вклад

Если вы хотите внести вклад в проект:

1. Создайте fork репозитория
2. Создайте feature branch (`git checkout -b feature/AmazingFeature`)
3. Сделайте коммит (`git commit -m 'Add some AmazingFeature'`)
4. Запустите все тесты
5. Создайте Pull Request

### Требования к коду

- ✅ Все тесты должны проходить (100%)
- ✅ Код должен следовать Kotlin style guide
- ✅ Новые функции должны иметь unit-тесты
- ✅ Коммит-сообщения следуют Conventional Commits

## 📝 Лицензия

Этот проект лицензирован под MIT License - см. [LICENSE](LICENSE) файл для деталей.

## 📞 Контакты

- GitHub: [@VitaliyTaranyuk](https://github.com/VitaliyTaranyuk)
- Проект: [Sport](https://github.com/VitaliyTaranyuk/sport)

## 🙏 Благодарности

- MediaPipe для Pose Detection
- Google для Android Studio и Jetpack Compose
- Сообществу Android разработчиков

---

**Версия**: 1.0.0  
**Последнее обновление**: 2026-06-19  
**Статус**: 🟢 В активной разработке
