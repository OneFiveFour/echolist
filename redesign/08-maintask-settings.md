# Screen Brief — Main Task Settings (due date, recurrence, reminder)

> Attach: `dark-maintask-settings-off.png`, `dark-maintask-settings-daily.png`,
> `dark-maintask-settings-weekly.png`, `dark-maintask-settings-monthly.png`,
> `dark-maintask-settings-yearly.png`, and the matching `light-*` variants.
> Screenshots are scope reference only, not a design target. Paste the block
> below after the master context.

---

## PROMPT TO PASTE

Use the approved color palette and type scale from the design-system foundation
step, follow the unified layout grid and placement rules established on the
Folder Browser, and keep to the global rules from the master context.

Design the **Main Task Settings** screen. It configures the scheduling of a
single main task — its due date, how it repeats, and whether it reminds the
user. It is reached by opening a task's scheduling from the Task List screen,
and it returns its changes to that task.

Required functionality:

- Let the user pick a **due date** for the task from a date picker that can move
  across months.
- Let the user set a **recurrence mode**, choosing between: none, daily, weekly,
  monthly and yearly.
- When a repeating mode is chosen, let the user refine it with the fields that
  mode needs — for example an interval ("repeat every N weeks/months"), and for
  monthly, which day of the month. The available refinement fields depend on the
  chosen mode, so the screen adapts to the current selection (see the per-mode
  screenshot variants).
- Let the user toggle a **reminder** for the task on or off.
- Provide the **way back** that returns the chosen settings to the owning task,
  per the global no-native-back rule.

Conditions and context:

- This belongs to a task, so where its identity is relevant, use the task-list
  signature color.
- Recurrence does not require a due date in every case, but due date, recurrence
  and reminder are closely related here and should feel like one coherent
  scheduling surface.
- Keep mode-switching and the way back within one-handed reach; changing the
  recurrence mode should take as few taps as possible.
- Avoid confirmation dialogs; settings apply and flow back directly.
