Migrated taskitem from JTextArea --> JTextPane because:
    - JTextArea cannot render text in multiple fonts/font-sizes
    - JTextArea cannot individually apply bold, italic, or underline to specific words only, it must be constant for all the text.

Therefore, migration of taskitem from JTextArea --> to JTextPane was necessary to implement markdown features in ArkIV.

These are 2 major steps to display markdown features:
    - Parsing
    - Rendering

1. Parsing
    - In the context of markdown, parsing is defined as- the process of breaking down a raw string of markdown plaintext and converting it into a structured, tree like digital blueprint called an Abstract Syntax Tree (**AST**)
    - Parsing will be performed using an open source java library called - '**commonMark-java**'. version- '**org.commonmark:commonmark:0.29.0**'
    - Parsing must be performed whenever an entry/sub-entry is created or altered in any way.
    - Parsing must also be performed whenever the program starts, users switches between registers, etc. since currently the AST of a register is discarded when it exited, hence it must be re-generated on reopening that register.
    - The generated AST will be stored in memory, since the data is negligible the chance of lags and slow performance is highly unlikely.
    - To add custom markdown features, commonMark has to be trained/told to recognize the customs syntax and parse them accordingly by coding it in as an extension to commonMark.

2. Rendering
    - It is the process of displaying the strings after after applying the markdown features.
    - Interpreting(visiting) the AST and rendering are performed as the same pass, not two separate ones.
    - It traverses the tree and writes styled runs into the the StyleDocument.
    - In ArkIVv9, the markdown  source syntax characters(**, #, -, - [ ]) stay visible, but are de-emphasized(dimmed).
    - This version will not implement WYSIWYG(What You See Is What You Get) style like Typora, Obsidian, etc. because then the JTextpane's displayed text length =/= raw text length. This will require an offset mapping layer to properly render the strings, which is complex to code.
    - Rendering must be performed when programs starts, user switches between registers and entry/sub-entry is created or altered in any way.
    - tasklist items (- [ ]) render as styled text, not interactive embedded JCheckBox widgets, for v9. Real embedded checkboxes were explicitly deferred because they reintroduce a scoped version of the offset-mapping problem.




problems noticed
- Empty lines dont get rendered



upgrades

**This version will not implement WYSIWYG(What You See Is What You Get) style like**

89c2fd
b1bdfc
9ebffd
92b9f3
d3bafd
dbb9fc



