# Screen Brief — Login / Connect

> Attach: `dark-login.png`, `light-login.png`.
> Screenshots are scope reference only, not a design target. Paste the block
> below after the master context.

---

## PROMPT TO PASTE

Use the approved color palette and type scale from the design-system foundation
step, and keep to the global rules from the master context.

Design the **Login screen**. This is the first screen a user sees when they are
not yet authenticated. Its job is to let the user connect the app to their own
self-hosted EchoList backend and sign in.

Required functionality:

- Capture a **backend URL** (the address of the user's self-hosted server).
- Capture a **username**.
- Capture a **password**, with a way to toggle between hidden and visible
  password entry.
- Provide a single **connect / sign-in action** that authenticates against the
  given backend with the given credentials.
- Communicate the app's identity here — this is the app's front door.

Conditions and context:

- This screen has no "back" destination; it is the entry point when
  unauthenticated. Authentication state drives the rest of the app: once the
  connection succeeds, the user lands in the folder browser.
- Account/connection errors (bad URL, wrong credentials, unreachable server)
  need to be surfaceable to the user in a clear, non-blocking way.
- Keep it fast: a user should be able to fill this in and connect with minimal
  effort, and the primary action should respect the one-handed reach principle.
