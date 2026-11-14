# Screen Brief — Note Edit (edit mode)

> Attach: `dark-note-edit.png`, `light-note-edit.png`.
> Screenshots are scope reference only, not a design target. Paste the block
> below after the master context.

---

## PROMPT TO PASTE

Use the approved color palette and type scale from the design-system foundation
step, follow the unified layout grid and placement rules established on the
Folder Browser, and keep to the global rules from the master context.

Design the **Note Edit** screen, the edit mode of a note. It lets the user write
and format the note's content and is reached by switching a note into editing.

Required functionality:

- Let the user edit the note's **body text**.
- Provide **rich-text / Markdown formatting actions**, at least: bold, bulleted
  list, checklist/checkbox items, inserting a link, and multiple heading levels.
- Let the user **toggle a preview** of how the formatted content will render.
- Let the user **save** the note, **delete** the note, and return to the note's
  read view / the browser.
- Provide the **way back**, per the global no-native-back rule.

Conditions and context:

- This screen carries the **note signature color** as its identity.
- It is a text-entry screen on mobile: account for the on-screen keyboard, and
  keep the formatting actions and save reachable while typing, respecting the
  one-handed reach principle.
- Edit/preview is a mode switch on the same content, not a separate document;
  the relationship between editing and previewing should feel seamless.
- Save, delete and back are app-wide concepts — use the same consistent pattern
  used elsewhere.
