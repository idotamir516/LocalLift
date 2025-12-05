<!--
  SYNC IMPACT REPORT
  ==================
  Version Change: N/A → 1.0.0 (initial ratification)
  
  Added Principles:
  - I. Simplicity First
  - II. It Just Works
  - III. Minimal Testing
  
  Added Sections:
  - Core Principles (3 principles)
  - Technology Stack
  - Governance
  
  Removed Sections: N/A (initial version)
  
  Templates Requiring Updates:
  - ✅ plan-template.md (no updates needed - template is generic)
  - ✅ spec-template.md (no updates needed - template is generic)
  - ✅ tasks-template.md (no updates needed - template is generic)
  
  Deferred Items: None
-->

# Workout App Constitution

## Core Principles

### I. Simplicity First

Every decision MUST favor the simplest solution that works. This is a personal app for
a single user—complexity is the enemy.

- YAGNI (You Aren't Gonna Need It) is the default stance
- No premature optimization or over-engineering
- If a feature isn't needed today, don't build it
- Prefer built-in Android components over third-party libraries when practical
- Delete code rather than commenting it out

**Rationale**: This app exists to serve one user's workout needs. Time spent on
architecture astronautics is time not spent working out.

### II. It Just Works

Functionality and reliability trump elegance. The app MUST work when the user needs it.

- Features MUST be usable offline (gym environments often have poor connectivity)
- UI MUST be operable with sweaty hands and minimal attention
- Data MUST persist reliably—losing workout history is unacceptable
- Crash-free operation is mandatory; graceful degradation when possible
- Performance matters only where it affects usability (e.g., quick app launch)

**Rationale**: A pretty app that crashes mid-workout is worthless. A simple app that
reliably tracks reps is valuable.

### III. Minimal Testing

Test what matters, skip what doesn't. Heavyweight test infrastructure is overkill for
a personal project.

- Manual testing during development is acceptable
- Automated tests are OPTIONAL and reserved for complex logic (e.g., calculations)
- No mandatory TDD—write tests when they save debugging time
- Integration tests only if a bug keeps recurring
- UI tests are explicitly out of scope

**Rationale**: The cost of a bug is low (only the developer is affected), and the cost
of extensive test infrastructure is high relative to project value.

## Technology Stack

- **Platform**: Android (Kotlin preferred, Java acceptable)
- **Min SDK**: Target reasonably modern devices (API 26+ / Android 8.0+)
- **Architecture**: Keep it simple—no need for Clean Architecture, MVVM, or complex
  patterns unless they solve a real problem
- **Storage**: Room or SQLite for local persistence; no cloud sync required
- **Dependencies**: Minimize third-party libraries; each added dependency MUST justify
  its inclusion

## Governance

This constitution defines the guiding principles for the Workout App. All development
decisions SHOULD align with these principles.

**Amendment Process**:
- Constitution updates require updating this file and incrementing the version
- Version follows semantic versioning: MAJOR.MINOR.PATCH
  - MAJOR: Fundamental principle changes
  - MINOR: New principles or significant clarifications
  - PATCH: Wording fixes, typo corrections

**Compliance**:
- No formal review process required—this is a personal project
- Use common sense: if you're overcomplicating something, step back and simplify

**Version**: 1.0.0 | **Ratified**: 2025-12-03 | **Last Amended**: 2025-12-03
