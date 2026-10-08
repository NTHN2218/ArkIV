
-  `v8`
	- `Registers`
		- [x] [[Data Storage]]
		- [x] [[Registers]]
		- [x] [[Unrecognized Registers]]
		- [x] [[Register Navigation]]
	- `Interacction`
		- [x] Scrollbar aligns with the newly created task-item.
	-  `Presention`
		- [x] Redesign Delete Confirmation pop-ups


-  `v9`
	- `Mark-Down`
		- [x] Convert Entries/sub-Entries from JTextArea -> JTextPane to enable MD rendering
		- [[Parsing and Rendering]]
			- [x] Headings
			- [x] Bold 
			- [x] Italic
			- [x] Ordered Lists
			- [x] Un-ordered Lists
			- [x] Task List
	- `Key-Binds`
		- [x] Navigate between registers with *Ctrl+Tab* / *Ctrl+Shift+Tab*
	- `Presentation` 
		- [x] [[Numbered Entries]] 
		- [x] Symbols for Registers/unrecognized Registers with **JetBrains Mono Nerd Fonts**
		- [x] Change selection highlight color 
	- `Interaction`
		- [x] Selection collapse
		- [x] Scrollbar follows Entries/sub-Entries when moved outside the visible window
		- [x] Keyboard focus navigation for pop-ups
	- `Precautions`
		- [x] Fallback for missing assets folder


-  `v10.0`
	- `Utilities`
		- [x] Selection Wrapper (bold, italic)
		- [x] Caret navigation - start/end of words or lines
- `v10.1`
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
	- `Interaction`
		- [x] Hide caret when not in editing mode
		- [x] Double left-click anywhere on task-item body to select that task-item
		- [x] Right-click anywhere on Entry's body to collapse/expand Entry
		- [x] task-item's flicker starts immediately after getting selected
		- [x] Implement Auto-scroll 
	- `Fixes`
		- [x] Return focus to selected task after closing an input-Area dialog
		- [x] Saving an Edit no longer deselects the selected task-item
		- [x] Redesign color tag syntax
        - [ ] Redesign right align syntax


- [ ] `v11.1`
	- [ ] `v10.1.1` [[Undo Action]]
	- [ ] `v10.1.2` - Undo Text
	- [ ] [[Copy-Paste-v1]]
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


