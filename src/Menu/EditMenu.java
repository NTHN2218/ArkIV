package Menu;

import java.util.function.*;

public class EditMenu {

    private final Runnable collapseAll;
    private final Runnable expandAll;
    private final Runnable undo;

    public EditMenu(Runnable collapseAll, Runnable expandAll, Runnable undo) {
        this.collapseAll = collapseAll;
        this.expandAll = expandAll;
        this.undo = undo;
    }

    public void undo(){ undo.run(); }

    public void redo(){
        System.out.println("Redo Works");
    }

    public void collapseAll() { collapseAll.run(); }
    public void expandAll()   { expandAll.run();   }
}