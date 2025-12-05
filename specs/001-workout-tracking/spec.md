# Feature Specification: Workout Tracking App

**Feature Branch**: `001-workout-tracking`  
**Created**: 2025-12-03  
**Status**: Draft  
**Input**: User description: "Build an app that allows me to track my workouts as I do them and over time. Workouts are weight lifting and calisthenics, consisting of a name, exercises with sets and reps. Define workout templates in advance, then select and track during workout. All data saved locally on device."

## Assumptions

- Single user app (no accounts, authentication, or multi-device sync needed)
- Weight is recorded in pounds (lbs)
- Calisthenics exercises may have zero weight (bodyweight only)
- Templates can be reused indefinitely
- Templates are starting points; exercises and sets can be reordered, added, or removed during a workout
- Workout history is append-only (completed workouts are not edited, only viewed)
- Rest timers can be optionally specified per set in templates and modified during workouts
- Rest timer triggers automatically after a set is recorded and plays an audio cue when finished
- A static exercise library is available for selecting exercises (not user-created)
- Weight and reps fields start empty each set (no pre-filling from previous workouts)

## Clarifications

### Session 2025-12-03

- Q: Should weight fields pre-fill from previous workouts? → A: No, always start empty (user enters fresh each time)
- Q: What navigation structure for the app? → A: Bottom navigation bar with 3 tabs (Start Workout, Templates, History)
- Q: How to display exercises during active workout? → A: Scrollable list with all exercises visible, inline set rows to tap and fill
- Q: How to enter reps/weight for a set? → A: Inline text fields editable directly in the list row (no dialog)
- Q: Allow reordering exercises? → A: Yes, both in templates and during active workout (templates are a starting point, fully modifiable during workout)

### Post-Clarification Additions (2025-12-03)

- Weight unit clarified: Pounds (lbs)
- Rest timer requirement added: Optional per-set timer with automatic trigger and audio cue
- Exercise library requirement added: Static library for exercise selection

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Execute a Workout from Template (Priority: P1)

I want to start a workout session by selecting a pre-defined template, then log my sets and reps as I go through each exercise. When I finish, the workout is saved with all the data I entered.

**Why this priority**: This is the core use case—the primary reason the app exists. Without this, there's no workout tracking.

**Independent Test**: Can be fully tested by selecting a template, logging a few sets with reps/weight, finishing the workout, and verifying the data persists after app restart.

**Acceptance Scenarios**:

1. **Given** I have at least one workout template saved, **When** I select it and start a workout, **Then** I see all the exercises from that template ready to log
2. **Given** I'm in an active workout session, **When** I tap on a set for an exercise, **Then** I can enter the reps completed and weight used
3. **Given** I'm in an active workout session, **When** I finish and save the workout, **Then** it is persisted with the date, all exercises, and all sets logged
4. **Given** I have completed a workout, **When** I restart the app, **Then** the completed workout still appears in my history

---

### User Story 2 - Create and Manage Workout Templates (Priority: P2)

I want to create workout templates that define which exercises I'll do and the target sets for each. I can edit or delete templates as my routine evolves.

**Why this priority**: Templates are required before Story 1 can be used, but a hardcoded template could allow Story 1 to work initially. This is the second thing to build.

**Independent Test**: Can be tested by creating a new template with 3-4 exercises, verifying it appears in the template list, editing it, and deleting it.

**Acceptance Scenarios**:

1. **Given** I'm on the templates screen, **When** I tap "Create Template," **Then** I can enter a workout name and add exercises with target set counts
2. **Given** I'm creating a template, **When** I add an exercise, **Then** I can specify the exercise name and number of sets
3. **Given** I have existing templates, **When** I select one to edit, **Then** I can modify the name, add/remove exercises, or change set counts
4. **Given** I have existing templates, **When** I delete a template, **Then** it no longer appears in my list (but past workouts using it are preserved)

---

### User Story 3 - View Workout History (Priority: P3)

I want to see my past workouts so I can review what I did on previous days and track my progress over time.

**Why this priority**: Viewing history provides value but isn't essential for the core tracking flow. Can be deferred until basic tracking works.

**Independent Test**: Can be tested by completing 2-3 workouts on different days, then viewing the history list and drilling into one workout to see full details.

**Acceptance Scenarios**:

1. **Given** I have completed workouts, **When** I open the history screen, **Then** I see a list of past workouts with date and workout name
2. **Given** I'm viewing workout history, **When** I tap on a past workout, **Then** I see the full details: all exercises, sets, reps, and weights logged
3. **Given** I have no completed workouts, **When** I open the history screen, **Then** I see an empty state message

---

### User Story 4 - Quick Add Exercise During Workout (Priority: P4)

Sometimes I want to do an exercise that wasn't in my template. I should be able to add an exercise on the fly during an active workout.

**Why this priority**: Nice to have for flexibility, but most workouts will follow the template. Can be added after core stories work.

**Independent Test**: Can be tested by starting a workout, adding a new exercise mid-session, logging sets for it, and verifying it saves with the workout.

**Acceptance Scenarios**:

1. **Given** I'm in an active workout, **When** I tap "Add Exercise," **Then** I can enter an exercise name and start logging sets for it
2. **Given** I added an exercise during a workout, **When** I finish and save, **Then** the ad-hoc exercise appears in the saved workout history

---

### Edge Cases

- What happens if the app is killed mid-workout? → Auto-save progress so the workout can be resumed
- What happens if I try to start a workout with no templates? → Show a message prompting to create a template first
- What happens if I leave a set blank (no reps entered)? → Treat as skipped/not performed (save as 0 or null)
- What happens if I enter weight but no reps? → Require at least reps to save a set
- What happens if I delete a template that was used in past workouts? → Past workouts retain their data; only the template is removed

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: App MUST allow creating workout templates with a name and list of exercises
- **FR-002**: Each exercise in a template MUST have a name and a target number of sets
- **FR-003**: App MUST allow starting a workout session from a selected template
- **FR-004**: During a workout, user MUST be able to log reps and weight for each set of each exercise
- **FR-005**: App MUST save completed workouts with date, workout name, and all logged set data
- **FR-006**: App MUST persist all data locally on the device (survives app restart/device reboot)
- **FR-007**: App MUST display a list of completed workouts in reverse chronological order
- **FR-008**: App MUST allow viewing full details of any past workout
- **FR-009**: App MUST allow editing and deleting workout templates
- **FR-010**: App MUST auto-save workout progress periodically to prevent data loss on crash
- **FR-011**: App MUST work fully offline (no network connectivity required)
- **FR-012**: App MUST allow adding exercises to an active workout that weren't in the template
- **FR-013**: App MUST use bottom navigation with 3 tabs: Start Workout, Templates, History
- **FR-014**: Active workout screen MUST display all exercises in a scrollable list with inline set entry rows
- **FR-015**: Set entry MUST use inline text fields for reps and weight, editable directly in the list without dialogs
- **FR-016**: App MUST allow reordering exercises within templates
- **FR-017**: App MUST allow reordering exercises during an active workout
- **FR-018**: App MUST allow modifying set counts during an active workout (add/remove sets per exercise)
- **FR-019**: App MUST support specifying a rest timer duration (in seconds) per set in templates
- **FR-020**: App MUST allow modifying rest timer duration during an active workout
- **FR-021**: App MUST automatically trigger rest timer after a set is recorded
- **FR-022**: App MUST play an audio cue when rest timer finishes
- **FR-023**: App MUST provide a static exercise library to choose from when creating templates or adding exercises
- **FR-024**: App MUST record weight in pounds (lbs)

### Key Entities

- **Workout Template**: A reusable starting point for a workout; has a name and an ordered list of exercise definitions (exercise name + target sets). Templates are not rigid—they provide defaults that can be fully modified during the workout
- **Workout Session**: A single instance of performing a workout; has a date, reference to template used (or "custom"), and the actual logged data
- **Exercise Log**: Record of performing one exercise in a session; has exercise name and a list of set logs
- **Set Log**: Single set performed; has reps completed, weight used (in pounds, can be 0 for bodyweight exercises), and optional rest timer duration (in seconds)
- **Exercise Library**: Static collection of predefined exercises available for template and workout creation

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: User can create a template and start tracking a workout within 2 minutes of first app launch
- **SC-002**: Logging a single set (reps + weight) takes under 5 seconds
- **SC-003**: App launches and is ready to use within 3 seconds
- **SC-004**: No workout data is lost due to app crashes (auto-save ensures recovery)
- **SC-005**: User can find and view any past workout within 10 seconds
- **SC-006**: App remains functional with 1 year of daily workouts stored (365+ workout records)
