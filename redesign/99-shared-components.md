# Final Brief — Shared Component Library (run last)

> Use this **after** all screens have been redesigned. No screenshots to attach.
> Paste the block below once the full set of screens exists in the tool.

---

## PROMPT TO PASTE

Keep using the approved color palette and type scale from the design-system
foundation step and the unified layout grid established on the Folder Browser;
the library must build on those, not diverge from them.

All EchoList screens are now redesigned. As the final step, consolidate them
into a **shared component library** so the app is built from a consistent, reused
set of building blocks rather than one-off pieces.

What to do:

- Review every screen you have designed and **identify the patterns that recur
  across them** — the structures that appear on more than one screen or that
  solve the same concept in different places.
- Turn each recurring pattern into a **single reusable component** with defined
  variants/states, and retrofit every screen to use that component so identical
  concepts are identical everywhere.
- Pay particular attention to the app-wide concepts the briefs called out as
  needing one consistent solution — for example: the way back / navigation,
  the primary and destructive actions (save, delete, create), list items for
  the three entity types, the entity **signature-color** treatment, due-date
  urgency indicators, toggles, and text-input controls.
- Make the **signature-color system** a first-class part of the library: a clear
  rule and shared treatment for how folder / note / task-list identity is
  expressed, applied uniformly through the components.
- Ensure every component is defined with its **light and dark** appearance and
  draws from the theme tokens, so future themes work without redefining
  components.

Deliverables:

- A named component for each recurring pattern, with its variants and states.
- A short mapping of which screens use which components.
- Any adjustments to screens needed to make them consistent with the
  consolidated components.

Constraints (unchanged from the master context):

- Components describe reusable *function and behavior*; keep the one-handed
  reach, few-taps and no-nag-dialog principles intact.
- Do not break the cross-platform rule: nothing may depend on a native back
  gesture or hardware control.
