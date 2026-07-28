package Menu;



public class FileMenu {

    private final Runnable onSave;
    private final Runnable onDeselect;
    private final Runnable onFocusInput;

    public FileMenu(Runnable onSave, Runnable onDeselect, Runnable onFocusInput) {
        this.onSave = onSave;
        this.onDeselect = onDeselect;
        this.onFocusInput=onFocusInput;
    }

    public void newEntry(){
        onFocusInput.run();
    }

    public void importData(){
        System.out.println("Import Data works");
    }

    public void exportData(){
        System.out.println("Export Data works");
    }

    public void backupData(){
        System.out.println("Backup Data works");
    }

    public void restoreData(){
        System.out.println("Restore Data is working");
    }

    public void clearAll(){
        System.out.println("Clear All is working");
    }

    public void exit(){
        onDeselect.run();
        onSave.run();
        System.exit(0);
    }


}
