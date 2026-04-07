# Text File Indexer

Простая библиотека для индексации текстовых файлов и поиска файлов по словам.

## Возможности

- Добавление отдельных файлов в индекс
- Добавление директорий (с рекурсивным обходом)
- Поиск файлов по слову
- Удаление файлов и директорий из индекса
- Автоматическое отслеживание изменений файлов:
    - создание файлов
    - изменение файлов
    - удаление файлов
- Потокобезопасный in-memory индекс
- Расширяемый механизм токенизации (через интерфейс `Tokenizer`)

---

## Архитектура

### 1. API
- `TextFileIndexer` — публичный интерфейс библиотеки

### 2. Core
- `DefaultTextFileIndexer` — основная реализация
- `FileIndexer` — индексация отдельного файла

### 3. Index
- `InMemoryInvertedIndex` — обратный индекс:
    - `word -> files`
    - `file -> words`

### 4. Tokenizer
- `Tokenizer` — интерфейс
- `SimpleTokenizer` — базовая реализация (split по regex)

### 5. Watcher
- `DirectoryWatcher` — отслеживание изменений через `WatchService`
- `FileChangeListener` — callback интерфейс

### 6. CLI
- `Main` — простое интерактивное приложение

---

## Использование

```text
Available commands:
  add-file <path>
  add-dir <path>
  search <word>
  remove <path>
  help
  exit
```

### Добавление файла
```bash
> add-file /tmp/docs/a.txt
File added to index
```

### Добавление директории
```bash
> add-file /tmp/docs/a.txt
File added to index
```

### Поиск слова
```bash
> search java
Found files:
  /tmp/docs/a.txt
  /tmp/docs/b.txt
```
и т.д.

---

## Сборка проекта

```bash
./gradlew build