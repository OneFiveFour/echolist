# Screen Brief — Task List

> Attach: `dark-tasklist-edit-autodelete-on.png`,
> `dark-tasklist-edit-autodelete-off.png`,
> `light-tasklist-edit-autodelete-on.png`,
> `light-tasklist-edit-autodelete-off.png`.
> Screenshots are scope reference only, not a design target. Paste the block
> below after the master context.

---

## PROMPT TO PASTE

Use the approved color palette and type scale from the design-system foundation
step, follow the unified layout grid and placement rules established on the
Folder Browser, and keep to the global rules from the master context.

Design the **Task List** screen. It shows and edits a single task list — the
tasks it contains and the list's own options — and is reached by opening a task
list from the browser.

Required functionality:

- Show the task list's **title** and the **tasks** it contains.
- For each **main task**: let the user mark it complete, see and edit its title,
  see whether it has a **due date** and open its scheduling/settings, see
  whether it is **recurring**, and convey **due-date urgency** (not urgent,
  due soon, overdue) when a due date exists.
- Support **sub-tasks** under a main task, each independently completable.
- Let the user **add a new main task** to the list.
- Let the user **delete** individual tasks and **delete the whole task list**.
- Provide an **"auto-delete" option** for the list: when enabled, completed
  tasks are removed automatically. The presence of this mode changes what
  per-task management the user needs — account for both the auto-delete-on and
  auto-delete-off states (see the two screenshot variants).
- Provide the **way back** to the browser, per the global no-native-back rule.

Conditions and context:

- This screen carries the **task-list signature color** as its identity, and
  due-date urgency uses the shared warning (due soon) and error (overdue)
  states.
- Completing tasks and adding tasks are the frequent actions — prioritize them
  for one-handed reach and few taps.
- Deep per-task scheduling (choosing the date, recurrence rules, reminders)
  happens on the Main Task Settings screen; from here the user opens into it.
- Delete and back are app-wide concepts; use the consistent pattern and avoid
  pure confirmation dialogs.
