# State — Owned, Scoped, Honest

Bad state design doesn't fail immediately. It accumulates. The screen that works fine with one item breaks with ten. The feature that works in isolation breaks when combined with another. The bug that only reproduces after a specific sequence of user actions, on a device you don't have, that three people have reported and no one can reliably recreate. You have been in that debugging session. You know how long it takes and how it ends — almost always with a state model that allowed something it shouldn't have.

---

## Illegal States Must Be Unrepresentable

You have seen the production crash that came from a screen that was simultaneously loading and showing an error. The type system allowed it. No individual developer intended it. A race condition produced it, in front of a real user, with a stack trace that pointed to the wrong place.

When the type system can prevent a class of bugs entirely, use it. A state model that allows impossible combinations will eventually produce them. A state the user can get stuck in because nothing moves them out is not an edge case — it is a frozen UI waiting to be discovered.

---

## State Lives at the Lowest Scope That Satisfies Its Consumers

You have seen local state promoted to global because it was easier at the time. Now it outlives the screen it belongs to, shows up in tests it has no business touching, and resets at the wrong moment for reasons that take an hour to trace.

Scope state to exactly the audience that needs it. No wider. State that persists longer than it needs to is stale state waiting to mislead the next thing that reads it. The decision to keep something alive beyond its natural scope is a conscious choice with a named reason — not a default.

---

## UI State and Domain State Are Different Things

Whether a button is loading is UI state. Whether a user is authenticated is domain state. You have seen what happens when they share a model — a UI bug that appears to corrupt domain truth, a loading flag that makes the app behave as if the user is logged out. Mixing them makes both harder to test and harder to reason about under failure. Keep them separated. When something goes wrong, you want to know immediately which world the bug lives in.

---

## State That Can't Be Observed Can't Be Debugged

State mutated in place, scattered across sources, hidden in local variables — this produces bugs that are reproducible but inexplicable. Someone can watch it happen and not understand why. State flows in one direction, from a single owner. When something is wrong, there is one place to look. That constraint is the thing that makes hard bugs findable.
