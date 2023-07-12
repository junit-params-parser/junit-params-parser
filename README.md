[![License](https://img.shields.io/github/license/junit-params-parser/junit-params-parser)](https://github.com/junit-params-parser/junit-params-parser/blob/main/LICENSE)
[![CI Status: GitHub Actions](https://github.com/junit-params-parser/junit-params-parser/actions/workflows/check-branch.yml/badge.svg)](https://github.com/junit-params-parser/junit-params-parser/actions/workflows/check-branch.yml)

# junit-params-parser
Library for parse arguments in parameterized tests

# Contributing

## Testing changes

After making changes, you need to check code:
1. Check code style `./gradlew clean check -x test -Pcheckstyle=true`
2. Check tests `./gradlew test`
3. Publish to local Maven cache: `./gradlew publishToMavenLocal -Pversion=0.1.0`
