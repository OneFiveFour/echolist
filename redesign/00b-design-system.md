# Design-System Brief — Colors & Typography (run before any screen)

> Paste this **after** the initial/master prompt and **before** the first screen
> brief. Its job is to lock the foundation — the default light + dark palette and
> the type scale — and get it approved on its own, so every screen afterwards
> builds on an agreed base. The layout grid is intentionally **not** finalized
> here; it is settled with the first real screen (the Folder Browser), where
> placement decisions actually have context.
>
> No screenshots to attach.

---

## PROMPT TO PASTE

Before we design any screen, establish and present the **foundation of the
design system**: the default color theme and the typography. Do not design any
app screen yet — produce only the foundation below, so we can review and agree
on it first.

### 1. Color — default light + dark theme

Define a cohesive, modern **default theme** with a full **light** variant and a
full **dark** variant. Give concrete values (hex) for every mandatory token in
both variants:

**Material color roles:** `background`, `surface`, `surfaceVariant`, `primary`,
`onPrimary`, `secondary`, `onSecondary`, `onBackground`, `onSurface`,
`onSurfaceVariant`, `error`, `onError`.

**EchoList-specific roles:** `background`, `taskColor` (task-list signature
color), `noteColor` (note signature color), `folderColor` (folder signature
color), `warning` (due-soon), `onWarning`.

Requirements for the palette:

- The three **signature colors** (`folderColor`, `noteColor`, `taskColor`) are
  the backbone of entity identity across the whole app. Make them clearly
  distinct from each other and legible in both light and dark. Show them
  prominently and explain the intended feel of each.
- Keep the three due-date states visually separable: normal, **due-soon**
  (`warning` / `onWarning`) and **overdue** (`error` / `onError`).
- Respect contrast/legibility for every `onX` pairing in both variants.
- You may introduce **additional tokens** if the system needs them — label any
  new ones clearly and say what they are for.
- Do not use gradient backgrounds.

### 2. Typography — default type scale

Define the default **type scale**. Sans-serif only — no serif faces, so it stays
crisp on screens. You may keep **Work Sans** as the starting point or propose
another clean sans-serif if it serves the system better; state your choice and
why. Provide a coherent set of levels (e.g. display / title / body / label) with
weights and sizes, and show how they read in both light and dark.

### 3. Extensibility note

Both color and typography must be structured as a **swappable theme**: the app
will add further light/dark color themes and possibly alternate type themes later
without reworking the screens. Define the default theme so that this is natural —
screens reference roles/levels, not hard-coded values.

### What to deliver now

- A presented **light palette** and **dark palette** with all mandatory tokens
  (and any new tokens), including the signature colors and the due-date states.
- A presented **type scale** with levels, weights and sizes.
- A short rationale for the key choices.

Produce **only** this foundation now. Do not lay out or mock any screen, and do
not define components yet — we will do screens one at a time afterwards, and the
layout grid will be settled with the first screen.
