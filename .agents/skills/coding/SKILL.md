# Coding — The Language Working With You

You have read code that fought the language. You know what it costs — every developer who touches it spending mental energy on code that should have read like a sentence, tracing mechanisms instead of understanding intent. The bugs that live in that cognitive gap are real. The slowdown is real. It compounds with every new developer who has to learn the quirks instead of the domain.

Code reads like intent, not implementation. When it doesn't, the language is being worked against instead of with.

---

## The Type System Is On Your Side

You have seen the boolean soup — `isLoading`, `hasError`, `isEmpty`, `isSuccess` — and you have found the production bug that lived in the combination nobody thought to handle. The type system was offering to prevent that. The offer was declined.

When the compiler can enforce that every case is handled, let it. When an illegal state can be made unrepresentable, make it unrepresentable. The safety net is free. Refusing it means trusting every developer, under every deadline, forever, to never produce the impossible combination.

Structural equality belongs in the same category. A type used as UI state that doesn't implement equality correctly will either cause unnecessary work or miss necessary work — a UI that lies to the user about the state of the world. You have seen both. Neither is acceptable.

---

## Build Errors Are Structural Questions

The mechanical fix — the thing that makes it compile — is the last step, not the first. The error is a symptom. What decision upstream produced it? What contract is missing, what assumption broke, what change wasn't fully propagated?

The danger is the green build. A passing build feels like resolution. It can be the opposite — a structural problem made invisible, committed, pushed, and forgotten until it surfaces later as something much harder to untangle. The compiler stopped complaining. The problem didn't go away.

---

## Static Analysis Is the Codebase Talking

Every linter signal is a design problem making itself visible. A suppression annotation is a conversation that never happened — the problem noted, dismissed, and left for the next developer to find and wonder about. Fix the structure. The tooling exists to make quality the path of least resistance. Work with it.
