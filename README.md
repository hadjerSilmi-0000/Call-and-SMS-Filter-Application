# Call & SMS Filter — Android App

> University project — SEM Module | ING4 | Université M'Hamed Bougara de Boumerdès  
> Instructor: Mr. BITIT

An Android application that filters incoming phone calls and SMS messages based on user-managed blacklists and whitelists. Numbers on the blacklist are automatically rejected; only numbers on the whitelist are allowed through when the whitelist is active.

---

## Features

- **Blacklist management** — add or remove phone numbers that should be blocked
- **Whitelist management** — add or remove phone numbers that are allowed to communicate
- **Automatic call rejection** — incoming calls from blacklisted numbers are silently rejected using the `TelecomManager` API (Android 9+) with a reflection-based fallback for older devices
- **SMS blocking** — SMS messages from blacklisted numbers are intercepted before reaching the default messaging app
- **Alert service** — plays an audio alert when a blocked SMS is received, with a toast notification showing the sender's number
- **Persistent storage** — blacklist and whitelist are saved to plain text files (`blacklist.txt`, `whitelist.txt`) in the app's internal storage, so they survive app restarts
- **Number normalization** — handles both local format (`0XXXXXXXXX`) and international format (`+213XXXXXXXXX`) transparently
- **Runtime permissions** — requests all required permissions at launch with proper rationale dialogs and a settings fallback for permanently-denied permissions

---

## Architecture

```
app/src/main/java/com/example/homeworksem/
├── MainActivity.java      — UI for managing blacklist and whitelist
├── CallReceiver.java      — BroadcastReceiver: intercepts incoming calls
├── SmsReceiver.java       — BroadcastReceiver: intercepts incoming SMS
├── MyService.java         — Service: plays alert audio on blocked SMS
└── FileHelper.java        — Utility: reads/writes number lists from files
```

### Component responsibilities

| Component | Type | Role |
|---|---|---|
| `MainActivity` | Activity | Displays and edits the blacklist and whitelist |
| `CallReceiver` | BroadcastReceiver | Captures `PHONE_STATE` broadcasts, rejects blacklisted/non-whitelisted calls |
| `SmsReceiver` | BroadcastReceiver | Captures `SMS_RECEIVED` broadcasts, blocks messages and starts the alert service |
| `MyService` | Service | Plays a looping alert sound; stopped manually via the UI |
| `FileHelper` | Utility class | Reads and writes `blacklist.txt` and `whitelist.txt`; handles number format normalization |

---

## Permissions

```xml
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.CALL_PHONE" />
<uses-permission android:name="android.permission.ANSWER_PHONE_CALLS" />
<uses-permission android:name="android.permission.READ_CALL_LOG" />
<uses-permission android:name="android.permission.RECEIVE_SMS" />
<uses-permission android:name="android.permission.READ_SMS" />
```

> Note: `ANSWER_PHONE_CALLS` is a runtime permission requiring explicit user approval. On some devices, call rejection may require granting the app as the default dialer or additional system-level access.

---

## How It Works

### Call filtering
1. `CallReceiver` listens for `android.intent.action.PHONE_STATE`
2. When the phone starts ringing, it reads the incoming number
3. It checks `blacklist.txt` and `whitelist.txt` via `FileHelper`
4. If the number is blacklisted **or** not in the whitelist (when the whitelist is non-empty), `TelecomManager.endCall()` is called to reject it

### SMS filtering
1. `SmsReceiver` listens for `android.provider.Telephony.SMS_RECEIVED` at priority 1000
2. It reads the sender address and checks it against both lists
3. If blacklisted → starts `MyService` (plays alert, shows toast) and calls `abortBroadcast()` to suppress the message
4. If not whitelisted → calls `abortBroadcast()` silently

### Number storage
Lists are stored as newline-separated plain text in the app's private files directory (`context.getFilesDir()`). The app does not require internet access or external storage.

---

## Setup & Build

**Requirements:** Android Studio, Java 11+, minSdk 24 (Android 7.0), targetSdk 36

1. Clone or open the project in Android Studio
2. Place an MP3 file named `alert.mp3` under `app/src/main/res/raw/`
3. Build and run on a physical device (call/SMS features do not work on emulators without special configuration)
4. Grant all requested permissions when prompted

---


## Authors

*Université M'Hamed Bougara de Boumerdès — Faculty of Sciences, Department of Computer Science*
