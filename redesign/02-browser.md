# Screen Brief — Folder Browser

> Attach: `dark-browser-home.png`, `dark-browser-subfolder.png`,
> `light-browser-home.png`.
> Screenshots are scope reference only, not a design target. Paste the block
> below after the master context.

---

## PROMPT TO PASTE

Use the approved color palette and type scale from the design-system foundation
step, and keep to the global rules from the master context. This is the first
real screen, so **establish the unified layout grid and the consistent
placement rules here** (navigation, the way back, page title, primary/create
actions, list items) — later screens will follow the same grid.

Design the **Folder Browser**. This is the home and the central navigation
surface of the app. It shows the contents of the current folder and lets the
user move through the folder tree and act on its items.

Required functionality:

- Show the **contents of the current folder**: a mixed listing of sub-folders,
  notes and task lists. Each item must make its **entity type instantly
  recognizable** through its signature color (folder / note / task list), per
  the global rule.
- For folders, convey that they are navigable and give a sense of how much they
  contain (a count of contained items).
- For notes and task lists, give a lightweight preview hint of their content
  where helpful, so the user can tell items apart without opening them.
- Let the user **open any item**: opening a folder navigates into it; opening a
  note opens the note; opening a task list opens the task list.
- Let the user understand and navigate their **current location in the folder
  tree**, including moving back up toward the root. Because there is no native
  back button, the way back and the sense of "where am I" must be part of this
  screen. At the root there is nowhere further up.
- Let the user **create new items** from here: a new folder, a new note and a
  new task list, each created inside the current folder.
- Provide access to two cross-folder task overviews (each is its own screen,
  briefed later; you only need enough here to decide where their entry points
  belong and which icon fits):
  - **Due-date tasks overview** — collects tasks that have a due date from
    across all folders into one time-focused list, so the user can see and
    complete what is coming up. Think "what's due".
  - **Task notifications / reminders overview** — collects tasks that can remind
    the user from across all folders, where the user toggles reminders per task.
    Think "which tasks notify me".

Conditions and context:

- This is the most-used screen, so creation and navigation actions are prime
  candidates for the one-handed, few-taps reach principle.
- The listing can be long; it must scroll comfortably while keeping navigation
  and primary actions reachable.
- An empty folder is a valid state and should still feel intentional and offer
  the create actions.
- Creating a folder (and likewise a note or task list) may collect the minimum
  input it needs — e.g. a name — which is acceptable; avoid any pure
  confirmation/nag step beyond that.
