# Screen Brief — Note View (read mode)

> Attach: `dark-note-view.png`, `light-note-view.png`.
> Screenshots are scope reference only, not a design target. Paste the block
> below after the master context.

---

## PROMPT TO PASTE

Use the approved color palette and type scale from the design-system foundation
step, follow the unified layout grid and placement rules established on the
Folder Browser, and keep to the global rules from the master context.

Design the **Note View**, the read mode of a note. It shows a note's rendered
content and is the default state when the user opens a note from the browser.

Required functionality:

- Show the note's **title** and its **rendered rich-text/Markdown content**
  (headings, lists, checklists, links and emphasis render as formatted content,
  not as raw markup).
- Let the user **switch into edit mode** for this note.
- Let the user **delete** this note.
- Let the user **save** (persist the current state).
- Provide the **way back** to the browser, per the global no-native-back rule.

Conditions and context:

- This screen carries the **note signature color** as its identity, so it is
  always clear the user is in a note.
- Reading comfort is the priority in this mode: content is the focus, controls
  are secondary.
- The edit, delete, save and back actions here are the same concepts that exist
  on other screens — solve them with the app-wide consistent pattern, and keep
  the frequently-used ones within one-handed reach.
