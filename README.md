# BehindTheCreator

BehindTheCreator is a native Kotlin Android YouTuber encyclopedia. Creator content is separate from the UI: each creator entry is primarily a **YouTube channel URL**, while the app fetches current channel information and displays the custom stories, facts and timelines stored in the data file.

## Creator data

Add creators in `app/src/main/assets/creators.json`.

Each entry needs a `channelUrl`. The other fields are the information you control:

```json
{
  "channelUrl": "https://www.youtube.com/@Example",
  "category": "Technology",
  "story": "The creator's story...",
  "facts": ["Interesting fact 1", "Interesting fact 2"],
  "timeline": ["2015 — Started the channel", "2020 — Major milestone"]
}
```

When `YOUTUBE_API_KEY` is configured, the app uses the YouTube Data API to fetch the channel name, handle, profile picture, subscriber count and channel creation date. Without an API key it falls back to YouTube oEmbed for basic name and thumbnail data; subscriber count and join date require the Data API.

## Local API key setup

Create `local.properties` in the project root and add:

```text
YOUTUBE_API_KEY=your_key_here
```

Do **not** commit `local.properties` or the key. Restrict the API key where possible.

## Current app

- Native Kotlin + Jetpack Compose
- URL-driven creator data
- YouTube channel fetching
- Search and category filters
- Creator stories, facts and timelines
- Subscriber count and join date when API data is available
- Direct link to the original YouTube channel
- GitHub Actions Android build

The goal is that adding a creator means editing the data file, not rewriting the Android UI.
