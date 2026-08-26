# JetBrains Marketplace release checklist

## Owner information required before the first upload

- [x] Use the MIT open-source license and include its `LICENSE` file.
- [x] Replace repository-owner placeholders with `DevCodexStudio`.
- [x] Add the vendor website and contact email to both `plugin.xml` files.
- [x] Create the `DevCodexStudio` JetBrains Marketplace Vendor, accept the Developer Agreement, and declare Non-trader status.
- [x] Confirm `https://devcodexstudio.github.io/ColorPeek/` after the repository is public and the GitHub Pages workflow completes.
- [x] Enable GitHub private vulnerability reporting so `SECURITY.md` has a private reporting destination.

## Build and verification

```shell
./gradlew clean legacy:clean test legacy:test buildReleaseArtifacts
```

- [x] Complete a clean build, unit tests, plugin configuration checks, and both distribution packages locally.
- [ ] Run JetBrains Plugin Verifier against the intended IDE/Android Studio builds for each ZIP.
- [ ] Manually install the Legacy ZIP on an `AI-232` Android Studio build.
- [ ] Manually install the Modern ZIP on a K2-enabled IDE.
- [ ] Confirm the gutter swatch, picker, PSI write-back, language settings, and restart persistence.

## Marketplace upload

Upload both files under the same plugin entry and stable channel:

- `build/distributions/ColorPeek-1.0.0.zip` — build `242+`.
- `legacy/build/distributions/ColorPeek-1.0.0-legacy.232.zip` — builds `232–241.*`.

Select accurate tags, add screenshots of the gutter preview/editing experience in currently supported languages and the settings page, and link the public source repository and license. Describe Java and Kotlin as current support and Rust, Python, and other languages as roadmap items rather than available features. Keep both compatibility ranges unchanged so Marketplace selects the correct artifact.

## Optional signing and automated publishing

For automated Marketplace publishing, configure repository secrets rather than local files:

- `PUBLISH_TOKEN`
- `CERTIFICATE_CHAIN`
- `PRIVATE_KEY`
- `PRIVATE_KEY_PASSWORD`

Never commit these values. Plugin signing is recommended; Marketplace applies its own signature after upload as well.
