# Changelog

All notable changes to ColorPeek are documented here.

## [1.1.0] - 2026-08-31

- Optional eight-digit ARGB numeric color previews for Java and Kotlin, preserving suffixes, separators, and surrounding conversions.
- Persistent string/numeric controls and default avoidance of resolved Compose Color calls.
- Constrain the description preview image to prevent it from overflowing JetBrains Marketplace and IDE plugin layouts.

## [1.0.0] - 2026-08-26

- Java color previews for string `PsiLiteralExpression` values.
- Kotlin color previews for literal `KtStringTemplateExpression` entries.
- Gutter swatches with click-to-edit IDE color picker integration.
- Support for `#RGB`, `#ARGB`, `#RRGGBB`, `#AARRGGBB` and equivalent `0x`/`0X` forms.
- PSI-based color write-back that preserves source formatting and promotes RGB to ARGB when transparency is selected.
- Java and Kotlin controls under **Settings/Preferences → Tools → ColorPeek** with persistent settings.
- Kotlin K1/K2 compatibility in the Modern distribution and Legacy support for IntelliJ Platform builds `232–241.*`.
- Extensible `LanguageColorProvider` architecture for future languages.

[1.1.0]: https://github.com/DevCodexStudio/ColorPeek/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/DevCodexStudio/ColorPeek/releases/tag/v1.0.0
