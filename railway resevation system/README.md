<p align="center">
  <img src="/assets/logo_irctc.png" alt="Railway Booking System Logo" width="600"/>
</p>

[![Java](https://img.shields.io/badge/Language-Java-blue?style=flat-square)](https://www.java.com/) [![Maven](https://img.shields.io/badge/Build-Maven-%23007d9c?style=flat-square)](https://maven.apache.org/) [![BCrypt](https://img.shields.io/badge/Security-BCrypt-orange?style=flat-square)](https://en.wikipedia.org/wiki/Bcrypt) [![License-MIT](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE) [![Version](https://img.shields.io/badge/Version-v1.0.0-brightgreen?style=flat-square)](#)

# Railway Booking System

A production-grade console-based reservation system implemented using Java and JSON for local persistence. This repository is prepared for distribution and deployment.

## Features
- Secure BCrypt-based user authentication
- 30-day seat availability per train
- Ticket booking and cancellation with persistence
- Console-based interactive UI

## Quick Start
1. Build the project: `mvn clean install`  
2. Run: `mvn exec:java -Dexec.mainClass="com.irctc.App"`

## Release
See `RELEASE.md` for v1.0.0 release notes.

---
