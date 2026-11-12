# ParticleFX — 50 анимаций из частиц для Paper 1.21.11

Плагин (Java 21, Maven) добавляет 50 уникальных масштабных анимаций, нарисованных **только частицами**
(без Armor Stand, Display, Falling Block, мобов, ресурспаков и внешних библиотек), меню на стандартном
инвентаре, команды `/particlefx` (`/pfx`) и подробный `config.yml`.

## Дерево проекта
```
ParticleFX/
├─ pom.xml
├─ README.md
├─ docs/EFFECTS.md                       # описание всех 50 эффектов
└─ src/main/
   ├─ resources/{plugin.yml, config.yml}
   └─ java/dev/particlefx/
      ├─ ParticleFXPlugin.java            # главный класс
      ├─ command/ParticleFXCommand.java   # команды и tab-completion
      ├─ core/  AnimationCatalog, AnimationInfo, Animation, AnimationRegistry,
      │         EffectManager (единственный планировщик), EffectSettings, LaunchMode, Range, PluginSettings
      ├─ gui/   MenuManager, MenuHolder
      ├─ util/  Text
      └─ fx/    Frame, Pen, Shapes, Colors, Renderer      # движок частиц и геометрия
                SpaceFx (1–10), ElementFx (11–20), MythFx (21–30), FormFx (31–40), FinalFx (41–50)
```

## Сборка
Нужны JDK 21 и Maven 3.8+:
```
mvn -q clean package
```
Готовый файл: `target/ParticleFX-1.0.0.jar`.

## Установка и проверка (Paper 1.21.11, Java 21)
1. Скопируйте jar в `plugins/`, запустите сервер; в логе: `Загружено анимаций: 50`.
2. В игре: `/particlefx` → выберите анимацию → «Предпросмотр» или «Запустить».
3. `/particlefx list`, `/particlefx play atom`, `/particlefx stop`, `/particlefx reload` (админ).
4. Консоль: `particlefx play supernova <игрок>`.

## Команды
| Команда | Описание |
|---|---|
| `/particlefx`, `/particlefx menu` | открыть меню |
| `/particlefx play <название\|номер> [игрок]` | запустить эффект (чужой игрок — только админ) |
| `/particlefx stop [игрок]` | остановить свои эффекты |
| `/particlefx list` | список анимаций |
| `/particlefx reload` | перезагрузить конфиг (админ) |

## Права
- `particlefx.use` (по умолч. все) — меню, список, предпросмотр, остановка.
- `particlefx.play` (по умолч. все) — запуск эффектов.
- `particlefx.admin` (по умолч. op) — reload, эффекты для других, обход лимитов количества эффектов.

## Производительность
- Одна задача `runTaskTimer` в основном потоке на все эффекты; Bukkit API асинхронно не вызывается.
- Лимиты в `config.yml`: частиц за тик на эффект (600) и на сервер (2500), эффектов на игрока (3) и на сервер (20).
- Резкие «взрывы» отправляются как частицы с нулевым количеством и вектором скорости — полёт считает клиент.
- Эффект останавливается при выходе игрока, выгрузке мира, выключении плагина или ошибке рендера.
- Все значения (GUI, команды, конфиг) ограничиваются диапазонами; NaN/∞ заменяются безопасными.
- Предпросмотр короче, меньше и виден только вам (`preview.*`).
