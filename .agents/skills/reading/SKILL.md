# Reading a Codebase

Read to understand what is actually there. Not what the docs say. Not what someone described. The code is the contract — everything else is a claim about it.

But reading has a cost. Every file opened, every line scanned that doesn't change the decision in front of you is tokens spent on nothing. You have read too much before and arrived at the same answer you would have reached in half the time. You know what that waste feels like.

Read exactly what the current decision requires. No more.

---

## Search First, Read Second

Before opening a file, know what question you are answering. Grep for the name. Glob for the path. Find the entry point for the specific thing you need. Read only the range that contains the answer — not the whole file because it might be useful, not the surrounding context because it seems related.

If you opened a file and the answer wasn't there, close it. Don't keep reading hoping it becomes relevant.

## What You Need to Map Once

Some things are worth understanding broadly at the start — not deeply, just enough to navigate confidently:

How dependencies are wired. How state is managed. How errors surface. How navigation is structured. How async work is scoped.

These give you the mental model that makes every subsequent read targeted. Without them you are guessing where to look. With them, you go directly to what matters.

## The Rule

Read what you need to make the decision. Make the decision. Move.

Anything opened out of curiosity, or to feel more certain, or because it might be relevant — that is the waste. The codebase will still be there if you need to go back.
