package Menu;

import java.util.function.*;

public class EditMenu {

    private final Runnable collapseAll;
    private final Runnable expandAll;

    public EditMenu(Runnable collapseAll, Runnable expandAll) {
        this.collapseAll = collapseAll;
        this.expandAll = expandAll;
    }

    public void undo(){
        System.out.println("Undo Works");
    }

    public void redo(){
        System.out.println("Redo Works");
    }

    public void collapseAll() { collapseAll.run(); }

    public void expandAll()   { expandAll.run();   }

}
