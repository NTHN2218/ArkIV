
- [ ] `v8`
	- `Registers`
		- [x] [[Data Storage]]
		- [x] [[Registers]]
		- [x] [[Unrecognized Registers]]
	-  `User Interface`
		- [x] Navigation Tree for Registers - [[Register Navigation]]
	- `User Experience`
		- [x] Improve Delete Confirmation Popup UI
		- [x] Scroll Bar moves to and aligns to same line when a new task-item gets created


- [ ] `v9`
	- `UI Features`
	- `Mark-Down`
	- `Precautions`

## UI Features

- [x] Change selection highlight color 
- [x] Selection collapse - to start, - to end
- [x] Keyboard focus navigation for pop-ups
- [x] [[Numbered Entries]] 
- [x] When moving Entry/sub-Entry up or down, scrollbar follows if it moves outside the window
- [x] Symbols for Registers, locked Registers, Unrecognized Registers and wherever possible using JetBrains Mono Nerd Font.
- [x] Ctrl + Tab to navigate between registers
## Precautions

- [x] Add fallback and handling for missing assets folder
## Markdown Syntax

- [x] Convert JTextArea to JTextPane for all Entries and Sub-Entries
- [x] [[Markdown Parsing and Rendering]]
	- [x] Markdown Syntax - parsing using commonMark
	- [x] Markdown Syntax - Rendering
		- [x] Bold
		- [x] Italic
		- [x] Heading (6 levels)
		- [x] Un-Ordered list
		- [x] Ordered list
		- [x] Task list



- [ ] `v10.0`
	- `Utilities`
		- [x] Selection Wrapper (bold, italic)
		- [x] Caret navigation - start/end of words or lines
- [ ] `v10.1`
	- `Mark-Down`
		- [x] Inline Code
		- [x] [[Right Align]] (custom)
		- [x] VIBGYOR color tags (custom)
	- `Register`
		- [x] App opens into the last visited Register
    - `Key-Binds`
	    - [x] Key-binds for expand all/collapse all 
	    - [x] Key-binds to apply **bold, italic, color tags, right align** while editing
	    - [x] Create main-Entry through dialog input-Area with *Ctrl+N*
	    - [x] Create sibling sub-Entry through a selected sub-Entry with *Ctrl+N*
	    - [x] Jump scrollbar to top/bottom of the current register through a key-bind
    - `App Appearance`
	    - [x] Add taskbar icon for ArkIV
	    - [x] Implement custom JFrame title bar 
	    - [x] Anchor dialog input-Areas to the main JFrame
	- `Behaviour`
		- [x] Hide caret when not in editing mode
		- [x] Double left-click anywhere on task-item body to select that task-item
		- [x] Right-click anywhere on Entry's body to collapse/expand Entry
		- [x] task-item's flicker starts immediately after getting selected
	- `Fixes`
		- [x] Return focus to selected task after closing an input-Area dialog
		- [x] Saving an Edit no longer deselects the selected task-item



- [ ] `v11.1`
	- [ ] `v10.1.1` [[Undo Action]]
	- [ ] `v10.1.2` - Undo Text
	- [ ] [[Copy and Paste]]
	- [ ] Read Only Mode
		- [ ] Finish Incomplete [[Unrecognized Registers]] Handling

- [ ] `v10.3` - GitHub integration  (very basic)

## Mark-Down
- [ ] Ordered/un-Ordered lists
	- [ ] Hanging indent 
    - [ ] nested lists
- [ ] Custom MD features
	


## Fixes
- [ ] Fix emoji rendering
- [ ] Create sub-Entry/Edit input box - Smart Increase height and width based on text size

- [ ] Auto close bracket 
- [ ] Help
    - [ ] Basic User Guide
	- [ ] Key-Binds
    - [ ] Hot Strings


