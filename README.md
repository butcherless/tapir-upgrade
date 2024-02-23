# Kotlin Research

> Project for research on functional programming through the Kotlin language ecosystem.

![Badge-CI]

## Commands

- ```./gradlew clean test jacocoTestReport```
- ```./gradlew test --tests ValidationModuleTest```

## Links
- https://kotlinlang.org/docs/home.html
- https://arrow-kt.io/

[Badge-CI]: https://github.com/butcherless/kotlin-research/actions/workflows/kotlin-ci.yml/badge.svg

@startuml

artifact artifact1
artifact artifact2
artifact artifact3
artifact artifact4
artifact artifact5
artifact artifact6
artifact artifact7
artifact artifact8
artifact artifact9
artifact artifact10
artifact1 --> artifact2
artifact1 --* artifact3
artifact1 --o artifact4
artifact1 --+ artifact5
artifact1 --# artifact6
artifact1 -->> artifact7
artifact1 --0 artifact8
artifact1 --^ artifact9
artifact1 --(0 artifact10

@enduml
