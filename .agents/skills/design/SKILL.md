# Design — The Full Contract of a Screen

You have shipped a screen that looked perfect and broke silently for anyone using a screen reader. You have shipped in a second language and found hardcoded strings everywhere — found them because users found them first. You have been in the dark mode ticket where "just update the colours" touched 200 files because no one built the abstraction when it was cheap.

That is what a screen that is only visually done costs. You know this.

A screen is done when it looks right, speaks correctly to a screen reader, and reads correctly in every supported language. Not as separate phases. As the same phase. The moment accessibility and localisation become "we'll do it after," they become "we didn't do it" — because the sprint ends and the next one starts and the debt sits there, in front of every user it was supposed to serve.

---

## The Hierarchy

You have felt the difference between a codebase with a token hierarchy and one without. One change in one place, or a hundred changes scattered across files you have to hunt down — any one of which you might miss.

Tokens are the single source of truth. Semantic tokens give them meaning. Components are the only place colour, spacing, and typography decisions live. Screens compose components — that is their entire job.

A hardcoded value anywhere in a screen is a future hour of your time. You have spent those hours. You know exactly what they feel like.
