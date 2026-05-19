---
name: yousells-dev
description: Use for any development, planning, coding, testing, refactoring, or release work inside the YouSells project. This skill defines the project's required engineering workflow: clarify requirements first, record development planning and logs, implement module code only after scope is fixed, write automated tests immediately after each module is completed, and explicitly remind developers to perform frontend/browser联调 when a change affects UI or integration behavior.
---

# YouSells Development Skill

Follow this workflow for all work inside `YouSells`.

## Core Rule

Treat `YouSells` as a formal internal software project, not a casual demo.

Every task should follow this order:

1. Clarify the requirement
2. Confirm the development scope
3. Record the plan or change log
4. Implement the module
5. Write automated tests immediately
6. Run verification
7. If the change affects UI, interaction, or integration, explicitly remind the developer to do frontend/browser联调

Do not jump straight into code without first making the requirement and task boundary clear.

## Project Context

- Team size: `5`
- Core stronger developers: `秦梓源`, `志明`
- Team development style: `Vibe Coding`
- Delivery target: internal formal Web platform
- User scale target: `50-100`
- Engineering direction: follow enterprise-style workflow and quality discipline

## Required Workflow

### 1. Requirement First

Before coding, always identify:

- What module is being changed
- What exact problem is being solved
- Whether the task belongs to `P0`, `P1`, or later
- Whether it changes backend only, frontend only, or both
- Whether it changes schema, API contract, permission logic, or deployment behavior

If the scope is not yet explicit, stop and define it in a doc first.

### 2. Planning First

Before implementation, record:

- task objective
- module owner
- affected files or modules
- dependencies
- verification method

Use the project docs under `docs/` to keep planning visible.

Before parallel module work, also align with:

- `docs/11_后端架构收口与协作边界规范.md`
- `docs/06_P0开发任务分配方案.md`
- `docs/05_开发流程与日志规范.md`

### 3. Development Log

For meaningful tasks, keep a short development log that records:

- date
- owner
- task
- current status
- blockers
- next action

If no dedicated log file exists yet for the task, create or extend one under `docs/logs/`.

### 4. Implementation Discipline

When implementing:

- keep module boundaries clear
- do not mix unrelated changes
- do not bypass agreed conventions
- prefer stable, maintainable code over clever shortcuts
- if changing shared contracts, update docs immediately

For backend work, additionally enforce:

- business modules stay inside their owned directories
- cross-module calls go through `service` interfaces only
- do not place business logic in controllers
- do not add new persistence work without `entity / mapper / convert`
- dashboard aggregation belongs only in `modules/dashboard/**`

### 5. Testing Is Mandatory

After finishing a module, write automated tests immediately.

This is not optional.

At minimum:

- backend module changes must have unit tests
- service logic changes must have correctness tests
- controller or API behavior should have integration-style verification where practical
- utility changes must have focused tests

Do not leave testing as “later work” unless the user explicitly pauses the task.

### 6. 联调 Reminder Rule

If a task affects any of the following, explicitly remind the developer to perform frontend/browser联调:

- UI rendering
- form submission
- table display
- auth flow
- permission behavior
- API contract used by frontend
- any page-level interaction

This reminder should be explicit, not implied.

### 7. Acceptance Mindset

Before considering a task complete, verify:

- requirement is met
- code compiles or runs
- tests pass, or any failure is clearly explained
- docs are updated if behavior changed
- frontend/browser联调 is called out when needed

## Team-Specific Expectations

### For Qin Ziyuan

- own infrastructure, project baseline, engineering rules, and final acceptance
- own architecture direction, repo conventions, CI/testing expectations, and release standards

### For Module Developers

- own their module implementation
- own tests for their module
- own local self-check before asking for acceptance
- do not edit another member's owned directory without lead approval

No developer should hand off a module without tests.

## What To Produce During Project Work

Depending on task scope, create or update:

- requirement docs
- planning docs
- task split docs
- development logs
- code
- tests
- verification notes

## Default Behavior

When asked to help with YouSells work:

- first inspect the relevant docs
- then clarify scope
- then update planning/logging artifacts if needed
- then implement
- then test
- then report verification and联调 needs

Do not skip the engineering workflow just because the code change looks small.
