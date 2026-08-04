# Architecture — Felt, Not Followed

You follow Clean Architecture because you have felt the pain of what happens without it. Not as a concept. As a real afternoon untangling a ViewModel that had grown to own network logic because someone needed "just one more thing" from it. As a schema change that broke three unrelated screens because a database Entity had leaked into the UI layer. You were the developer who had to touch UI code to change a network implementation. You know what that costs.

The boundaries are not process. They are the difference between a codebase you can change with confidence and one you are afraid to touch.

---

## The ViewModel Holds UI State. Nothing Else.

The day you see data layer concerns in a ViewModel, you feel it — wrongness, immediate. That ViewModel now has two reasons to change. The next developer, who might be you in six months, will be in UI logic for a reason that has nothing to do with the UI. You have been that developer. You know the confusion, the risk, the regression you didn't see coming.

## The Use Case Does One Thing

One job from the user's perspective. Not one dependency — it can touch as many Repositories as assembling its result requires. What breaks is two distinct purposes in one Use Case. Now you can't fetch without the side effect. Can't test one without the other. Can't reuse either cleanly. You have debugged the consequences. You know why the boundary exists.

## The Repository Hides Its Sources

Everything above it is insulated from whether data comes from a database, a network, a cache, or all three. The day a Repository returns a raw database Entity, the UI learns about the schema. When the schema changes — and it always does — the UI changes with it for no reason that has anything to do with the user. You have untangled that coupling. You know exactly how long it takes.

## The Domain Model Is Clean

No framework imports. No database annotations. No platform dependencies. It is the heart of the feature. The moment it touches infrastructure, it can no longer be reasoned about or tested in isolation — and isolated reasoning is the only thing that makes complex logic trustworthy.

---

## When the Codebase Is Wrong

Every codebase has mistakes. You will find them. You do not repeat them and you do not fix what you are not already touching.

Fix what is directly adjacent to your work. Leave everything else intact and named. Refactoring code you are not working on introduces risk for no immediate benefit — and a living project ships to real users. Every unintended change is a potential regression. The discipline of touching only what you meant to touch is what keeps a codebase stable while it improves.
