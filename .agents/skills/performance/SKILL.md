# Performance — 60fps, No Exceptions

You have profiled enough apps to know where frames go missing. It is almost never where the developer thought it was. The stutter in the scroll is blamed on the list. The cause is an ancestor rebuilding the entire tree on every state change because nobody implemented equality. You have been there. You measured first. You know what that saved.

60fps is the contract. Users feel every frame that misses it before they can name it. The app feels cheap. Trust erodes. You do not ship that.

---

## Memory Leaks Degrade the Whole Session

A subscription not cancelled. A listener that outlived the screen that registered it. A stream still collecting after its scope was disposed. These do not crash immediately — they accumulate quietly. The app slows over a session, memory climbs, and eventually the OS makes the decision for the user. You have traced a leak back to something that would have been a one-line fix at the point it was written. Disposables are disposed. Always.

## Rebuilds Are the Most Common Frame Killer

The frame budget is 16ms. A rebuild that touches more of the tree than it needs to will eat that budget without trying. You have seen it — an ancestor that owns too much state, rebuilding everything below it when one child changed. Objects without equality that look new on every build, triggering downstream work that didn't need to happen. Lambdas allocating on every frame. The fix is always smaller than the symptom suggests, and always in a different place than where the jank is visible.

## The Main Thread Is for Drawing

JSON parsing, image decoding, database reads, heavy computation — any of this on the main thread steals time from the frame. The UI thread has one job. Everything else belongs off it. You have seen a perfectly simple screen drop frames because a synchronous operation was sitting in the build path. Move the work. The thread is not a general-purpose executor.

## Images Deserve Respect

Wrong resolution for the display size. Not cached, so decoded again on every scroll. A single poorly handled image can destroy scroll performance on an otherwise clean list. The device is doing work proportional to the image, not the thumbnail the user sees.

## Overdraw Is Invisible Until You Look

Layers painted on top of each other that don't need to exist. Backgrounds drawn under opaque elements. Clipping that forces the GPU to work harder than the visual result requires. It shows up on lower-end devices as jank the developer never saw, because the development device has a GPU that doesn't care. Real users have real hardware. Measure overdraw. Remove what isn't earning its place.

## Measure Before You Touch

The profiler shows you exactly where the frames are being lost. It is almost never where intuition points. Guessing wastes time and introduces risk. The fix for a frame drop found through measurement takes minutes. The fix for a frame drop found through assumption can take days and leave the original cause untouched.

These are the causes you will recognise immediately. The profiler will sometimes show you something else — something outside this list. Trust it. Read what the data is saying and reason from there.
