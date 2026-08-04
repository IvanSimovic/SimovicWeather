# Planning — Confidence Before Code

You have built the wrong thing correctly. The code was clean, the tests passed, the feature shipped — and it solved a problem nobody had, or solved the right problem in the wrong order, or missed the edge case that was obvious in retrospect but invisible until the code already existed. The cost of being wrong grows dramatically once the code is written. A misaligned plan costs a conversation. Misaligned code costs a rewrite, and rewrites cost more than the code.

Every task gets a plan. No exceptions. Small task — a few sentences before the first file is touched. Medium or large — a `plan.md`, written and agreed on before implementation begins.

---

## A Plan Is Not a Spec

A spec defines every detail. A plan answers one question: are we solving the right problem in the right order? It names the decisions that aren't obvious and stops there. When it starts growing into implementation detail, it has become a spec in disguise. Cut it back. The obvious decisions will be made correctly during implementation. The non-obvious ones are the only thing worth writing down.

## Start With Why

The why changes every decision that follows. A plan that starts with what — "we will build X" — assumes the what is already correct. It usually isn't. The why surfaces whether X is the right thing to build at all.

## Edge Cases Belong in the Plan

What happens when the list is empty. What happens when the operation fails mid-execution. What happens when the user navigates away. Named before the code exists, these become designed behaviours. Discovered during implementation, they become tactical decisions made under pressure. The output of those two processes is not the same.

## When the Plan Exists, Execute From It

The plan was written to be executed, not reconsidered. If implementation surfaces something the plan didn't account for, surface it explicitly — "this changes the plan, here is how." Don't silently adapt. The plan was a shared alignment. Changing it without saying so means the alignment no longer exists and nobody knows.
