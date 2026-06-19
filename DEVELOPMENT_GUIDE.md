# DEVELOPMENT_GUIDE.md - Руководство для разработчиков

## 🛠️ Установка и настройка

### Предварительные требования

- **Java Development Kit (JDK)** 11 или выше
  ```bash
  # Проверить версию
  java -version
  
  # Установить (если не установлен)
  # Windows: https://www.oracle.com/java/technologies/downloads/
  # macOS: brew install openjdk@11
  # Linux: sudo apt-get install openjdk-11-jdk
  ```

- **Android Studio** Arctic Fox (2020.3.1) или выше
  - Скачать: https://developer.android.com/studio

- **Android SDK**
  - API 34 (target)
  - API 21 (minimum)
  ```bash
  # Установить через Android Studio:
  # Preferences → Appearance & Behavior → System Settings → Android SDK
  ```

### Клонирование и настройка проекта

```bash
# 1. Клонировать репозиторий
git clone https://github.com/VitaliyTaranyuk/sport.git
cd sport

# 2. Синхронизировать Gradle
./gradlew sync

# 3. Построить проект
./gradlew build

# 4. Запустить тесты
./gradlew test
```

### Установка JAVA_HOME переменной окружения

#### Windows

1. Найти путь установки JDK:
   ```
   C:\Program Files\Java\jdk-11.0.x
   ```

2. Установить переменную окружения:
   ```
   JAVA_HOME = C:\Program Files\Java\jdk-11.0.x
   PATH += %JAVA_HOME%\bin
   ```

3. Проверить:
   ```bash
   java -version
   ```

#### macOS/Linux

```bash
# Добавить в ~/.bashrc или ~/.zshrc
export JAVA_HOME=$(/usr/libexec/java_home -v 11)
export PATH=$JAVA_HOME/bin:$PATH

# Перезагрузить
source ~/.bashrc

# Проверить
java -version
```

## 📁 Структура проекта

```
WorkoutCounter/
├── app/
│   ├── build.gradle.kts          # Конфигурация build
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/workoutcounter/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── WorkoutCounterApplication.kt
│   │   │   │   ├── data/                    # Data Layer
│   │   │   │   │   ├── local/db/
│   │   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   │   ├── ExerciseEntity.kt
│   │   │   │   │   │   └── WorkoutDao.kt
│   │   │   │   │   └── repository/
│   │   │   │   │       └── RepositoryImpl.kt
│   │   │   │   ├── domain/                  # Domain Layer
│   │   │   │   │   ├── model/
│   │   │   │   │   │   └── Models.kt
│   │   │   │   │   └── repository/
│   │   │   │   │       └── Repository.kt
│   │   │   │   ├── di/                      # Dependency Injection
│   │   │   │   │   └── AppModule.kt
│   │   │   │   ├── navigation/
│   │   │   │   │   └── AppNavGraph.kt
│   │   │   │   ├── ui/                      # UI Layer
│   │   │   │   │   └── screens/
│   │   │   │   │       └── Screens.kt
│   │   │   │   └── vision/                  # Vision Module
│   │   │   │       ├── PoseDetector.kt
│   │   │   │       ├── ExerciseClassifier.kt
│   │   │   │       ├── RepetitionCounter.kt
│   │   │   │       ├── WorkoutSessionManager.kt
│   │   │   │       └── error/
│   │   │   │           └── ErrorHandler.kt
│   │   │   └── AndroidManifest.xml
│   │   ├── test/java/                       # Unit & Integration Tests
│   │   │   └── com/example/workoutcounter/
│   │   │       ├── vision/
│   │   │       ├── integration/
│   │   │       └── data/repository/
│   │   └── androidTest/java/                # E2E Tests
│   │       └── com/example/workoutcounter/e2e/
│   └── build.gradle.kts
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── README.md
├── TESTING.md
├── ALGORITHMS.md
├── ARCHITECTURE.md
└── .git/
```

## 🔧 IDE Setup (Android Studio)

### Плагины и расширения

1. **Обязательные плагины:**
   - Kotlin (встроен)
   - Jetpack Compose
   - Android Lint

2. **Рекомендуемые плагины:**
   - SonarLint
   - Detekt (Kotlin Static Analysis)
   - Gradle Dependency Analyzer

### Конфигурация стиля кода

```
File → Settings → Editor → Code Style → Kotlin
→ Set from... → Predefined style → Kotlin Style Guide
```

### Шаблоны

1. **File Templates:**
   ```
   Settings → Editor → File and Code Templates
   ```

2. **Live Templates:**
   ```
   Settings → Editor → Live Templates
   
   // Добавить:
   - `test` → Unit test template
   - `mockk` → MockK setup template
   ```

## 🔄 Git Workflow

### Основной workflow

```bash
# 1. Создать feature branch
git checkout -b feature/exercise-detection

# 2. Сделать изменения
# (редактировать файлы)

# 3. Добавить файлы
git add .

# 4. Сделать коммит (Conventional Commits)
git commit -m "feat: implement pull-up detection

- Add landmark extraction for pull-ups
- Implement angle calculation
- Add 5 unit tests

Closes #123"

# 5. Запустить тесты
./gradlew test

# 6. Если тесты не проходят
git commit --amend

# 7. Запушить на GitHub
git push origin feature/exercise-detection

# 8. Создать Pull Request
```

### Conventional Commits Format

```
<type>(<scope>): <subject>

<body>

<footer>

Types:
- feat:     New feature
- fix:      Bug fix
- docs:     Documentation
- style:    Code style (formatting)
- refactor: Code refactoring
- test:     Tests
- chore:    Build, dependencies
- perf:     Performance
- ci:       CI/CD

Scope (optional):
- vision
- data
- ui
- core

Example:
feat(vision): implement exercise classifier

- Add ExerciseClassifier class
- Implement 4 exercise types
- Add 7 unit tests
- Update ALGORITHMS.md

Closes #42
```

## 🧪 Тестирование

### Запуск тестов

```bash
# All unit tests
./gradlew test

# Specific test class
./gradlew test --tests ExerciseClassifierTest

# Specific test method
./gradlew test --tests ExerciseClassifierTest.testClassifyPushUpsPose

# With coverage report
./gradlew test --tests "*" -Dkover.enabled=true

# Integration tests only
./gradlew test --tests "*IntegrationTest"

# E2E tests (requires emulator/device)
./gradlew connectedAndroidTest

# All tests
./gradlew test connectedAndroidTest
```

### Написание новых тестов

#### Unit Test Template

```kotlin
class MyFeatureTest {
    @Before
    fun setUp() {
        // Initialize test fixtures
    }

    @Test
    fun testHappyPath() {
        // Given
        val input = createTestData()
        
        // When
        val result = myFeature.process(input)
        
        // Then
        assertEquals(expected, result)
    }

    @Test
    fun testErrorHandling() {
        // Given
        val invalidInput = null
        
        // When/Then
        assertThrows(NullPointerException::class.java) {
            myFeature.process(invalidInput)
        }
    }
}
```

#### Integration Test Template

```kotlin
class MyIntegrationTest {
    @Before
    fun setUp() {
        // Initialize multiple components
    }

    @Test
    fun testComponentInteraction() {
        // Test how components work together
    }
}
```

## 🐛 Отладка

### Debug Mode

```bash
# 1. Запустить в debug mode
./gradlew installDebugApp

# 2. Подключиться к debugger
- Debug → Attach Debugger to Android Process
- Выбрать процесс приложения
```

### Логирование

```kotlin
import android.util.Log

// В коде
Log.d("WorkoutCounter", "Debug message: $value")
Log.e("WorkoutCounter", "Error occurred", exception)

// В Logcat
// Android Studio → View → Tool Windows → Logcat
```

### Профилирование

```bash
# Профиль памяти
./gradlew :app:profileDebugUnitTest

# Профиль производительности
- Android Studio → Profiler → Record
```

## 📦 Build и Release

### Debug Build

```bash
# Build APK
./gradlew assembleDebug

# Install on device
./gradlew installDebug

# Run with tests
./gradlew connectedCheck
```

### Release Build

```bash
# 1. Обновить версию в build.gradle.kts
versionCode = 2
versionName = "1.1.0"

# 2. Build release APK (требует keystore)
./gradlew assembleRelease

# 3. или Build Bundle для Google Play
./gradlew bundleRelease

# 4. Sign with keystore
# (инструкции в Google Play Console)
```

### Keystore Setup

```bash
# Создать keystore
keytool -genkey -v -keystore my-release-key.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias my-key-alias

# Использовать в build.gradle.kts
signingConfigs {
    release {
        storeFile = file("my-release-key.keystore")
        storePassword = "..."
        keyAlias = "my-key-alias"
        keyPassword = "..."
    }
}
```

## 📊 CI/CD Setup

### GitHub Actions

Создать файл `.github/workflows/test.yml`:

```yaml
name: Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v2
    
    - name: Setup Java
      uses: actions/setup-java@v2
      with:
        java-version: '11'
    
    - name: Run Unit Tests
      run: ./gradlew test
    
    - name: Run Lint
      run: ./gradlew lint
    
    - name: Generate Coverage Report
      run: ./gradlew test --tests "*" -Dkover.enabled=true
    
    - name: Upload Coverage to Codecov
      uses: codecov/codecov-action@v2
```

## 🚀 Deployment

### Beta Testing (Google Play)

```
1. Build release APK/Bundle
2. Upload to Google Play Console → Releases → Testing → Beta
3. Share link with testers
4. Collect feedback
5. Make fixes
6. Deploy to production
```

### Firebase Distribution (Internal Testing)

```
1. ./gradlew assembleDebug
2. Firebase → App Distribution
3. Upload APK
4. Add testers
5. Send invitations
```

## 📞 Troubleshooting

### Проблема: "JAVA_HOME is not set"

**Решение:**
```bash
# Windows
set JAVA_HOME=C:\Program Files\Java\jdk-11
set PATH=%JAVA_HOME%\bin;%PATH%

# macOS/Linux
export JAVA_HOME=$(/usr/libexec/java_home -v 11)
export PATH=$JAVA_HOME/bin:$PATH
```

### Проблема: Gradle build fails

**Решение:**
```bash
./gradlew clean build --stacktrace
```

### Проблема: Tests not found

**Решение:**
```bash
# Убедиться, что тесты находятся в правильной директории
# src/test/java/ для unit tests
# src/androidTest/java/ для E2E tests

# Пересинхронизировать Gradle
./gradlew sync
```

### Проблема: Low performance

**Решение:**
```bash
# Увеличить heap size
export _JAVA_OPTIONS="-Xmx2048m"
./gradlew build
```

## 📚 Полезные ссылки

- [Android Development Guide](https://developer.android.com/guide)
- [Kotlin Documentation](https://kotlinlang.org/docs/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [MediaPipe](https://developers.google.com/mediapipe)
- [Testing Best Practices](https://developer.android.com/training/testing)

## 🤝 Contributing

1. Fork the project
2. Create feature branch
3. Make changes
4. Run tests
5. Commit and push
6. Create Pull Request

---

**Версия**: 1.0.0  
**Последнее обновление**: 2026-06-19  
**Статус**: 🟢 Актуально
