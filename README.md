# Тестовое задание

Разработать Android-приложение, которое позволяет:

* Искать музыку через открытую API ✅
* Воспроизводить найденные треки ✅
* Управлять проигрыванием (пауза, продолжение, следующая/предыдущая песня) ✅
* Воспроизводить локальную музыку и искать по ней в том числе ✅

**Jetpack Compose based!**

## Issues
Background прослушивание ✅, но seekbar с длительностью для перемотки одного трека сделать не вышло на данный момент, навигация по плейлисту работает.

## Технологии
* Навигация через Compose type-safe navigation
* Сеть - Retrofit
* Архитектурный паттерн - попробовал сделать MVI
* DI - чистый Dagger, w/o Hilt

Polishing, конечно, можно делать бесконечно, но торопился сделать все фичи и не упасть в рантайме.

Модуляризации по слоям через package's.

## Запуск
Для запуска: git clone -> build&deploy -> grant permissions -> use

![image](https://github.com/user-attachments/assets/34d43fe4-cc2c-4804-9af4-0601113bec1d)
