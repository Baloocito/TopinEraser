# TopinEraser 🖼️✂️

**TopinEraser** is an Android app by [TopinGames](https://github.com/Baloocito) that removes image backgrounds completely on-device — no internet, no API, no cost per image.

## Features

- 🔒 **100% offline** — uses Google ML Kit Subject Segmentation
- 💾 **Save as PNG** with transparent background
- ⚡ **Fast** — on-device inference, no uploads
- 🎨 **Clean Material You UI** (Jetpack Compose)

## Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose + Material 3 |
| DI | Hilt |
| ML | ML Kit Subject Segmentation |
| Navigation | Navigation Compose |
| Image loading | Coil |

## Architecture

```
app/
├── data/
│   ├── repository/       # BackgroundRemovalRepository
│   └── source/           # MlKitBackgroundRemovalSource
├── domain/
│   ├── model/            # EraserResult
│   └── usecase/          # RemoveBackgroundUseCase
├── ui/
│   ├── navigation/       # NavGraph
│   ├── screens/
│   │   ├── home/         # HomeScreen + HomeViewModel
│   │   └── editor/       # EditorScreen + EditorViewModel
│   └── theme/
├── di/                   # AppModule (Hilt)
└── util/                 # BitmapHolder, ImageSaver
```

## Getting Started

1. Clone the repo
2. Open in Android Studio Hedgehog or newer
3. Build & run on Android 8.0+ (API 26+)

> No API keys required. Everything runs locally.

## License

MIT © TopinGames / Xiombo
