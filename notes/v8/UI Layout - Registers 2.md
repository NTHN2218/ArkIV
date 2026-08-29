This is to detail the changes for Register UI system. The logic for data storage along with creation, saving, loading, and deletion of registers has already been completed. This update will focus only on the UI, primarily look and feel of the UI. The core logic will remain untouched.

1. File Tree Navigation System for Registers
     - ArkIV will move towards using a file tree to navigate between all the Registers. As java's native file tree is extremely simplistic, the code will use flatlaf to improve the look of the the file tree.
     - The file tree will have 2 hierarchies , the highest level will be referred to as branches and the its sublevel as leaves.
     - The branches will be the categories into which all the registers will be sorted such as 'Registers', 'Unrecognized Registers', 'Imported', etc.
     - In this update only 'Registers' and 'Unrecognized Registers' branches will be implemented, but there will always be the freedom to add new categories (branches) to this tree.
     - Inside the branches will be the leaves, will be all the segregated registers.
2. File Tree Branches - Categories
     - These branches can be collapsed like a normal file tree.
     - There will no longer be any button to create new registers, instead similar to how 'Obsidian' handles new note creation, ArkIV will also implement a similar method to create new registers.
     - To create a new Register the user must right click on the branch a context menu will include a option called 'new register', currently this is the only option provided to the user, other features can be implemented later.
     - The user will only be permitted to create registers under the 'Registers' branch, i.e.  left clicking on other branches does nothing.
3. File Tree Leaves - Registers
     - The registers will be placed under their respective categories, the current logic already handles that.
     - Since each register is stored as a separate file, a file tree is the cleanest way to represent them.
     - There will be no button to edit them, but right clicking them will open a context menu from which the user can edit each register.
4. Code Implementation
     - The code will be very similar to [[createNavTree()]] from VPEFASv3, with minor tweaks to match my specifications.