# Screen Brief — Create Folder (input dialog)

> Attach: `dark-create-folder.png`, `light-create-folder.png`.
> Screenshots are scope reference only, not a design target. Paste the block
> below after the master context.

---

## PROMPT TO PASTE

Use the approved color palette and type scale from the design-system foundation
step, follow the unified layout grid and placement rules established on the
Folder Browser, and keep to the global rules from the master context.

Design the **Create Folder** input. It appears over the folder browser when the
user chooses to create a new folder, and its only job is to collect the new
folder's name before the folder is created inside the current folder.

Required functionality:

- Collect a **folder name** via text entry.
- Provide a **create action** that creates the folder with the entered name,
  and a way to **dismiss without creating**.
- The create action should be unavailable or clearly inert until the input is
  valid (e.g. a non-empty name).

Conditions and context:

- This is a legitimate **input** step, not a confirmation step — it exists only
  because creating a folder needs a name. Keep it to that single purpose and as
  frictionless as possible.
- The new folder's identity is a folder, so wherever its signature color is
  relevant, use the folder signature color.
- Since it is text entry on mobile, account for the on-screen keyboard and keep
  the action reachable with it open.
- The same input pattern established here should be reused for any other
  "name a new item" case (e.g. naming a new note or task list), per the
  cross-screen consistency rule.
