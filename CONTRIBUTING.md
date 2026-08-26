# Contributing

Thanks for helping improve ColorPeek.

## Development

Requirements are a JDK capable of running the bundled Gradle wrapper and an internet connection for the first IntelliJ Platform SDK download.

```shell
./gradlew test legacy:test
./gradlew buildReleaseArtifacts
```

Use `./gradlew runIde` to test the Modern distribution in a sandbox IDE. Keep language-specific behavior behind `LanguageColorProvider`; new languages should be added as extension implementations rather than to the composite provider.

Before opening a pull request, include focused tests for parser behavior and confirm that both distributions build. Do not commit IDE state, build output, certificates, keys, tokens, or local environment files.
