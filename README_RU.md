<div align="center">
  <img width="2048" height="989" alt="17831909936168749184217530060427" src="https://github.com/user-attachments/assets/9af64f16-8a70-4128-9736-f907f1e3c06d" />

<h1><img src="https://github.com/user-attachments/assets/02086040-660d-42d8-8830-78053e1e05a4" height="30" style="vertical-align: middle;"> Kizzy Enhanced</h1>
<h4>Расширенный форк <a href="https://github.com/dead8309/Kizzy">Kizzy</a> — менеджер Discord Rich Presence для Android, написанный на Kotlin.</h4>
</div>

<div align="center">
<img src="https://img.shields.io/badge/Minimum%20SDK-27-%23?&style=flat-square&color=5b5ef7">
<img src="https://img.shields.io/badge/kotlin-5b5ef7.svg?logo=kotlin&logoColor=white&style=flat-square">
<img src="https://img.shields.io/badge/package-com.my.kizzy.enhanced-5b5ef7?style=flat-square">
<img src="https://img.shields.io/badge/name-Kizzy%20Enhanced-5b5ef7?style=flat-square">
</div>

<div align="center">

[![README in English](https://img.shields.io/badge/README-in%20English-blue?style=for-the-badge)](README.md)
</div>
<p>
<img src="https://github.com/user-attachments/assets/f8402300-d628-4104-8f86-dc6021c9fbbe" width=60%/>
</p>
</div>

> [!NOTE]
> **Kizzy Enhanced** — личный форк [Kizzy](https://github.com/dead8309/Kizzy) от dead8309. Добавления форка отмечены ⭐.

## Возможности

- [x] Кликабельные кнопки
- [x] Определяет текущее запущенное приложение
- [x] Определяет текущее воспроизводимое медиа
- [x] Опциональные временные метки
- [x] Кастомный статус
- [x] Сохранение/загрузка конфигураций присутствия
- [x] Material You тема
- [x] Переводы
- [x] Простая настройка
- [x] Создание кастомных конфигов со своими изображениями и ссылками
- [x] Превью RPC прямо в приложении
- [x] Работает в фоне даже когда экран выключен
- [x] Поддержка GIF
- [x] Поддержка внешних URL (можно указать ссылку на изображение в интернете — Discord покажет его!)
- [x] Использование изображений из галереи
- [x] ⭐ Подмена платформы (Desktop / Web / Android / iOS / Xbox / Playstation / VR)
- [x] ⭐ Кастомный Application ID — своя иконка и название активности в Discord
- [x] ⭐ Дополнительные плейсхолдеры шаблонов (`{{media_album}}`, `{{battery}}`, `{{playback}}` и др.)
- [x] ⭐ Выбор режима временной метки (медиа/приложение · текущее время · произвольный ms)
- [x] ⭐ Редизайн главного экрана — карточки во всю ширину с переключателями вкл/выкл
- [x] ⭐ Авто-переподключение gateway с exponential backoff (больше не падает через 1–2 ч)
- [x] ⭐ Защищённый цикл обнаружения — сетевые ошибки больше не останавливают определение игр/приложений
- [x] ⭐ Опрос смены приложений каждые **2 с** (было 5 с)

## Что нового в форке

Все добавления находятся на экране **Экспериментального RPC** и на главном экране.

### ⭐ Подмена платформы
- Выбор платформы, с которой транслируется активность — **Desktop, Web, Android, iOS, Xbox, Playstation, VR** (портировано с platformSpoofer от Equicord).
- Работает на уровне gateway `IDENTIFY` (статус клиента), поэтому корректно подхватывается плагинами вроде **platformindicators**.
- Применяется **мгновенно** — сервис перезапускается сам, ручное переключение не нужно.

### ⭐ Кастомный Application ID (своя иконка)
- Переключатель **Встроенный / Свой**. Встроенный оставляет стандартную иконку; Свой открывает поле для вставки **Application ID** из Discord-приложения, и активность показывает твою иконку и название.

### ⭐ Дополнительные плейсхолдеры шаблонов
В дополнение к оригинальным, с автодополнением:
- `{{media_album}}`, `{{media_album_artist}}`, `{{media_duration}}`, `{{media_writer}}`, `{{media_composer}}`
- `{{battery}}` — уровень заряда батареи
- `{{playback}}` — состояние воспроизведения (▶ / ⏸)

### ⭐ Выбор режима временной метки
- **Медиа / Приложение (по умолчанию)** — временные метки от текущего трека/приложения.
- **Текущее время суток** — Discord показывает текущее время как elapsed (например в 15:30 → показывает «15:30»). Та же логика что и в Vencord customRPC.
- **Произвольный** — вводишь значения **Start / End Timestamp в миллисекундах** вручную.

### ⭐ Редизайн главного экрана
- Карточки функций теперь расположены вертикально во всю ширину экрана.
- **Переключатели вкл/выкл на каждой карточке** — Media / Custom / Experimental RPC можно запускать и останавливать прямо с главного экрана.

### ⭐ Надёжность Gateway (портировано из [YuuzuTH/Kizzy](https://github.com/YuuzuTH/Kizzy))
- Авто-переподключение с exponential backoff при любом неожиданном разрыве соединения.
- Возобновляет сессию когда возможно; после 3 неудачных попыток делает новый identify.
- Фатальные коды (4004, 4010–4014) по-прежнему останавливают соединение навсегда.
- Лимит 60 секунд на ожидание отправки активности (раньше был бесконечный цикл).
- Цикл обнаружения обёрнут в try/catch — одна сетевая ошибка больше не убивает определение игр/приложений до ручного перезапуска.
- Опрос смены приложений: **2 с** (было 5 с).
- Фикс: смена между двумя включёнными приложениями теперь сразу обновляет активность.

### Исправления
- Исправлен краш при нажатии **«Поделиться журналом»** (FileProvider authority теперь совпадает с package id).
- Исправлены крэши при переключении RPC-функций с главного экрана.

## Системные требования
- ОС: Android 8.1 — 16
- ОЗУ: минимум 3 ГБ

## Скачать
Последний подписанный `app-release.apk` находится на странице [**Releases**](../../releases/latest).

**Kizzy Enhanced** использует package id **`com.my.kizzy.enhanced`**, поэтому устанавливается параллельно с оригинальным Kizzy.

> [!WARNING]
> Приложение использует подключение к Discord Gateway. Используй на свой страх и риск.

## Сборка
> Требования: Android Studio, знакомство с Gradle, Kotlin и Jetpack Compose.

```console
git clone https://github.com/milliarderr/Kizzy-Enhanced.git
```
Открой проект в Android Studio, импортируй, затем собери и запусти. Релизные сборки собираются автоматически через GitHub Actions workflow **Build Release Apk**.

## Благодарности
✨ [dead8309](https://github.com/dead8309/Kizzy) — оригинальное приложение и весь апстрим.

✨ [YuuzuTH/Kizzy](https://github.com/YuuzuTH/Kizzy) — фиксы надёжности gateway.

✨ [Read You](https://github.com/Ashinch/ReadYou) и [Seal](https://github.com/JunkFood02/Seal) — UI компоненты

✨ [Material Color Utilities](https://github.com/material-foundation/material-color-utilities)

✨ [Rich-Presence-U](https://github.com/ninstar/Rich-Presence-U) — данные игр Nintendo и Wii U

✨ [Logra](https://github.com/wingio/Logra) — UI логов

✨ [Xbox-Rich-Presence-Discord](https://github.com/MrCoolAndroid/Xbox-Rich-Presence-Discord) — данные игр Xbox

✨ [Monet](https://github.com/Kyant0/Monet) — Material3 палитры

## Лицензия
**Kizzy** — проект с открытым исходным кодом под лицензией GNU GPL 3.0, которая позволяет свободно использовать, изучать и изменять код, но не разрешает распространять изменённый код как закрытое коммерческое ПО.
