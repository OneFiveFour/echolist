# EchoList Design Briefing

Briefings for redesigning EchoList's design system and UI with a prompt-driven
design tool (Figma Make / Banani). Each file contains a ready-to-paste English
prompt plus German usage notes about which screenshots to attach.

## How to run it

1. Start with **`00-initial-prompt.md`** — paste its prompt first. It sets up the
   app context, the color/typography system, the signature-color rule, the
   cross-platform navigation rules and the one-handed/few-taps/no-nag-dialog
   principles. Everything after it depends on this.
2. Then run **`00b-design-system.md`** — it locks the default light + dark
   palette and the type scale on their own, before any screen, so there is an
   agreed foundation to build on. (The layout grid is settled later, with the
   first screen.)
3. Then feed the **screen briefs one at a time**, attaching the dark + light
   screenshots named in each file. Screenshots are scope reference only, never a
   design target.
4. Finish with **`99-shared-components.md`** — run it last to extract the shared
   component library from the finished screens.

## Order

| # | File | Screen |
|---|------|--------|
| 00 | `00-initial-prompt.md` | Master context + design system (paste first) |
| 00b | `00b-design-system.md` | Colors & typography foundation (paste before screens) |
| 01 | `01-login.md` | Login / Connect |
| 02 | `02-browser.md` | Folder Browser (home + subfolders) |
| 03 | `03-create-folder.md` | Create Folder input |
| 04 | `04-tasks-overview.md` | Due-Date Tasks Overview (cross-folder) |
| 05 | `05-note-view.md` | Note View (read mode) |
| 06 | `06-note-edit.md` | Note Edit (edit mode) |
| 07 | `07-tasklist-edit.md` | Task List (incl. auto-delete states) |
| 08 | `08-maintask-settings.md` | Main Task Settings (due date/recurrence/reminder) |
| 09 | `09-task-notifications.md` | Task Notifications / Reminders (cross-folder) |
| 99 | `99-shared-components.md` | Shared component library (paste last) |

## Core principles baked into every brief

- **Signature colors** — folder / note / task list each keep their own identity
  color consistently across all screens (`folderColor`, `noteColor`,
  `taskColor`).
- **Function, not form** — briefs state *what* a screen must do; the tool owns
  all layout and visual decisions.
- **Cross-platform** — Android, Desktop, Web from one UI; never rely on a native
  back button; every screen owns its way back.
- **One-handed, few taps, no nag dialogs** — frequent actions within thumb
  reach (lower / lower-right); no "are you sure?"-style confirmations (input
  dialogs that collect a needed name are fine).
- **Consistency** — the same concept (back, save, delete, create) solved the
  same way everywhere.
- **Themes** — redefine the default light + dark palette (all mandatory tokens,
  new tokens allowed); drop the old background gradients; sans-serif only.
