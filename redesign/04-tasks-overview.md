# Screen Brief — Due-Date Tasks Overview

> Attach: `dark-maintask-peek.png`.
> Screenshots are scope reference only, not a design target. Paste the block
> below after the master context.

---

## PROMPT TO PASTE

Use the approved color palette and type scale from the design-system foundation
step, follow the unified layout grid and placement rules established on the
Folder Browser, and keep to the global rules from the master context.

Design the **Due-Date Tasks Overview**. This is a cross-folder view that
collects tasks which have a due date from across the whole folder tree and
presents them together, so the user can see and act on what is coming up without
hunting through individual task lists.

Relationship to the folder browser: the browser shows one folder's contents at a
time; this overview is the complementary view that cuts *across* folders to
surface time-relevant tasks in one place. The user reaches it from the browser,
and it is a sibling navigation surface to it.

Required functionality:

- Gather tasks that have a **due date** from across all folders and present them
  grouped so the user understands which task list / folder each belongs to.
- For each task, show its **title**, its **due date / time-until-due**, and
  whether it is **recurring**.
- Convey **due-date urgency** clearly through three distinct states: not urgent,
  due soon (warning), and overdue (error). The time-until-due reads differently
  depending on urgency.
- Let the user **mark tasks complete** directly from this overview.
- Preserve the task-list **signature color** so it stays clear that these are
  tasks, consistent with the rest of the app.
- Provide the **way back** to where the user came from, per the global
  no-native-back rule.

Conditions and context:

- The list can be long and spans many source lists; grouping and scanning speed
  matter more than density.
- This is a read-and-act surface for due items; editing the task's schedule in
  depth happens on the task settings screen, not here.
- Completion and the way back are the frequent actions here, so respect the
  one-handed reach principle for them.
