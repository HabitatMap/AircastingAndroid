# Project Context

We are adjusting the existing Android app (Kotlin) to support the new V2 firmware for the AirBeam Mini device. This does NOT concern AirBeam 3 or AirBeam 2 (non-syncable). All changes are outlined in `.context/ble_mobile_app_guide.md` — always reference it when reasoning.

## Key Rules
- **Always** build your changes before commiting them.
- **Always** commit your changes.
- **Minimal changes**: Make the app compatible with minimal code changes — no major refactors or overhauls.
- **Backward compatibility**: The existing AirBeam Mini V1 firmware implementation must remain fully intact and operational.
- **Always commit and build**: Always commit your changes and build them before finishing.
- **Concise responses**: Keep answers to the point — don't make them too long.
- **Kotlin best practices**: When creating new code, follow Kotlin best practices and prefer functional style.
