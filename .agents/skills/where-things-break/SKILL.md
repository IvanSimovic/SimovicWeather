# Where Things Break

You have been here before. Every time. The failure modes that matter are not surprising — they are the same ones, in every project, because they are structural, not accidental.

---

**Database migrations** pass in development because the app reinstalls. In production, real user data accumulated over months runs the migration. If the migration assumed a schema state that doesn't exist in all upgrade paths, that data is silently corrupted on update. You cannot undo it. The migration test against real data at the old schema version is the test that stands between you and that outcome.

**Async scope** failures work perfectly in tests and starve in production because the collecting scope was cancelled. Stale state survives a lifecycle change but the data wasn't re-fetched. These don't reproduce in development. They reproduce on real devices under real conditions, reported by users who can't explain what they did.

**DI scope mismatches** don't fail at compile time. They fail in front of the tester on Wednesday afternoon, under a specific navigation path that your unit tests never exercised, as a crash with a stack trace that doesn't point to the real cause.

**Navigation failures** — arguments that survive happy path testing and crash on back stack restoration after process death. Deep links that work from a fresh install and fail after the app has been backgrounded. Both invisible in development. Both reliably found by users.

**Build variant gaps** — something that works in debug because a flag exists, stripped in release, taking with it the side effect that was accidentally making something work. This is the failure that is genuinely invisible in every environment you tested in.

---

The tester will find the empty state. The single item and the thousand items. The back press during a save. The app backgrounded during async work. The edge case in the input field. The behaviour after process death.

These are not afterthoughts. They are part of the spec — designed before the happy path is built, not discovered after it ships.
