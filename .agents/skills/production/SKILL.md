# Production — Shipping to Real People

You have shipped something that worked perfectly in every environment you had access to and broke silently in front of real users. No developer present. No way to reproduce it locally. A crash reporter showing obfuscated stack traces you couldn't read, or nothing at all because the reporter wasn't configured. You know what that feels like — the helplessness of a failure you can't see clearly enough to fix.

Production is a different environment from development in ways that are invisible until they aren't. The gap only reveals itself in front of real users. Check it before they get there.

---

## The Debug / Release Gap

Debug and release builds are not the same program. Logic inside `assert` blocks does not exist in release — if the app depends on it, that dependency silently disappears. A debug flag that was accidentally making something work disappears with it. Obfuscation renames everything accessed by name. A release build that is still marked debuggable exposes the app to runtime inspection.

You have been surprised by this gap. It is not a gap you get surprised by twice.

## Secrets

No credential lives in source code. Not temporarily. Not in a comment. A secret committed to version control exists forever in the history regardless of whether it is later removed. Secrets are injected at build time. They are never logged. A secret in a log file is a secret that is no longer secret — and log files go places you don't control.

## Crash Reporting

If users hit a crash with no reporter configured, it did not happen as far as the team is concerned. It accumulates invisibly. Users leave and nobody knows why. Crash reporting must be configured, must work in release builds, and must produce stack traces you can actually read. Symbolication is not optional. An unreadable crash report is the same as no crash report.

## The Platform-Specific Checks

These are the things that are only wrong once real users are running the app — permissions that don't match what's used, network config that inherited debug allowances, entitlements that differ between debug and release profiles. They don't fail in development. They surface in app store review, in user reports, or silently in behaviour that is wrong in ways that are hard to trace.

Check them. Every release. Not because the checklist says to — because you have paid the price of skipping them and you know exactly what it cost.
