
1. Side Bar Panel
     - All the registers both recognized and unrecognized will be placed on the [[Sidebar]] below the [[Search]].
     - The registers both recognized and unrecognized will be placed in a scroll pane inside the [[Sidebar]] that does not conflict with the other components, thus allowing the user to create as many registers as they wish.
2. Register Scroll Pane
     - There will be a '+' (add) button below the list of all recognized registers, the '+' button will be placed towards the left of the sidebar, however it will not have a fixed y-position as it will get pushed down as more registers get added.
     - If the program does not identify any unrecognized register there will be no separate category for them and will simply appear as empty space below the '+' add button. However, if there are registers that the header cannot recognize there will be a separation below the '+' button, it will have a 'Unrecognized Registers' title.
     - The recognized registers category will have a header title of 'Registers' and the unrecognized registers category will have a header title of 'Unrecognized Registers' below the separating line.
     - When new registers are added in the recognized registers category it will be placed below the last register in the list and will push the '+' button down and the whole of the 'Unrecognized Register' Category down to accommodate the space taken up by the button that would open up to the new register.
3. Register Creation and Appearance
      -  When users add a new register with the '+' , a popup asking for the name will appear where the user can enter the name, if unnamed it will have the default name as "Register" and a number indicating which register it is from all the total registers.
      - More creation related features will be implemented later
      - A selected register, i.e. the register in which the user is present will have the teal color, the unselected ones will be a gray slightly different from the sidebar bg.  
4. Recognized Registers 
      - All the registers will have a button towards the right extreme which will open up a picklist.
      - The picklist will provide options such as Rename, Move up/down, Delete, etc.
      - The header file must remember in which order the registers are placed from top to bottom.  
5. Unrecognized Registers
      -  The unrecognized Registers will be read only, and cannot be renamed, moved, only deletion is allowed.
      - The picklist will provide the option to recognize that specific register, once recognized it will behave like a normal Register.
6. Everything will strictly follow the [[hierarchy.canvas|hierarchy]] of ArkIV.