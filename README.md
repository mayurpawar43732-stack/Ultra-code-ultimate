# Ultra Code Ultimate

An Android-first AI coding assistant project.

## Current starter features
- Jetpack Compose dark coding-studio interface
- Gemini, Groq, OpenRouter, and Cloudflare Workers AI provider settings
- API credentials encrypted locally with Android Keystore
- Provider connection tests and a prompt-to-code workspace
- Local GGUF model download manager

## Build status and limitations
This repository contains an early Android starter, not a completed autonomous Codex replacement. It can request code from a configured provider and display the response. It does **not** yet safely edit arbitrary project files, execute generated code, run tests, build APK/EXE projects from prompts, or perform autonomous multi-step agent workflows.

The local-model screen can queue downloads, but an Android inference runtime is not integrated yet, so downloaded models do not currently generate responses offline.

## Android project
Open this repository in Android Studio and open the project root. Use JDK 17 and Android SDK 35. Build the debug variant with the Gradle task `assembleDebug`.

## API key safety
Never commit API keys, passwords, or account tokens. Add keys inside the app's provider settings; they are encrypted on the device. For Cloudflare, both an account ID and API token are required.
