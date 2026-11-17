# EchoList — Initial Design Brief (Master Context)

> **How to use this file.** Paste the whole block below (everything inside
> "PROMPT TO PASTE") into the design tool (Figma Make / Banani) as the very
> first prompt, before any screen brief. It establishes the app, the design
> system and the global rules every screen must follow. After this, feed one
> screen brief at a time, attaching the matching dark-mode and light-mode
> screenshots for that screen.
>
> The screenshots show the *current* app only as a reference for scope and
> content. They are **not** a design target. You are explicitly asked to
> redesign the look and feel from scratch.

---

## PROMPT TO PASTE

You are designing a new design system and a unified UI for an existing,
feature-complete productivity app called **EchoList**. I will give you this
context first, then brief you on one screen at a time. Build a fresh visual
language — do not copy the current look.

### What EchoList is

EchoList is a personal knowledge and task manager built around a **hierarchical
folder structure**, like a file system for your own notes and tasks. A user
signs in to their own self-hosted backend, then browses into folders and
creates, reads, edits and deletes three kinds of items:

- **Folders** — containers that hold other folders, notes and task lists. They
  form an arbitrarily deep tree.
- **Notes** — rich text documents (Markdown-based), with a title and formatted
  body.
- **Task lists** — named lists of tasks. Each task can have sub-tasks, an
  optional due date, optional recurrence, and optional reminders.

Think of it as a cross between a file browser, a note app and a to-do app,
unified under one folder tree.

### The three entity types and their Signature Colors

Folders, notes and task lists are the three core entity types. Each one has a
dedicated **signature color** that identifies it everywhere in the app:

- **Folder** → folder signature color
- **Note** → note signature color
- **Task list** → task signature color

This is a hard rule for the whole design system: **wherever an entity of a given
type appears — in a list, as a card, as a header, as an accent, in a creation
control, anywhere — it is consistently tied to its signature color.** The user
should be able to tell at a glance whether they are looking at a folder, a note
or a task list, on every screen, without reading labels. Choose how strongly to
express this (border, accent, icon tint, background wash, etc.) — but keep the
mapping consistent and unambiguous across all screens.

### Target platforms (this shapes every interaction decision)

One single UI must work on **Android, Desktop and Web**. Design once; it runs on
all three. Two consequences are mandatory:

1. **Never rely on a platform-native back gesture or hardware back button.**
   Not every platform has one. Every screen that can be navigated away from must
   offer its own explicit way back, built into the UI.
2. **Design for one-handed mobile use as the primary case.** Assume a user
   holding a phone in one hand. Any control the user needs to reach frequently
   must sit within comfortable thumb reach — the lower portion of the screen,
   biased toward the lower-right. Do not place primary, frequently-used actions
   at the top of the screen where the user has to stretch or use a second hand.
   This takes priority over visual conventions.

### Global interaction principles (apply to every screen)

- **Absolute top priority: reach everything in as few taps as possible.** Favor
  direct actions over menus, nesting and multi-step flows.
- **No confirmation or nag dialogs.** Do not design "Are you sure you want to
  delete?" prompts, "press back again to exit" interruptions, or similar
  friction. Destructive and navigational actions happen directly. (Dialogs that
  *collect input the action genuinely needs* — e.g. typing a name when creating
  something — are fine and are not what this rule forbids.)
- **Consistency across screens.** The same concept must be solved the same way
  everywhere. If two screens both let the user save, delete, go back, or create
  something, those actions should feel and behave identically across the app.
  You decide the single pattern for each concept — then apply it everywhere.
- **Communicate with as little text as possible.** Do not fill the UI with
  titles, captions, hints, helper text and labels. Prefer communicating through
  meaningful **icons**, **spatial arrangement**, and **expressive shapes and
  colors** (including the signature colors). Use words only where they carry
  real meaning the user must read — primarily the user's own content (folder,
  note and task names/bodies) and genuinely ambiguous actions. When an icon,
  position or color can convey the meaning clearly, use that instead of a text
  label. Keep chrome text minimal.
- **You own all visual and layout decisions.** This brief and the per-screen
  briefs describe *what each screen must do*, never *how it should look or be
  laid out*. Do not treat any wording here as a layout instruction. Decide
  structure, placement, grid, components and styling yourself, guided only by
  the principles above.

### Themes and color system

The app supports multiple color themes, each shipping a **light** and a **dark**
variant. It must launch with one default dark theme and one default light theme,
and the architecture already allows adding further light/dark themes later
without code changes. Your first color job is to **redefine the default theme's
palette** from scratch — a cohesive, modern light scheme and dark scheme.

Define values for **all** of the following mandatory color tokens, for both the
light and the dark variant:

**Material color roles** (general surfaces, text, states):
`background`, `surface`, `surfaceVariant`, `primary`, `onPrimary`, `secondary`,
`onSecondary`, `onBackground`, `onSurface`, `onSurfaceVariant`, `error`,
`onError`.

**EchoList-specific roles** (app identity + entity signatures + due-date
urgency):
`background`, `taskColor` (task-list signature color), `noteColor` (note
signature color), `folderColor` (folder signature color), `warning` (due-soon
emphasis), `onWarning` (content on the warning color).

Notes on color:
- The three signature colors (`folderColor`, `noteColor`, `taskColor`) must be
  clearly distinguishable from each other and must read well in both light and
  dark, since they carry entity identity everywhere.
- `warning` / `onWarning` are used to flag tasks that are due soon; the standard
  `error` / `onError` roles flag tasks that are overdue. Keep these three states
  (normal, due-soon, overdue) visually distinct.
- You are **explicitly free to introduce additional color tokens** if your
  design system needs them. Just make clear which are new.
- Do **not** reintroduce the old background-gradient tokens; the previous design
  used a multi-stop gradient background that is being dropped.

### Typography

Typography follows the same philosophy as color: a default type scale now, with
room to swap or add type themes later. Keep it simple and screen-friendly:

- **Sans-serif only.** No serif fonts — everything must stay crisp on screens.
- You may keep the current font (**Work Sans**) as the starting point, or
  propose another clean sans-serif if it serves the new system better.
- Define a coherent type scale (display/title/body/label levels) that works
  across the three entity types and all screens.

### Layout grid

You have a free hand on the layout grid, with one goal: **one consistent spatial
system across all screens** so that structurally identical things (navigation,
primary actions, page titles, list items, the way back) sit in predictable,
consistent places app-wide. The current app is inconsistent here — that
inconsistency is exactly what the new unified grid should eliminate. Define the
grid, spacing scale and the consistent placement rules, then hold every screen
to them.

### What I will send next

I will brief each screen separately. For each, I will describe the screen's
**purpose and required functionality only**, and attach its current dark-mode
and light-mode screenshots for content/scope reference. Treat those screenshots
as "what the screen must be able to do," not as a design to match. For each
screen, propose a redesigned light and dark version consistent with this master
context.

So you know the whole picture up front, here is the full set of screens you can
expect, each with a one-line summary of what it does:

1. **Login / Connect** — connect the app to the user's self-hosted backend and
   sign in (backend URL, username, password with reveal toggle, connect action).
2. **Folder Browser** — the home and central navigation surface; lists the
   current folder's sub-folders, notes and task lists, lets the user open and
   create items, and move through the folder tree.
3. **Create Folder** — a small input step that collects a name when creating a
   new folder (an input dialog, not a confirmation).
4. **Due-Date Tasks Overview** — a cross-folder view collecting all tasks that
   have a due date, grouped by source, with urgency states and direct
   completion.
5. **Note View** — read mode of a note: its title and rendered rich-text/Markdown
   content, with entry into editing, plus delete/save/back.
6. **Note Edit** — edit mode of a note: editing the body with Markdown
   formatting actions (bold, lists, checklists, links, headings) and a preview
   toggle, plus save/delete/back.
7. **Task List** — view and edit a single task list: its tasks (with completion,
   sub-tasks, due date, recurrence, urgency), adding tasks, deleting tasks or the
   whole list, and an auto-delete option for completed tasks.
8. **Main Task Settings** — configure a single task's scheduling: due date via a
   calendar, recurrence mode (none/daily/weekly/monthly/yearly) with mode-specific
   refinement fields, and a reminder toggle.
9. **Reminders Summary** — a cross-folder list of all tasks that can remind the
   user, with a per-task reminder toggle and due-date context.

Items 4 and 9 are cross-folder overviews that sit alongside the browser rather
than inside a single folder.

**Do not design or produce any screen yet.** This message is context only. Its
list of screens exists purely to prime you on the overall shape of the app. Wait
for each screen's dedicated detail brief, and only design that one screen once
its brief arrives. Until then, do not generate layouts, mockups or components —
just acknowledge the context and wait for the first screen brief.

### Deliverables over the whole engagement

1. The default light + dark theme palettes (all mandatory tokens, plus any new
   tokens you introduce).
2. A typography scale.
3. A unified layout grid with consistent placement rules.
4. A redesign of every screen I brief you on, in both light and dark.
5. **At the very end**, after all screens exist, a shared-component library
   extracted from the recurring patterns across those screens. Do **not**
   predefine components up front — let them emerge from the screens, then
   consolidate.
