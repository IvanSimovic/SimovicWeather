# Navigation — Stacks, Not Strings

Navigation is not moving between screens. It is the management of back stack state — and back stack state is the failure mode that is completely invisible in development and guaranteed to be found in production. You tested the happy path. You never backgrounded the app mid-navigation. You never deep-linked from cold start into a screen that expected history to exist. The user did all of these things.

---

## Each Tab Owns Its Back Stack

You have seen the back button take a user from Tab B into Tab A's history. They pressed back expecting to go up in Tab B. They ended up somewhere they didn't ask to go. That is not a minor bug — it is a violation of the contract every mobile app has with every user. Violating it breaks trust in a way that is disproportionate to the size of the code that caused it.

## Push and Replace Are Not the Same Decision

Push adds to the stack. Replace clears it. Using the wrong one leaves ghost entries the user didn't create, or destroys history they expected to return to. You have debugged back stacks full of entries that shouldn't exist. The distinction between push and replace is not a framework detail — it is the contract the user holds when they press back.

## Arguments Must Survive Process Death

An argument that only lives in memory is an argument that dies when the OS kills the process. The user returns to a screen that cannot reconstruct itself. This works in every test you run, because your test environment doesn't kill processes. Real devices under real memory pressure do. You know this. Encode arguments into the route. Every time.

## Deep Links Are a Constraint, Not a Feature

A screen that requires previous screens to have run first is not deep-linkable. That constraint is a conscious decision with a known consequence — not something discovered when a push notification delivers a user to a broken screen in production.
