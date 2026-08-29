
1. Header file
    - The program will first read the header file before loading the data to understand the following :
    - How many registers are there
    - In what order they were created and saved
    - Which file was the default register and which ones were the user created register file
    - The program always opens in the default register, the user must navigate to the others to access it.
2. The .json files that actually store the user data
     - Only the default register is loaded on startup
     - The remaining registers are only loaded when when the register is opened by the user
     - At all times the user must be in any one register, in the program. The program currently allows the user to be only in one register at a time.
3. All the data files will be stored in ./assets/ 
4.  If there is any other .json file in the assets folder that closely or completely follows the pre-defined .json structure for user data but is not recognized by the header file then:
     - The program still accepts that register but places it separately from the recognized registers under a category called 'Unrecognized Registers'
     - The 'Unrecognized Registers' category will follow below the Recognized Registers in the UI , however the recognized registers will not have any header naming it so.
     - All the unrecognized Registers will be named sequentially as "Unrecognized Reg1", "Unrecognized Reg2", etc.
     - The user will be given the choice to flag an "unrecognized register" as "recognized" through a set of options next to the register which will move that register out of the unrecognized category and into the recognized category and will also get saved into the header file.
     - While unrecognized the user will be unable to edit the name of the register, its contents, meaning it will be read only. Only when the the register gets recognized will it become editable.
5. Everything will strictly follow the [[hierarchy.canvas|hierarchy]] of ArkIV. 
    