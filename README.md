# Ultra Code (Android)

An Android-first AI coding workspace inspired by agentic coding tools.

## Current implementation

- Jetpack Compose dark UI with Workspace, Local AI and API Keys tabs.
- Bring your own Gemini, Groq, OpenRouter or Cloudflare Workers AI credentials.
- API credentials are encrypted at rest with Android Keystore (AES-GCM).
- Pick a project folder with Android's Storage Access Framework.
- Browse supported source/configuration files in the selected folder.
- Read and edit the selected file in the app.
- Ask the configured AI provider to produce a complete replacement for the selected file.
- Review and edit the proposed replacement before explicitly applying it.
- Remembers the selected folder URI when Android grants persistent access.
- GitHub Actions workflow attempts a debug APK build and uploads the APK artifact when successful.

## Android build

- Android Studio / Android Gradle Plugin 8.9.2
- JDK 17
- Android SDK 35
- Gradle 8.11.1
- Open the repository root in Android Studio and run the `assembleDebug` task.
- CI: open the Actions tab and inspect the latest **Android CI** run. A downloadable APK appears only if the build succeeds.

## Important: current scope (not a complete Codex replacement yet)

This is an early Android coding workspace. It is **not** yet a full autonomous Codex replacement.

Not implemented:
- Multi-step agent tool-calling loop with planning, tool execution, and verification.
- Safe terminal/shell command execution, test running, Gradle builds, or debugging of the selected project.
- Multi-file patch generation and atomic multi-file apply/rollback.
- Git status, diff, commit, branch, and pull-request workflows.
- APK/AAB/EXE generation for arbitrary user projects from inside the app.
- Integrated offline GGUF inference runtime. The Local AI screen is currently a model-download concept, not a working offline inference engine.
- Background agent jobs, task history, checkpoints, and resumable sessions.

Android apps run in a sandbox. This app cannot silently run arbitrary desktop commands or create Windows EXE files on the phone. A practical full-featured version will need a secure remote build/agent runner or a carefully sandboxed on-device execution environment, plus explicit user approval for file writes and commands.

## API key safety

Never commit API keys, passwords, or account tokens. Enter credentials in the app. Cloudflare requires both an account ID and API token. Provider availability, model names, quotas and pricing are controlled by each provider.

## Safety model

AI-generated file changes are shown as a draft. The user must review and press **Apply reviewed change to file** before the selected file is overwritten. Back up important projects before testing.
