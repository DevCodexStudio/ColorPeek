# ColorPeek

[![Build](https://github.com/DevCodexStudio/ColorPeek/actions/workflows/build.yml/badge.svg)](https://github.com/DevCodexStudio/ColorPeek/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

[Website](https://devcodexstudio.github.io/ColorPeek/) · [Releases](https://github.com/DevCodexStudio/ColorPeek/releases) · [Source](https://github.com/DevCodexStudio/ColorPeek)

ColorPeek provides Java and Kotlin code with the gutter color preview and editing experience familiar from Android Studio resource files.

Color values are shown as swatches beside the source line. Click a swatch to open the IDE color picker; ColorPeek writes the selected value back through PSI while preserving the original prefix, letter case, and compact/full representation where possible.

## Preview

![ColorPeek gutter preview and color picker](docs/images/ColorPeekPreview.png)

## Features

- Gutter color previews for Java string literals and Kotlin literal string templates.
- Click-to-edit integration with the IDE color picker.
- PSI-based source updates performed inside a write action.
- Independent Java and Kotlin enable/disable controls.
- Kotlin K1 and K2 support on compatible IDE versions.
- Extensible language-provider architecture for future language support.

## Supported color formats

| Prefix | Formats |
| --- | --- |
| `#` | `#RGB`, `#ARGB`, `#RRGGBB`, `#AARRGGBB` |
| `0x` / `0X` | `0xRGB`, `0xARGB`, `0xRRGGBB`, `0xAARRGGBB` |

When transparency is selected for an RGB value, ColorPeek promotes it to ARGB so the alpha channel is preserved. For example, `#9E2525` may become `#CC9E2525`.

## Settings

Open **Settings/Preferences → Tools → ColorPeek** to enable or disable ColorPeek independently for Java and Kotlin.

## Compatibility

Releases contain two distributions with the same plugin ID. JetBrains Marketplace selects the correct one automatically:

- **Modern:** IntelliJ Platform `242+`, including Kotlin K1/K2 support.
- **Legacy:** IntelliJ Platform `232–241.*`.

## Language roadmap

- [x] Java
- [x] Kotlin
- [ ] Rust
- [ ] Python
- [ ] JavaScript / TypeScript
- [ ] Go
- [ ] C / C++
- [ ] C#
- [ ] More IntelliJ Platform languages requested by the community

## Development

```shell
./gradlew test legacy:test
./gradlew runIde
./gradlew buildReleaseArtifacts
```

New languages implement `LanguageColorProvider` and register against `online.devcodex.colorpeek.languageColorProvider`; the composite provider requires no changes.

See [CONTRIBUTING.md](CONTRIBUTING.md) for development guidance, [CHANGELOG.md](CHANGELOG.md) for release notes, and [MARKETPLACE.md](MARKETPLACE.md) for the publication checklist.
