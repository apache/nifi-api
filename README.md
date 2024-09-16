<!-- SPDX-License-Identifier: Apache-2.0 -->

# Apache NiFi API

[![license](https://img.shields.io/github/license/apache/nifi-api)](https://github.com/apache/nifi-api/blob/main/LICENSE)
[![build](https://github.com/apache/nifi-api/actions/workflows/build.yml/badge.svg)](https://github.com/apache/nifi-api/actions/workflows/build.yml)

The [Apache NiFi](https://nifi.apache.org) API repository contains public interfaces and classes for building extension components.

## Requirements

- Java 21
- Maven 3.9

## Versioning

The project follows [Semantic Versioning 2.0.0](https://semver.org).

## Building

This project uses [Maven](https://maven.apache.org) to build distribution packages.

This project includes the [Maven Wrapper](https://maven.apache.org/wrapper/) to build with required Maven versions.

```
./mvnw install
```

## Verifying

Code and documentation changes must pass build verification to be eligible for review.

```
./mvnw verify
```

The project uses several build plugins for static code analysis and license evaluation.

- [Apache Maven Checkstyle Plugin](https://maven.apache.org/plugins/maven-checkstyle-plugin/)
- [Apache Maven PMD Plugin](https://maven.apache.org/plugins/maven-pmd-plugin/)
- [Apache Release Audit Tool Plugin](https://creadur.apache.org/rat/apache-rat-plugin/)
- [JaCoCo Maven Plugin](https://www.jacoco.org/jacoco/trunk/doc/maven.html)

## Documentation

The [Apache NiFi Documentation](https://nifi.apache.org/documentation/) includes reference information for project capabilities.

## Contributing

The [Apache NiFi Contributor Guide](https://cwiki.apache.org/confluence/display/NIFI/Contributor+Guide)
describes the process for getting involved in the development of this project.

## Issues

This project uses [Jira](https://issues.apache.org/jira/browse/NIFI) for tracking bugs and features.

## Licensing

This project is released under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
