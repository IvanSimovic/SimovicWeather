# Error Handling — Reading What Broke

Something broke. The job is the same regardless of how you found out — read what the system is saying before touching anything. The mechanical fix is the last step, not the first.

---

## The Error Is Never the Problem

A crash report, a failing test, a build error — none of these are the problem. They are the problem making itself visible. You have fixed the symptom before and watched the real cause surface two weeks later as something harder. You know what that costs. Read the error as a question: what decision upstream produced this?

A build error is the compiler asking about structure. What contract is missing? What assumption broke? What change wasn't fully propagated? The mechanical fix that makes it compile is not the same as the fix that makes it correct.

A runtime crash points to where the app fell, not where the bug lives. The cause is usually somewhere above the stack trace — an assumption that turned out to be wrong under conditions that only exist in production. Read upward. Find where the bad state was created.

A failing test is the most honest signal you have. It was a guarantee. It is now telling you the guarantee no longer holds. Before changing the test, understand why. If the behaviour changed intentionally, the test updates. If it changed accidentally, the code fixes. A test changed to make it pass — without understanding the divergence — is a guarantee quietly removed.

---

## Errors That Disappear Are the Most Dangerous

A silent catch block is a future mystery. An error that reaches the user in silence — a frozen screen, an action that does nothing — is worse than a visible failure. The user doesn't know what happened. They retry. They lose trust. They leave.

Every caught error does one of three things: recovers to a known state, surfaces something useful to the user, or is explicitly acknowledged as safe to ignore with a reason in the code. Anything else is a problem that was found and hidden.

---

## Errors Belong to Their Layer

The data layer surfaces failures. The business logic layer decides what to do with them. The UI layer decides what to show. An error handled directly in the UI — skipping the layers below — couples concerns that belong apart and makes the next change to error handling a UI change. Keep the layers honest. Errors travel through them for a reason.
