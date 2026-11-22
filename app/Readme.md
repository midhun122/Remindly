# Remindly

**Reminders that behave like alarms.** Remindly is a native Android reminder app that rings when it's time, keeps unfinished reminders visible, and stays free of ads, paywalls and mandatory accounts.

> **Status: early development.** This README is temporary and will be replaced with a full version, with screenshots, once the core is finished.

---

## Why this exists

My dad wanted a simple reminder app that would actually ring like an alarm. Most apps I tried had ads, locked useful features behind subscriptions, or only showed a quiet notification that was easy to miss. Remindly is my attempt to build the version I wished existed.

## Principles

- Free, with no ads
- No mandatory account or login
- Offline-first: alarms never depend on the internet
- Reliable alarm delivery over a long feature list
- Simple enough for non-technical users

## What works today

- **Reminders:** add, edit, delete and complete, with date and time pickers and quick picks (In 1 hour, This evening, Tomorrow morning). Saved on the phone.
- **Screens:** Today (with an Up next card), Upcoming, Missed, and Settings.
- **Themes:** system, light and dark, saved between launches.
- **Alarm engine (test only):** the Settings screen has an Alarm check card and a test alarm that rings with the app closed. It plays on the alarm volume stream, vibrates, opens a full-screen alarm over the lock screen, offers Dismiss and Snooze, and stops by itself after 60 seconds. Alarms are set again after a restart.

## Not built yet

- **Real reminders don't ring yet.** Only the test alarm does. Connecting reminders to the alarm engine is the next step.
- Recurring reminders
- Notification-only mode (as an alternative to a ringing alarm)
- Follow-through (keeping a reminder pending until it's marked done) and a full Missed workflow
- Room database (reminders currently use temporary JSON storage)
- Quick-add with natural language and voice, pre-reminders, categories, widget
- Optional backup, sync and shared alarms (later)

## Tech stack

Kotlin, Jetpack Compose, Material 3, DataStore, AlarmManager (`setAlarmClock`), a media-playback foreground service, Gradle Kotlin DSL. Min SDK 26, target SDK 37.

## Project structure

```
app/src/main/java/com/midhun/remindly/
  MainActivity.kt        entry point, theme and edge-to-edge setup
  RemindlyApp.kt         app shell: tabs, actions, add/edit sheet
  alarm/                 scheduler, receiver, foreground service, alarm screen,
                         notifications, permission checks, boot recovery
  data/                  Reminder model and on-device storage
  ui/components/         bottom bar, reminder row, Up next card, add/edit sheet,
                         alarm check card
  ui/screens/            Today, Upcoming, Missed, Settings
  ui/theme/              colors, typography, shapes
  util/                  time formatting helpers
```

## Build and run

1. Open the project in Android Studio and let Gradle sync.
2. Connect an Android phone with USB debugging enabled (use a real phone; emulators are poor at proving alarm reliability).
3. Press Run.

To make an installable file for another phone: **Build > Build Bundle(s) / APK(s) > Build APK(s)**. The file is at `app/build/outputs/apk/debug/app-debug.apk`. Copy it to the other phone (USB cable, Google Drive link or Bluetooth), open it, and allow installing from that source when Android asks.

## Testing alarms on a phone

Alarm reliability is the main thing this app has to get right, so every new phone gets this test. Phones from Xiaomi, Oppo, Vivo, Realme and Samsung are the most likely to stop background apps.

1. Open **Settings** in Remindly. In the **Alarm check** card, tap **Allow** on each item that isn't ready, then return until every row shows a check. Turn the alarm volume to at least 50%.
2. Tap **Ring in 10 seconds** and keep the app open. It should ring. Dismiss it.
3. Tap **In 1 minute**, swipe Remindly away from recent apps, and lock the phone. It should ring, light the screen and show the alarm over the lock screen.
4. Repeat step 3 with battery saver on.
5. Set a 1-minute alarm and restart the phone before it rings. After the restart you should get a "Missed" notification, not silence. Then try again with more time left, and the alarm should still ring after the restart.
6. Let one alarm ring for 60 seconds without touching it. It should stop and leave a "Missed" notification.

If an alarm doesn't ring, check the alarm volume, Do Not Disturb, and the phone's battery setting for Remindly (set it to **No restrictions** or **Unrestricted**; the menu name varies by brand). Some brands also have an auto-start or "allow background activity" switch.

### Device test log

| Phone (brand, model) | Android version | Settings all checked | Rang with app open | Rang after swipe-away, locked | Rang with battery saver | Survived restart | 60-second stop | Notes |
|---|---|---|---|---|---|---|---|---|
| | | | | | | | | |
| | | | | | | | | |

## Roadmap

1. Connect reminders to alarms, with alarm or notification mode
2. Follow-through and the Missed workflow
3. Room database
4. Recurring reminders and configurable snooze
5. Reliability Guard as a first-run setup
6. Quick-add with natural language and voice, pre-reminders, widget
7. Optional backup, sync and shared alarms

## License

To be decided.