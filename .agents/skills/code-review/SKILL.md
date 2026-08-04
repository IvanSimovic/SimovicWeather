# Code Review

Every problem you catch in review is a problem that doesn't reach production. Every problem you miss does. The review is the last moment where the cost of fixing something is low. Hold that weight.

---

## What You Are Looking For

**Architecture violations** — business logic in the wrong layer, data models leaking upward, UI making decisions that belong below it. These feel like working code. They are code that is correct now and expensive later — the coupling that becomes load-bearing, the regression in a screen you didn't expect to touch because the boundary that should have contained it didn't exist.

**Design system violations** — any hardcoded colour, spacing, or font. Any interactive element that bypassed the shared component. These feel like minor shortcuts. They are the beginning of a UI that no longer coheres, that breaks when the theme changes, that takes ten times longer to update because the values are scattered. Every one is a decision that has to be found and re-made every time the design evolves.

**Performance** — is anything receiving more than it needs? Is work being repeated that doesn't need to be? Is the symptom here, or is the cause three layers down? Know which before commenting.

**Error handling** — what happens when this fails? Does the failure propagate correctly through the layers? Does the user know something went wrong? A failure that disappears silently is not handled — it is hidden.

**Edge cases** — empty state, maximum data, back press mid-operation, navigation away during async work. The tester will find these. Find them first.

**The drift** — does this code follow the patterns the rest of the project established? A deviation that has a reason is a decision. A deviation without one is a shortcut that the next developer will either copy or spend time understanding before they can work confidently.

---

## How to Name a Problem

Precisely. In terms of consequences, not preferences. Not "this should use the repository pattern" — but "this couples the UI to the data source, so when the source changes, this screen changes with it for no reason." A developer who understands why something is wrong won't produce it again. A developer who received a style note will revert to their default in the next PR.
