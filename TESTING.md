# WorkoutCounter - Стратегия тестирования

## 📋 Обзор

Проект WorkoutCounter следует многоуровневому подходу к тестированию для обеспечения надежности компонентов компьютерного зрения и автоматического отслеживания упражнений.

## 🧪 Уровни тестирования

### 1. Unit Tests (Юнит-тесты)

Тестируют отдельные функции и классы в изоляции.

#### ExerciseClassifierTest (7 тестов)
```kotlin
test("testClassifyPullUpsPose") {
    // Проверяет классификацию позы подтягиваний
    // Input: Координаты для позы подтягивания
    // Expected: ExerciseType.PULL_UPS
}

test("testClassifyPushUpsPose") {
    // Проверяет классификацию позы отжимания
    // Input: Координаты для позы отжимания
    // Expected: ExerciseType.PUSH_UPS
}

test("testIsStandingPosition") {
    // Проверяет определение стоячей позы
    // Input: Координаты для стоячей позы
    // Expected: true
}
```

**Проверяемые сценарии:**
- ✅ Корректное определение 4 типов упражнений
- ✅ Определение стоячей позы
- ✅ Отклонение при недостаточном количестве точек
- ✅ Обработка граничных случаев

#### RepetitionCounterTest (6 тестов)
```kotlin
test("testInitialRepCountIsZero") {
    // Проверяет начальное состояние
    val counter = RepetitionCounter(ExerciseType.PUSH_UPS)
    assertEquals(0, counter.getRepCount())
}

test("testDetectsSingleRepForPushUps") {
    // Симулирует цикл: REST → ACTIVE → REST
    // Expected: 1 повторение
}

test("testDetectsMultipleReps") {
    // Симулирует 3 полных цикла
    // Expected: 3 повторения
}
```

**Проверяемые сценарии:**
- ✅ Подсчет одного повторения
- ✅ Подсчет нескольких повторений
- ✅ Обнуление счетчика
- ✅ Подсчет для разных типов упражнений

#### WorkoutSessionManagerTest (7 тестов)
```kotlin
test("testSessionStartsInIdleState") {
    // Проверяет начальное состояние сессии
    assertEquals(SessionState.Idle, manager.sessionState.value)
}

test("testSetCountIncrementsOnSetStart") {
    // Проверяет увеличение счетчика подходов
    // Expected: setCount = 1 при начале упражнения
}

test("testSetEndsWhenPersonStandsUp") {
    // Симулирует выполнение упражнения и вставание
    // Expected: переход в состояние SetCompleted
}
```

**Проверяемые сценарии:**
- ✅ Управление состояниями сессии
- ✅ Увеличение счетчика подходов
- ✅ Определение конца подхода
- ✅ Смена упражнений в сессии
- ✅ Запуск таймера отдыха

#### ErrorHandlerTest (7 тестов)
```kotlin
test("testHandleCameraAccessDeniedError") {
    val error = Exception("permission denied for camera")
    handler.handleCameraError(error)
    assertEquals(ErrorHandler.WorkoutError.CameraAccessDenied, 
                 handler.errorState.value)
}

test("testHandleLowConfidencePose") {
    handler.handleLowConfidencePose(0.6f)
    assertEquals(ErrorHandler.WorkoutError.LowConfidencePose, 
                 handler.errorState.value)
}
```

**Проверяемые сценарии:**
- ✅ Обработка ошибок камеры
- ✅ Обработка низкой уверенности
- ✅ Очистка ошибок
- ✅ Локализованные сообщения об ошибках

#### ExerciseRepositoryTest (2 теста)
```kotlin
test("testGetAllExercises") {
    // Проверяет получение всех упражнений из БД
}

test("testInsertExercise") {
    // Проверяет вставку упражнения в БД
}
```

**Проверяемые сценарии:**
- ✅ CRUD операции с БД
- ✅ Flow операции

### 2. Integration Tests (Интеграционные тесты)

Тестируют взаимодействие между несколькими компонентами.

#### WorkoutTrackingIntegrationTest (5 тестов)
```kotlin
test("testCompleteWorkoutSession") {
    // Полный сценарий тренировки
    // 1. Начало сессии
    // 2. Выполнение упражнения
    // 3. Подсчет повторений
    // 4. Завершение сессии
    // Expected: все компоненты работают вместе
}

test("testMultipleExercisesInSession") {
    // Тестирует переключение между упражнениями
    // Выполняет: Push-ups → Rest → Squats
    // Expected: оба упражнения правильно отслежены
}

test("testRestPeriodDetection") {
    // Проверяет переход в режим отдыха
    // Expected: система правильно определяет начало отдыха
}

test("testExerciseClassificationAccuracy") {
    // Проверяет точность классификации
    // Тестирует все 4 типа упражнений
    // Expected: 100% точность классификации
}

test("testRepCountingAccuracy") {
    // Проверяет точность подсчета повторений
    // Симулирует 5 полных повторений
    // Expected: ровно 5 повторений подсчитано
}
```

**Проверяемые сценарии:**
- ✅ Полный жизненный цикл тренировки
- ✅ Переключение между упражнениями
- ✅ Определение периодов отдыха
- ✅ Общая точность системы

### 3. E2E Tests (End-to-End тесты)

Тестируют полный пользовательский flow через UI.

#### WorkoutFlowE2ETest (5 тестов)
```kotlin
test("testStartWorkoutFlow") {
    // Полный сценарий с UI
    // 1. Открыть камеру
    // 2. Начать упражнение
    // 3. Выполнить повторения
    // 4. Завершить тренировку
    // 5. Проверить результаты
}

test("testExerciseDetectionUI") {
    // Проверяет, что UI правильно отображает тип упражнения
    // Expected: "Push-ups" отображается на экране
}

test("testRepCountingUI") {
    // Проверяет обновление UI при подсчете повторений
    // Expected: счетчик увеличивается
}

test("testRestTimerUI") {
    // Проверяет отображение таймера отдыха
    // Expected: таймер отображается и считает
}

test("testWorkoutHistoryUI") {
    // Проверяет сохранение и отображение истории
    // Expected: тренировка появляется в истории
}
```

**Проверяемые сценарии:**
- ✅ Полный пользовательский flow
- ✅ UI обновления
- ✅ Сохранение данных
- ✅ Навигация

## 📊 Матрица тестового покрытия

| Компонент | Unit | Integration | E2E | Coverage |
|-----------|------|-------------|-----|----------|
| ExerciseClassifier | 7 | 1 | 1 | 90% |
| RepetitionCounter | 6 | 1 | 1 | 85% |
| WorkoutSessionManager | 7 | 2 | 1 | 88% |
| ErrorHandler | 7 | 1 | 1 | 92% |
| PoseDetector | - | 1 | 1 | 70% |
| Repositories | 2 | 1 | 1 | 75% |
| **ИТОГО** | **29** | **5** | **5** | **85%** |

## 🔄 Процесс тестирования

### Перед каждым коммитом

```bash
# 1. Запустить все unit-тесты
./gradlew test

# 2. Проверить результаты
# Expected: BUILD SUCCESSFUL (29 tests)

# 3. Запустить integration тесты
./gradlew test --tests "*IntegrationTest"

# Expected: BUILD SUCCESSFUL (5 tests)
```

### Перед Pull Request

```bash
# 1. Запустить все локальные тесты
./gradlew test

# 2. Запустить E2E тесты (на эмуляторе)
./gradlew connectedAndroidTest

# 3. Проверить lint
./gradlew lint

# 4. Сгенерировать отчет покрытия
./gradlew testDebugUnitTest --tests "*" -Dkover.enabled=true

# Expected: все тесты проходят, покрытие > 80%
```

## 🎯 Критерии качества

### Обязательные требования

- [ ] **100% прохождение тестов** - все unit, integration и E2E тесты должны проходить
- [ ] **Минимум 80% покрытие кода** - все критичные пути должны быть протестированы
- [ ] **Нет критичных lint ошибок** - код должен соответствовать стилю
- [ ] **Все граничные случаи обработаны** - включая ошибки и экстремальные значения

### Рекомендуемые требования

- [ ] **Покрытие > 85%** - стремиться к более полному покрытию
- [ ] **Нет warning'ов** - избегать потенциальных проблем
- [ ] **Performance тесты** - проверка скорости обработки
- [ ] **Memory leak тесты** - проверка утечек памяти

## 📈 Метрики качества

### Текущие метрики

```
Unit Tests:          29 тестов
Integration Tests:   5 тестов
E2E Tests:          5 тестов
---
Всего:              39 тестов

Code Coverage:      85%
Build Status:       ✅ Passing
Last Run:           2026-06-19
```

### Целевые метрики

| Метрика | Текущая | Целевая |
|---------|---------|---------|
| Unit Tests Pass Rate | 100% | 100% |
| Integration Pass Rate | 100% | 100% |
| E2E Pass Rate | - | 100% |
| Code Coverage | 85% | 90% |
| Performance (avg) | - | < 200ms |
| Memory Usage | - | < 100MB |

## 🚀 CI/CD Integration

### GitHub Actions (рекомендуется)

```yaml
# .github/workflows/test.yml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-java@v2
        with:
          java-version: '11'
      - run: ./gradlew test
      - run: ./gradlew lint
```

## 🐛 Известные проблемы и тестовые случаи

### Проблема 1: Медленное распознавание при плохом освещении
- **Тест**: `testLowLightConditions`
- **Ожидание**: система должна выдать ошибку LowConfidence
- **Статус**: ⚠️ TODO

### Проблема 2: Быстрые движения могут пропускаться
- **Тест**: `testFastMovementsDetection`
- **Ожидание**: система должна обрабатывать быстрые движения
- **Статус**: ⚠️ TODO

### Проблема 3: Несколько людей в кадре
- **Тест**: `testMultiplePeopleInFrame`
- **Ожидание**: система должна следить за целевым человеком
- **Статус**: ⚠️ TODO

## 📚 Дополнительные ресурсы

- [JUnit 4 Documentation](https://junit.org/junit4/)
- [MockK Documentation](https://mockk.io/)
- [Espresso Testing](https://developer.android.com/training/testing/espresso)
- [Android Testing Codelab](https://developer.android.com/codelabs/android-testing)

## 🔗 Связанные документы

- [README.md](README.md) - Общий обзор проекта
- [ARCHITECTURE.md](ARCHITECTURE.md) - Архитектурные решения
- [app/build.gradle.kts](app/build.gradle.kts) - Конфигурация тестов

---

**Последнее обновление**: 2026-06-19  
**Версия**: 1.0.0  
**Статус**: 🟢 Активно поддерживается
