# BehindTheCreator

BehindTheCreator is a native Kotlin Android app that turns YouTube creators into an encyclopedia of stories, timelines and lesser-known facts.

## Current version

- Native Kotlin + Jetpack Compose UI
- Creator cards with remote profile pictures
- Search by creator name or handle
- Category filters
- Rich creator detail pages
- Career/story summaries
- Lesser-known-facts section
- Career timeline
- Source links that open in the system browser
- MrBeast, Mark Rober, MKBHD and PewDiePie included as starter profiles
- Automated GitHub Actions debug APK build

## Roadmap

- Add many more creators
- Move creator data into a dedicated local database/JSON data layer
- Favorites/bookmarks
- Offline-first profile images and data
- Better fact-level source attribution
- Creator discovery and related creators
- Polished empty/loading/error states
- Release APK signing and Play Store preparation

## Build

Open the project in Android Studio and run the `app` configuration. The project uses Kotlin, Jetpack Compose and Gradle 8.10.2.

Every push to `main` also runs the Android build workflow and uploads a debug APK as a GitHub Actions artifact.
