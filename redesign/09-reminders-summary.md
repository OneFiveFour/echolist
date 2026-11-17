# Screen Brief — Reminders Summary / Reminders

> Attach: `dark-notifications-settings.png`, `light-notification-settings.png`.
> Screenshots are scope reference only, not a design target. Paste the block
> below after the master context.

---

## PROMPT TO PASTE

Use the approved color palette and type scale from the design-system foundation
step, follow the unified layout grid and placement rules established on the
Folder Browser, and keep to the global rules from the master context.

Design the **Reminders Summary** screen. It is a cross-folder overview of all
tasks that have a reminder, letting the user see and toggle reminders in one
place. It is reached from the folder browser.

Relationship to the other surfaces: like the Due-Date Tasks Overview, this view
cuts across the whole folder tree rather than showing a single folder. Where the
due-date overview is about *completing* upcoming tasks, this screen is about
*managing which tasks remind the user*.

Required functionality:

- Gather tasks that can remind the user from across all folders and list them
  together.
- For each task, show its **title** and its **due date / time-until-due**, and
  indicate whether it is **recurring**.
- Let the user **toggle the reminder on or off per task** directly from this
  list.
- Convey **due-date context** consistently with the rest of the app (the same
  not-urgent / due-soon / overdue treatment used elsewhere).
- Keep the **task-list signature color** so it stays clear these are tasks.
- Provide the **way back**, per the global no-native-back rule.

Conditions and context:

- The list can be long; scanning and toggling quickly are the point.
- Toggling and the way back are the frequent actions — respect one-handed reach.
- This is a management surface; it does not need to open full task editing, but
  it must make a task's reminder state and its timing clear at a glance.
