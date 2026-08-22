# RideOS — Project Definition

## Product

RideOS is an Android application intended to act as a configurable cycling computer, with special support for electric-bike telemetry.

## MVP

1. Dashboard
2. BLE device layer
3. VESC telemetry abstraction
4. GPS ride tracking
5. Ride recording
6. Local ride statistics

## Architecture principles

- Separate UI, domain logic, data, and device communication.
- Keep VESC protocol handling independent from the UI.
- Avoid coupling the app to one motor, controller, or bicycle configuration.
- Build testable interfaces around BLE and telemetry sources.

## Future scope

- Intelligent range estimation
- Route planning
- Battery analytics
- Diagnostics
- Alerts and safety features
- AI-assisted troubleshooting
