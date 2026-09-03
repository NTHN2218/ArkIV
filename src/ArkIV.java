import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.*;

import java.nio.charset.StandardCharsets;
import java.util.function.*;

//Data Encryption
import java.security.spec.KeySpec;
import javax.crypto.*;
import javax.crypto.spec.*;

//Package - utilities
import Undo.UndoDebug;
import utilities.PathResolver;
import utilities.UniversalFactory;
import utilities.UniversalThemes;

//Mouse
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

//Package - Menu
import Menu.FileMenu;
import Menu.EditMenu;
import Menu.SettingsMenu;

//Data storage: json file handler
import com.google.gson.*;

//Package - Registers
import Registers.RegisterManager;
import Registers.RegisterContextMenu;

//Package Markdown
import Markdown.MarkdownRenderer;
import Markdown.MarkdownDebug;

// Package AutoHotkeys
import AutoHotkey.Hotstring;


//Register System: Navigation Tree
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.Icon;


public class ArkIV implements ActionListener{

    ///==============================================================================================================
    ///== Fields
    ///==============================================================================================================
    private JFrame frame;
    private JPanel taskPanel;
    private JTextField inputField;
    private int taskCounter = 1;
    private RegisterManager registerManager;
    private String FILE_NAME;

    private static final String SECRET_KEY = "dataEncryptKey15";
    private static final String SALT = "dataEncryptSalt7";
    private static final String IV = "dataEncryptIV328";
    private JTextArea inputArea;

    private JPanel sidebarPanel;

    // ── Text pane width budget (Phase 1) ────────────────────────────
    private static final int CHECKBOX_COLUMN_WIDTH = 30;       // checkBox.setPreferredSize width
    private static final int MAIN_ENTRY_BORDER_INSET = 4;      // outer matte(0,0) + inner line(2+2)
    private static final int SUB_ENTRY_BORDER_INSET = 64;      // outer matte(30+30) + inner line(2+2)
    private static final int MAIN_ENTRY_BUTTON_COLUMN_WIDTH = 50; // 40px button + 5+5 FlowLayout hgap
    private static final int SUB_ENTRY_BUTTON_COLUMN_WIDTH = 0;   // empty buttonPanel for subtasks

    private int mainEntryTextWidth = -1;
    private int subEntryTextWidth = -1;
    private Timer widthSettleTimer;

    private JMenuBar menuBar;
    private JMenu fileMenu, editMenu, settingsMenu, helpMenu;

    //Data as .json structure
    private Map<Integer, TaskItem> idToTaskMap = new HashMap<>();
    private List<TaskItem> allTasks = new ArrayList<>();


    private TaskItem selectedTask = null;

    private JDialog dialog;

    //to call private methods in this class from FileMenu.java
    FileMenu Menu_file = new FileMenu(
            this::saveTasks,
            this::deselectAll,
            () -> SwingUtilities.invokeLater(() -> {
                inputArea.requestFocusInWindow();
                UniversalThemes.flashBorder(inputArea, UniversalThemes.ACCENT_COLOR, UniversalThemes.BORDER_COLOR1, 2);
            })
    );

    EditMenu Menu_edit = new EditMenu(
            this::collapseAll,
            this::expandAll,
            this::performUndo
    );

    Undo.UndoCallbacks undoCallbacks = new Undo.UndoCallbacks(
            this::reinsertTask,
            this::deleteTaskById,
            this::editTaskText,
            this::swapTasksById,
            this::deleteRegisterById,
            this::restoreRegister,
            this::renameRegisterById,
            this::reorderRegistersById,
            this::setDefaultRegisterById,
            this::unrecognizeRegisterById,
            msg -> UniversalThemes.showToast(frame, msg),
            this::expandIfCollapsed,
            this::scrollToTaskById,
            this::refreshRegisterList
    );

    Undo.ActionUndoManager actionUndoManager = new Undo.ActionUndoManager(undoCallbacks);
    // ── TEMPORARY Phase 3 wiring test — delete once confirmed ──


    SettingsMenu Menu_settings = new SettingsMenu();

    //Search and find functionality
    private JScrollPane taskScrollPane;
    private JTextField searchBar;
    private JButton searchPrevButton, searchNextButton;
    private String lastSearchedQuery = "";
    private List<TaskItem> searchResults = new ArrayList<>();
    private int searchIndex = -1;
    private TaskItem highlightedSearchTask = null;

    //Registers
    private int currentRegisterId;
    private JTree registerTree;
    private DefaultMutableTreeNode registerTreeRoot;
    private DefaultMutableTreeNode registersBranchNode;
    private DefaultMutableTreeNode unrecognizedBranchNode;
    private int hoveredTreeRow = -1;
    private DefaultMutableTreeNode editingNode = null;
    private JTextField registerRenameField = null;

    ///==============================================================================================================
    ///== Constructor
    ///==============================================================================================================
    public ArkIV() {

        PathResolver.ensureAssetsStructure();

        registerManager = new RegisterManager();
        FILE_NAME = registerManager.getRegisterFilePath(registerManager.getDefaultRegister());

        frame = new JFrame("ArkIV");
        frame.getContentPane().setBackground(UniversalThemes.BG_MAIN);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);

        // ── Task panel + scroll ──────────────────────────────────────────
        taskPanel = new ScrollableTaskPanel();
        taskPanel.setBackground(UniversalThemes.BG_MAIN);
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS));

        taskScrollPane = new JScrollPane(taskPanel);
        taskScrollPane.setBorder(BorderFactory.createLineBorder(UniversalThemes.BORDER_COLOR1, 1));
        taskScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        taskScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        taskScrollPane.getVerticalScrollBar().setUnitIncrement(35);
        taskScrollPane.getViewport().setBackground(UniversalThemes.BG_MAIN);
        UniversalThemes.applyScrollbarTheme(taskScrollPane);

        // ── Input area + scroll ──────────────────────────────────────────
        inputArea = new JTextArea(3, 30);
        inputArea.setFont(UniversalThemes.getCompositeFont(17));
        inputArea.setBackground(UniversalThemes.BG_COMPONENT);
        inputArea.setForeground(UniversalThemes.TXT_PRIMARY);
        inputArea.setCaretColor(UniversalThemes.ACCENT_COLOR);
        inputArea.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, UniversalThemes.BORDER_COLOR1));
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setMargin(new Insets(8, 8, 8, 8));
        UniversalThemes.applySelectionTheme(inputArea);
        UniversalThemes.applyCollapseSelectionNavigation(inputArea);
        UniversalThemes.freeCtrlTabFromTraversal(inputArea);
        Hotstring.attach(inputArea);


        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputScroll.getViewport().setBackground(UniversalThemes.BG_PANEL);
        inputScroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UniversalThemes.BORDER_COLOR1));
        UniversalThemes.applyScrollbarTheme(inputScroll);
        inputScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        inputScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        inputScroll.setMinimumSize(new Dimension(0, 60));
        inputScroll.setPreferredSize(new Dimension(0, 90));

        // Auto-grow input area
        inputArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { adjustHeight(); }
            public void removeUpdate(DocumentEvent e)  { adjustHeight(); }
            public void changedUpdate(DocumentEvent e) { adjustHeight(); }

            private void adjustHeight() {
                int rows = inputArea.getLineCount();
                if (rows > inputArea.getRows()) {
                    inputArea.setRows(Math.min(rows, 10));
                    inputScroll.revalidate();
                }
            }
        });

        // Key bindings: Shift+Enter = new line, Enter = submit
        InputMap im = inputArea.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = inputArea.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK), "insert-newline");
        am.put("insert-newline", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int pos = inputArea.getCaretPosition();
                String text = inputArea.getText();
                inputArea.setText(text.substring(0, pos) + "\n" + text.substring(pos));
                inputArea.setCaretPosition(pos + 1);
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "submit-note");
        am.put("submit-note", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createTask();
            }
        });

        // ── Sidebar ──────────────────────────────────────────────────────
        sidebarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(UniversalThemes.BORDER_COLOR2);
                g2.setStroke(new BasicStroke(1f));

                g2.dispose();
            }
        };
        sidebarPanel.setBackground(UniversalThemes.BG_SIDEBAR);
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UniversalThemes.BORDER_COLOR2));

        createMenuBar();
        createSearchBar();
        createRegisterPanel();




        // ── Inner split: tasks (top) + input (bottom) ────────────────────
        JSplitPane innerSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, taskScrollPane, inputScroll);
        innerSplitPane.setResizeWeight(0.96);   // tasks absorb all extra space
        innerSplitPane.setDividerSize(0);
        innerSplitPane.setBorder(null);
        innerSplitPane.setBackground(UniversalThemes.BG_MAIN);
        innerSplitPane.setEnabled(false);      // lock divider, input height is fixed

        // ── Outer split: sidebar (left) + inner split (right) ────────────
        JSplitPane outerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebarPanel, innerSplitPane);
        outerSplitPane.setResizeWeight(0.18);
        outerSplitPane.setDividerSize(0);
        outerSplitPane.setBorder(null);
        outerSplitPane.setBackground(UniversalThemes.BG_MAIN);
        outerSplitPane.setEnabled(false);

        frame.add(outerSplitPane, BorderLayout.CENTER);

// ── Debounced width capture: waits until layout events stop firing
        // before trusting the width, since startup maximize can fire multiple
        // resize events before the split panes fully settle. ──────────────
        taskPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int width = taskPanel.getWidth();
                //System.out.println("[WidthCalc] componentResized fired, taskPanel width=" + width);
                if (width <= 0) return;

                if (widthSettleTimer != null && widthSettleTimer.isRunning()) {
                    //System.out.println("[WidthCalc] resize still in progress, resetting settle timer");
                    widthSettleTimer.stop();
                }

                widthSettleTimer = new Timer(200, ev -> {
                    //System.out.println("[WidthCalc] layout settled, finalizing capture at width=" + taskPanel.getWidth());
                    computeTextPaneWidths();
                    taskPanel.removeComponentListener(this);
                });
                widthSettleTimer.setRepeats(false);
                widthSettleTimer.start();
            }
        });

        // ── Window close handler ─────────────────────────────────────────
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                deselectAll();
                saveTasks();
            }
        });

        JRootPane rootPane = frame.getRootPane();
        InputMap rootIm = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap rootAm = rootPane.getActionMap();

        rootIm.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.CTRL_DOWN_MASK), "nextRegister");
        rootAm.put("nextRegister", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { cycleRegister(1); }
        });

        rootIm.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "prevRegister");
        rootAm.put("prevRegister", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { cycleRegister(-1); }
        });

        rootIm.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "performUndo");
        rootAm.put("performUndo", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { performUndo(); }
        });

        loadTasks();
        currentRegisterId = registerManager.getDefaultRegisterId();
        refreshRegisterList();
        frame.setVisible(true);
    }

    ///==============================================================================================================
    ///== Menu Bar Construction
    ///==============================================================================================================

    private void computeTextPaneWidths() {
        int panelWidth = taskPanel.getWidth();
        if (panelWidth <= 0) return; // layout not settled yet, ignore

        mainEntryTextWidth = panelWidth - MAIN_ENTRY_BORDER_INSET - CHECKBOX_COLUMN_WIDTH - MAIN_ENTRY_BUTTON_COLUMN_WIDTH;
        subEntryTextWidth  = panelWidth - SUB_ENTRY_BORDER_INSET - CHECKBOX_COLUMN_WIDTH - SUB_ENTRY_BUTTON_COLUMN_WIDTH;

        int viewportWidth = taskScrollPane.getViewport().getWidth();
//        System.out.println("[WidthCalc] taskPanel width=" + panelWidth
//                + " | viewport width=" + viewportWidth
//                + (panelWidth == viewportWidth ? " (MATCH ✓)" : " (MISMATCH ✗ — Scrollable clamp not working)")
//                + " | mainEntryTextWidth=" + mainEntryTextWidth
//                + " | subEntryTextWidth=" + subEntryTextWidth);

        // Retroactively fix entries constructed before the cache was ready (initial loadTasks())
        int reflowedCount = 0;
        for (TaskItem task : allTasks) {
            int width = task.isSubtask() ? subEntryTextWidth : mainEntryTextWidth;
            task.applyFixedTextWidth(width);
            reflowedCount++;
        }
        //System.out.println("[WidthCalc] reflowed " + reflowedCount + " existing TaskItem(s)");
        taskPanel.revalidate();
        taskPanel.repaint();
    }

    private void createMenuBar() {
        menuBar = new JMenuBar();
        menuBar.setBackground(UniversalThemes.BG_SIDEBAR);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UniversalThemes.BORDER_COLOR2));

        //Menu
        fileMenu     = UniversalFactory.createMenuBar("File");
        editMenu     = UniversalFactory.createMenuBar("Edit");
        settingsMenu = UniversalFactory.createMenuBar("Settings");
        helpMenu     = UniversalFactory.createMenuBar("Help");

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(settingsMenu);
        menuBar.add(helpMenu);

        createFileMenu();
        createEditMenu();
        createSettingsMenu();

        int menuHeight = menuBar.getPreferredSize().height;
        menuBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, menuHeight));
        menuBar.setMinimumSize(new Dimension(0, menuHeight));
        menuBar.setPreferredSize(new Dimension(0, menuHeight));  // 0 width lets BoxLayout control it

        JPanel menuWrapper = new JPanel(new BorderLayout());
        menuWrapper.setBackground(UniversalThemes.BG_SIDEBAR);
        menuWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, menuHeight));
        menuWrapper.setMinimumSize(new Dimension(0, menuHeight));
        menuWrapper.setPreferredSize(new Dimension(0, menuHeight));
        menuWrapper.add(menuBar, BorderLayout.CENTER);

        sidebarPanel.add(menuWrapper);
    }

    private void createFileMenu(){

        JMenuItem newEntry    = UniversalFactory.createMenuBarItem("New Entry");
        newEntry.addActionListener(this);
        newEntry.setActionCommand("New Entry");

        JMenuItem importData  = UniversalFactory.createMenuBarItem("Import Data");
        importData.addActionListener(this);
        importData.setActionCommand("Import Data");

        JMenuItem exportData  = UniversalFactory.createMenuBarItem("Export Data");
        exportData.addActionListener(this);
        exportData.setActionCommand("Export Data");

        JMenuItem backupData  = UniversalFactory.createMenuBarItem("Backup Data");
        backupData.addActionListener(this);
        backupData.setActionCommand("Backup Data");

        JMenuItem restoreData = UniversalFactory.createMenuBarItem("Restore Data");
        restoreData.addActionListener(this);
        restoreData.setActionCommand("Restore Data");

        JMenuItem clearAll    = UniversalFactory.createMenuBarItem("Clear All");
        clearAll.addActionListener(this);
        clearAll.setActionCommand("Clear All");

        JMenuItem exit        = UniversalFactory.createMenuBarItem("Exit");
        exit.addActionListener(this);
        exit.setActionCommand("Exit");

        fileMenu.add(newEntry);
        fileMenu.add(importData);
        fileMenu.add(exportData);
        fileMenu.add(backupData);
        fileMenu.add(restoreData);
        fileMenu.add(clearAll);
        fileMenu.add(exit);

    }

    private void createEditMenu(){
        JMenuItem undo        = UniversalFactory.createMenuBarItem("Undo");
        undo.addActionListener(this);
        undo.setActionCommand("Undo");

        JMenuItem redo        = UniversalFactory.createMenuBarItem("Redo");
        redo.addActionListener(this);
        redo.setActionCommand("Redo");

        JMenuItem collapse    = UniversalFactory.createMenuBarItem("Collapse All");
        collapse.addActionListener(this);
        collapse.setActionCommand("Collapse All");

        JMenuItem expand      = UniversalFactory.createMenuBarItem("Expand All");
        expand.addActionListener(this);
        expand.setActionCommand("Expand All");

        editMenu.add(undo);
        editMenu.add(redo);
        editMenu.add(collapse);
        editMenu.add(expand);
    }

    private void createSettingsMenu(){
        JMenuItem preferences = UniversalFactory.createMenuBarItem("Preferences");
        preferences.addActionListener(this);
        preferences.setActionCommand("Preferences");

        settingsMenu.add(preferences);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        switch(command){
            //File Menu
            case "New Entry"  : Menu_file.newEntry();   break;
            case "Import Data": Menu_file.importData(); break;
            case "Export Data": Menu_file.exportData(); break;
            case "Backup Data": Menu_file.backupData(); break;
            case "Restore Data":Menu_file.restoreData();break;
            case "Clear All": Menu_file.clearAll();break;
            case "Exit": Menu_file.exit(); break;

            //Edit
            case "Undo": Menu_edit.undo(); break;
            case "Redo": Menu_edit.redo(); break;
            case "Collapse All": Menu_edit.collapseAll(); break;
            case "Expand All": Menu_edit.expandAll(); break;

            //Settings
            case "Preferences": Menu_settings.preferences();break;


        }

    }

    ///==============================================================================================================
    ///== Search
    ///==============================================================================================================
    private void createSearchBar(){
        searchBar = new JTextField();
        searchBar.setBackground(UniversalThemes.BG_SIDEBAR);
        searchBar.setForeground(UniversalThemes.TXT_PRIMARY);
        searchBar.setCaretColor(UniversalThemes.ACCENT_COLOR);
        searchBar.setSelectedTextColor(Color.GREEN);
        searchBar.setFont(UniversalThemes.FONT_R_16);
        searchBar.setBorder(BorderFactory.createMatteBorder(1,1,1,1, UniversalThemes.BORDER_COLOR2));

        UniversalThemes.applySelectionTheme(searchBar);
        UniversalThemes.applyCollapseSelectionNavigation(searchBar);
        UniversalThemes.freeCtrlTabFromTraversal(searchBar);
        searchBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30)); // cap searchBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30)); // cap height
        searchBar.setPreferredSize(new Dimension(0, 30)); // match button height; width controlled by BorderLayout.CENTER


        searchPrevButton = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int cx = getWidth() / 2, cy = getHeight() / 2;
                int arm = 5;

                g2.setColor(isEnabled() ? UniversalThemes.TXT_PRIMARY : UniversalThemes.DISABLED_TEXT);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx - arm, cy + 3, cx, cy - 3);
                g2.drawLine(cx, cy - 3, cx + arm, cy + 3);

                g2.dispose();
            }
        };
        searchPrevButton.setPreferredSize(new Dimension(26, 30));
        searchPrevButton.setBackground(UniversalThemes.BG_SIDEBAR);
        searchPrevButton.setBorder(new LineBorder(UniversalThemes.BORDER_COLOR2, 1));
        searchPrevButton.setFocusable(false);
        searchPrevButton.setUI(new UniversalThemes.NoPressedButtonUI());
        searchPrevButton.setEnabled(false);
        searchPrevButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (searchPrevButton.isEnabled()) searchPrevButton.setBackground(UniversalThemes.BORDER_COLOR1);
            }
            public void mouseReleased(MouseEvent e) {
                if (searchPrevButton.isEnabled()) searchPrevButton.setBackground(UniversalThemes.BG_SIDEBAR);
            }
        });

        searchNextButton = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int cx = getWidth() / 2, cy = getHeight() / 2;
                int arm = 5;

                g2.setColor(isEnabled() ? UniversalThemes.TXT_PRIMARY : UniversalThemes.DISABLED_TEXT);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx - arm, cy - 3, cx, cy + 3);
                g2.drawLine(cx, cy + 3, cx + arm, cy - 3);

                g2.dispose();
            }
        };
        searchNextButton.setPreferredSize(new Dimension(26, 30));
        searchNextButton.setBackground(UniversalThemes.BG_SIDEBAR);
        searchNextButton.setBorder(new LineBorder(UniversalThemes.BORDER_COLOR2, 1));
        searchNextButton.setFocusable(false);
        searchNextButton.setUI(new UniversalThemes.NoPressedButtonUI());
        searchNextButton.setEnabled(false);
        searchNextButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (searchNextButton.isEnabled()) searchNextButton.setBackground(UniversalThemes.BORDER_COLOR1);
            }
            public void mouseReleased(MouseEvent e) {
                if (searchNextButton.isEnabled()) searchNextButton.setBackground(UniversalThemes.BG_SIDEBAR);
            }
        });

        JPanel searchRow = new JPanel(new BorderLayout(5, 0)); // 4px gap
        searchRow.setBackground(UniversalThemes.BG_SIDEBAR);
        searchRow.add(searchBar, BorderLayout.CENTER);

        JPanel searchButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        searchButtonsPanel.setBackground(UniversalThemes.BG_SIDEBAR);
        searchButtonsPanel.add(searchPrevButton);
        searchButtonsPanel.add(searchNextButton);
        searchRow.add(searchButtonsPanel, BorderLayout.EAST);

        JPanel searchWrapper = new JPanel(new BorderLayout());
        searchWrapper.setBackground(UniversalThemes.BG_SIDEBAR);
        searchWrapper.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8)); // padding
        searchWrapper.add(searchRow, BorderLayout.CENTER);
        searchWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        searchBar.addActionListener(e -> {
            // If we already have results for this exact search, Enter advances to next match
            String query = searchBar.getText().trim();
            if (!searchResults.isEmpty() && searchIndex != -1) {
                jumpToResult(searchIndex + 1);
            } else {
                performSearch(query);
                if (!searchResults.isEmpty()) {
                    jumpToResult(0);
                }
            }
            updateSearchButtonStates();
        });

        searchBar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { checkForStaleSearch(); }
            public void removeUpdate(DocumentEvent e)  { checkForStaleSearch(); }
            public void changedUpdate(DocumentEvent e) { checkForStaleSearch(); }

            private void checkForStaleSearch() {
                String current = searchBar.getText().trim().toLowerCase();
                if (!current.equals(lastSearchedQuery)) {
                    if (highlightedSearchTask != null) {
                        highlightedSearchTask.clearSearchHighlight();
                        highlightedSearchTask = null;
                    }
                    searchResults.clear();
                    searchIndex = -1;
                    updateSearchButtonStates();
                }
            }
        });

        searchPrevButton.addActionListener(e -> {
            jumpToResult(searchIndex - 1);
            updateSearchButtonStates();
        });

        searchNextButton.addActionListener(e -> {
            jumpToResult(searchIndex + 1);
            updateSearchButtonStates();
        });

        sidebarPanel.add(searchWrapper);
    }

    private void performSearch(String query) {
        // Clear any highlight from a previous search before starting a new one
        if (highlightedSearchTask != null) {
            highlightedSearchTask.clearSearchHighlight();
            highlightedSearchTask = null;
        }

        searchResults.clear();
        searchIndex = -1;

        String q = query.trim().toLowerCase();
        lastSearchedQuery = q;
        if (q.isEmpty()) {
            return;
        }

        List<TaskItem> matched = new ArrayList<>();
        Map<TaskItem, Integer> matchPosition = new HashMap<>();

        for (TaskItem task : allTasks) {
            String text = task.getRawText().toLowerCase();
            int pos = text.indexOf(q);
            if (pos != -1) {
                matched.add(task);
                matchPosition.put(task, pos);
            }
        }

        // Earlier match position in the text = closer/more relevant
        matched.sort(Comparator.comparingInt(matchPosition::get));
        searchResults.addAll(matched);
    }

    private void jumpToResult(int index) {
        if (searchResults.isEmpty()) return;

        // Wrap around in both directions
        if (index < 0) index = searchResults.size() - 1;
        if (index >= searchResults.size()) index = 0;
        searchIndex = index;

        TaskItem target = searchResults.get(searchIndex);

        // Clear previous highlight if it's a different task
        if (highlightedSearchTask != null && highlightedSearchTask != target) {
            highlightedSearchTask.clearSearchHighlight();
        }

        // If the match is a subtask hidden under a collapsed parent, expand it
        if (target.isSubtask()) {
            TaskItem parent = idToTaskMap.get(target.getParentId());
            if (parent != null && parent.isCollapsed()) {
                expandTask(parent);
            }
        }

        // Avoid flicker-vs-highlight border conflict (see step 2 notes)
        if (selectedTask != null) {
            selectedTask.deselectThisTask();
        }

        target.applySearchHighlight();
        highlightedSearchTask = target;

        SwingUtilities.invokeLater(() -> {
            taskPanel.scrollRectToVisible(target.getBounds());
        });
    }

    private void updateSearchButtonStates() {
        boolean hasResults = !searchResults.isEmpty();
        searchPrevButton.setEnabled(hasResults);
        searchNextButton.setEnabled(hasResults);
        searchPrevButton.repaint();
        searchNextButton.repaint();
    }

    ///==============================================================================================================
    ///== Nerd Font Glyph Icon
    ///==============================================================================================================
    private static class NerdGlyphIcon implements Icon {
        private final char glyph;
        private final Font font;
        private final Color color;
        private final int size;

        NerdGlyphIcon(char glyph, Font font, Color color, int size) {
            this.glyph = glyph;
            this.font = font;
            this.color = color;
            this.size = size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(font);
            g2.setColor(color);

            FontMetrics fm = g2.getFontMetrics();
            int drawX = x;
            int drawY = y + (size + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(String.valueOf(glyph), drawX, drawY);

            g2.dispose();
        }

        @Override public int getIconWidth()  { return size; }
        @Override public int getIconHeight() { return size; }
    }

    // Register
    private static final char FOLDER_GLYPH          = '\uf07b'; // fa-folder (closed, solid) — current default
    private static final char FOLDER_OPEN_GLYPH      = '\uf07c'; // fa-folder-open
    private static final char FOLDER_OUTLINE_GLYPH    = '\ue5ff'; // md-folder-outline (thinner line style)
    private static final char FOLDER_MD_GLYPH         = '\uf413'; // md-folder (filled, rounder MDI look)
    private static final char FOLDER_MD_OPEN_GLYPH     = '\uf76f'; // md-folder-open (MDI variant)
    private static final char FOLDER_OCT_GLYPH         = '\uf413'; // (same codepoint slot varies by patch set — verify below)
    private static final char FOLDER_SETI_GLYPH        = '\ue5fe'; // seti-ui folder icon (flat, minimal)
    private static final char FOLDER_ZIP_GLYPH         = '\uf1c6'; // fa-file-archive-o (in case you want a distinct "closed/archived" look)

    //Unrecognised Register
    private static final char FOLDER_QUESTION_GLYPH = '\uf29c'; // fa-question-circle-ish folder-adjacent, or...
    private static final char FOLDER_ALERT_GLYPH     = '\uf071'; // fa-exclamation-triangle (pairs well with "unrecognized")
    private static final char FOLDER_LOCKED_GLYPH    = '\uf023'; // fa-lock (reserve this one for your future locked-register feature instead)
    private static final char FILE_QUESTION_GLYPH    = '\uf128'; // fa-question — plain question mark, minimal

    ///==============================================================================================================
    ///== Registers
    ///==============================================================================================================
    private void createRegisterPanel() {
        registerTreeRoot = new DefaultMutableTreeNode("root");
        registersBranchNode = new DefaultMutableTreeNode("Registers");
        unrecognizedBranchNode = new DefaultMutableTreeNode("Unrecognized Registers");
        registerTreeRoot.add(registersBranchNode);
        registerTreeRoot.add(unrecognizedBranchNode);

        registerTree = new JTree(registerTreeRoot);
        registerTree.setRootVisible(false);
        registerTree.setShowsRootHandles(true);
        registerTree.setRowHeight(28);
        registerTree.setBackground(UniversalThemes.BG_SIDEBAR);
        registerTree.setForeground(UniversalThemes.TXT_PRIMARY);
        registerTree.setFont(UniversalThemes.FONT_R_16);

        registerTree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree t, Object value, boolean selected,
                                                          boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(t, value, selected, expanded, leaf, row, hasFocus);

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Object userObj = node.getUserObject();
                boolean isBranch = !leaf;

                setFont(isBranch
                        ? UniversalThemes.FONT_R_16.deriveFont(Font.BOLD)
                        : UniversalThemes.FONT_R_16);

                // Transparent selection styling -- actual row painting handled in Phase 3
                setBackgroundSelectionColor(new Color(0, 0, 0, 0));
                setBackgroundNonSelectionColor(new Color(0, 0, 0, 0));
                setBorderSelectionColor(new Color(0, 0, 0, 0));
                setOpaque(false);


                if (isBranch) {
                    setForeground(UniversalThemes.TXT_SECONDARY);
                } else if (userObj instanceof RegisterManager.UnrecognizedEntry) {
                    setForeground(UniversalThemes.TXT_PRIMARY);
                    setIcon(new NerdGlyphIcon(FOLDER_ALERT_GLYPH, UniversalThemes.FONT_NERD_ICON_SMALL, UniversalThemes.BG_DELETE_BTN, 25));
                } else if (userObj instanceof RegisterManager.RegisterEntry entry) {
                    Color rowColor = entry.id == currentRegisterId
                            ? UniversalThemes.MD_COLOR_HEADING
                            : UniversalThemes.TXT_PRIMARY;
                    char FOLDER_GLYPH_CURRENT =  entry.id == currentRegisterId
                            ? FOLDER_GLYPH  // When register is selected
                            : FOLDER_MD_GLYPH; // When register is not selected

                    setForeground(rowColor);
                    setIcon(new NerdGlyphIcon(FOLDER_GLYPH_CURRENT, UniversalThemes.FONT_NERD_ICON_SMALL, rowColor, 25 ));
                } else {
                    setForeground(UniversalThemes.TXT_PRIMARY);
                }

                setLeafIcon(null);
                setOpenIcon(null);
                setClosedIcon(null);
                setBorder(BorderFactory.createEmptyBorder(0, isBranch ? 4 : 10, 0, 0));

                if (node == editingNode) {
                    setText(""); // hide label text while the overlay field is active on this row
                }

                return this;
            }
        });

        registerTree.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = registerTree.getRowForLocation(10, e.getY());
                if (row != hoveredTreeRow) {
                    hoveredTreeRow = row;
                    registerTree.repaint();
                }
            }
        });
        registerTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredTreeRow = -1;
                registerTree.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) { maybeShowBranchMenu(e); }

            @Override
            public void mouseReleased(MouseEvent e) { maybeShowBranchMenu(e); }

            private void maybeShowBranchMenu(MouseEvent e) {
                if (!e.isPopupTrigger()) return;

                int row = registerTree.getRowForLocation(e.getX(), e.getY());
                if (row == -1) return;

                TreePath path = registerTree.getPathForRow(row);
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

                if (node == registersBranchNode) {
                    showBranchContextMenu(registerTree, e.getX(), e.getY());
                }
                // right-click on unrecognizedBranchNode or leaves: no-op here
                // (leaf right-clicks are handled separately below)
            }
        });

        registerTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { maybeShowLeafMenu(e); }

            @Override
            public void mouseReleased(MouseEvent e) { maybeShowLeafMenu(e); }

            private void maybeShowLeafMenu(MouseEvent e) {
                if (!e.isPopupTrigger()) return;

                int row = registerTree.getRowForLocation(e.getX(), e.getY());
                if (row == -1) return;

                TreePath path = registerTree.getPathForRow(row);
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (!node.isLeaf()) return;

                Object userObj = node.getUserObject();
                if (userObj instanceof RegisterManager.RegisterEntry entry) {
                    showRegisterContextMenu(entry, registerTree, e);
                } else if (userObj instanceof RegisterManager.UnrecognizedEntry unrecognized) {
                    showUnrecognizedContextMenu(unrecognized, registerTree, e);
                }
            }
        });

        final Color ACCENT_OVERLAY = new Color(
                UniversalThemes.ACCENT_COLOR.getRed(),
                UniversalThemes.ACCENT_COLOR.getGreen(),
                UniversalThemes.ACCENT_COLOR.getBlue(),
                45
        );
        final Color HOVER_OVERLAY = new Color(
                UniversalThemes.ACCENT_COLOR.getRed(),
                UniversalThemes.ACCENT_COLOR.getGreen(),
                UniversalThemes.ACCENT_COLOR.getBlue(),
                22
        );
        final int ACCENT_BAR_WIDTH = 3;

        registerTree.setUI(new BasicTreeUI() {

            @Override
            protected void paintRow(Graphics g, Rectangle clipBounds, Insets insets,
                                    Rectangle bounds, TreePath path, int row,
                                    boolean isExpanded, boolean hasBeenExpanded, boolean isLeaf) {

                int treeWidth = registerTree.getWidth();
                int rowY = bounds.y;
                int rowH = bounds.height;
                Graphics2D g2 = (Graphics2D) g;

                g2.setColor(UniversalThemes.BG_SIDEBAR);
                g2.fillRect(0, rowY, treeWidth, rowH);

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                Object userObj = node.getUserObject();
                boolean isCurrentRegister = isLeaf && userObj instanceof RegisterManager.RegisterEntry entry
                        && entry.id == currentRegisterId;
                boolean isHovered = row == hoveredTreeRow && isLeaf && userObj instanceof RegisterManager.RegisterEntry;

                if (isCurrentRegister) {
                    g2.setColor(ACCENT_OVERLAY);
                    g2.fillRect(0, rowY, treeWidth, rowH);
                    g2.setColor(UniversalThemes.ACCENT_COLOR);
                    g2.fillRect(0, rowY, ACCENT_BAR_WIDTH, rowH);
                } else if (isHovered) {
                    g2.setColor(HOVER_OVERLAY);
                    g2.fillRect(0, rowY, treeWidth, rowH);
                }

                if (!isLeaf) {
                    g2.setColor(UniversalThemes.TXT_SECONDARY);
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int ax = 6;
                    int ay = rowY + rowH / 2;
                    if (isExpanded) {
                        int[] xs = {ax, ax + 8, ax + 4};
                        int[] ys = {ay - 3, ay - 3, ay + 3};
                        g2.fillPolygon(xs, ys, 3);
                    } else {
                        int[] xs = {ax, ax, ax + 6};
                        int[] ys = {ay - 4, ay + 4, ay};
                        g2.fillPolygon(xs, ys, 3);
                    }
                }

                super.paintRow(g, clipBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf);
            }

            @Override
            public void paint(Graphics g, JComponent c) {
                super.paint(g, c);

                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(
                        UniversalThemes.TXT_SECONDARY.getRed(),
                        UniversalThemes.TXT_SECONDARY.getGreen(),
                        UniversalThemes.TXT_SECONDARY.getBlue(),
                        50
                ));
                for (int i = 1; i < registerTree.getRowCount(); i++) {
                    TreePath p = registerTree.getPathForRow(i);
                    if (p == null) continue;
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) p.getLastPathComponent();
                    if (node.getLevel() == 1) {
                        Rectangle r = registerTree.getRowBounds(i);
                        if (r != null) g2.drawLine(0, r.y, c.getWidth(), r.y);
                    }
                }
            }
        });

        UIManager.put("Tree.paintLines", false);
        UIManager.put("Tree.repaintWholeRow", true);
        UIManager.put("Tree.selectionBackground", UniversalThemes.BG_SIDEBAR);
        UIManager.put("Tree.selectionInactiveBackground", UniversalThemes.BG_SIDEBAR);
        UIManager.put("Tree.selectionBorderColor", UniversalThemes.BG_SIDEBAR);
        UIManager.put("Tree.selectionForeground", UniversalThemes.TXT_PRIMARY);
        UIManager.put("Tree.icon.selectedExpandedColor", UniversalThemes.ACCENT_COLOR);
        UIManager.put("Tree.icon.selectedCollapsedColor", UniversalThemes.ACCENT_COLOR);
        UIManager.put("Tree.icon.expandedColor", UniversalThemes.TXT_SECONDARY);
        UIManager.put("Tree.icon.collapsedColor", UniversalThemes.TXT_SECONDARY);

        registerTree.setToggleClickCount(1);

        registerTree.addTreeSelectionListener(e -> {
            TreePath path = registerTree.getSelectionPath();
            if (path == null) return;
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

            if (!node.isLeaf()) {
                registerTree.clearSelection();
                return;
            }

            Object userObj = node.getUserObject();
            if (userObj instanceof RegisterManager.RegisterEntry entry) {
                if (entry.id != currentRegisterId) {
                    switchToRegister(entry);
                }
            } else if (userObj instanceof RegisterManager.UnrecognizedEntry) {
                UniversalThemes.showPopup(frame,
                        "This register is unrecognized and read-only.\nRight-click it to Recognize or Delete.",
                        "Unrecognized Register");
            }
            registerTree.clearSelection(); // avoid stock blue-box selection lingering under our custom paint
        });

        JScrollPane registerScrollPane = new JScrollPane(registerTree);
        registerScrollPane.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UniversalThemes.BORDER_COLOR2));
        registerScrollPane.getViewport().setBackground(UniversalThemes.BG_SIDEBAR);
        registerScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        registerScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        UniversalThemes.applyScrollbarTheme(registerScrollPane);

        registerScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        sidebarPanel.add(registerScrollPane);
    }

    private void refreshRegisterList() {
        registersBranchNode.removeAllChildren();
        unrecognizedBranchNode.removeAllChildren();

        for (RegisterManager.RegisterEntry entry : registerManager.getRegisters()) {
            registersBranchNode.add(new DefaultMutableTreeNode(entry) {
                @Override public String toString() { return entry.name; }
            });
        }

        List<RegisterManager.UnrecognizedEntry> unrecognized = registerManager.getUnrecognizedEntries();
        for (RegisterManager.UnrecognizedEntry entry : unrecognized) {
            unrecognizedBranchNode.add(new DefaultMutableTreeNode(entry) {
                @Override public String toString() { return entry.displayName; }
            });
        }

        registerTreeRoot.removeAllChildren();
        registerTreeRoot.add(registersBranchNode);
        if (!unrecognized.isEmpty()) {
            registerTreeRoot.add(unrecognizedBranchNode);
        }

        DefaultTreeModel model = (DefaultTreeModel) registerTree.getModel();
        model.reload();

        registerTree.expandPath(new TreePath(registersBranchNode.getPath()));
        if (!unrecognized.isEmpty()) {
            registerTree.expandPath(new TreePath(unrecognizedBranchNode.getPath()));
        }
    }

    private void showUnrecognizedContextMenu(RegisterManager.UnrecognizedEntry entry, Component invoker, MouseEvent e) {
        RegisterContextMenu.showForUnrecognized(invoker, e.getX(), e.getY(),
                new RegisterContextMenu.UnrecognizedHandler() {
                    @Override
                    public void onRecognize() {
                        SwingUtilities.invokeLater(() -> {
                            String name = promptForRegisterName();
                            if (name == null) return;
                            RegisterManager.RegisterEntry newEntry = registerManager.recognizeFile(entry.filename, name);
                            actionUndoManager.log(new Undo.RecognizeRegister(0, newEntry.id, newEntry.id));
                            refreshRegisterList();
                        });
                    }

                    @Override
                    public void onCopyPath() {
                        File f = new File(registerManager.getAssetsPathPublic(), entry.filename);
                        copyPathToClipboard(f.getAbsolutePath());
                    }

                    @Override
                    public void onOpenInExplorer() {
                        File f = new File(registerManager.getAssetsPathPublic(), entry.filename);
                        openInSystemExplorer(f.getAbsolutePath());
                    }

                    @Override
                    public void onDelete() {
                        SwingUtilities.invokeLater(() -> {
                            boolean confirmed = UniversalThemes.showDeleteConfirmPopup(
                                    frame,
                                    "Delete File",
                                    entry.filename,
                                    "It will be permanently removed. This cannot be undone."
                            );
                            if (confirmed) {
                                File f = new File(registerManager.getAssetsPathPublic(), entry.filename);
                                f.delete();
                                refreshRegisterList();
                            }
                        });
                    }
                });
    }

    private void handleCreateRegister() {
        String name = promptForRegisterName();
        if (name == null) return;

        RegisterManager.RegisterEntry entry = registerManager.createRegister(name);
        actionUndoManager.log(new Undo.CreateRegister(0, entry.id, entry.id)); //Logs it for Undo
        refreshRegisterList();
    }

    private DefaultMutableTreeNode findLeafNodeForEntry(Object entry) {
        for (int i = 0; i < registersBranchNode.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) registersBranchNode.getChildAt(i);
            if (child.getUserObject() == entry) return child;
        }
        return null;
    }

    private void startInlineRename(RegisterManager.RegisterEntry entry) {
        DefaultMutableTreeNode node = findLeafNodeForEntry(entry);
        if (node == null) return;

        TreePath path = new TreePath(node.getPath());
        int row = registerTree.getRowForPath(path);
        Rectangle bounds = registerTree.getRowBounds(row);
        if (bounds == null) return;

        editingNode = node;
        registerTree.repaint();

        registerRenameField = new JTextField(entry.name);
        registerRenameField.setFont(UniversalThemes.FONT_R_16);
        registerRenameField.setForeground(UniversalThemes.TXT_PRIMARY);
        registerRenameField.setBackground(UniversalThemes.BG_COMPONENT);
        registerRenameField.setCaretColor(UniversalThemes.ACCENT_COLOR);
        registerRenameField.setBorder(BorderFactory.createLineBorder(UniversalThemes.ACCENT_COLOR, 1));
        UniversalThemes.applySelectionTheme(registerRenameField);
        UniversalThemes.applyCollapseSelectionNavigation(registerRenameField);
        UniversalThemes.freeCtrlTabFromTraversal(registerRenameField);

        int textX = bounds.x + 10; // matches the leaf's left inset from the cell renderer
        int rightMargin = 12;
        int availableWidth = registerTree.getWidth() - textX - rightMargin;

        registerRenameField.setBounds(textX, bounds.y + 2, Math.max(availableWidth, 80), bounds.height - 4);

        registerTree.add(registerRenameField);
        registerTree.setComponentZOrder(registerRenameField, 0);
        registerTree.revalidate();
        registerTree.repaint();

        registerRenameField.requestFocusInWindow();
        registerRenameField.selectAll();

        boolean[] handled = {false};

        registerRenameField.addActionListener(e -> {
            if (handled[0]) return;
            handled[0] = true;
            commitInlineRename(entry, registerRenameField.getText());
        });

        registerRenameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (handled[0]) return;
                handled[0] = true;
                commitInlineRename(entry, registerRenameField.getText());
            }
        });

        InputMap im = registerRenameField.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = registerRenameField.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel-rename");
        am.put("cancel-rename", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (handled[0]) return;
                handled[0] = true;
                cancelInlineRename();
            }
        });
    }

    private void commitInlineRename(RegisterManager.RegisterEntry entry, String newNameRaw) {
        cleanupInlineRenameField();
        String newName = newNameRaw.trim();
        if (!newName.isEmpty() && !newName.equals(entry.name)) {
            String oldName = entry.name;
            registerManager.renameRegister(entry.id, newName);
            actionUndoManager.log(new Undo.RenameRegister(0, entry.id, oldName));// Logs it for Undo
        }
        refreshRegisterList();
    }

    private void cancelInlineRename() {
        cleanupInlineRenameField();
        registerTree.repaint();
    }

    private void cleanupInlineRenameField() {
        if (registerRenameField != null) {
            registerTree.remove(registerRenameField);
            registerRenameField = null;
        }
        editingNode = null;
        registerTree.revalidate();
        registerTree.repaint();
    }

    private void showBranchContextMenu(Component invoker, int x, int y) {
        RegisterContextMenu.showForBranch(invoker, x, y, this::handleCreateRegister);
    }

    private String promptForRegisterName() {
        UniversalThemes.RoundedDialog rd = UniversalThemes.createRoundedDialogShell(frame, "New Register");

        JLabel label = new JLabel("Register name:");
        label.setForeground(UniversalThemes.TXT_SECONDARY);
        label.setFont(UniversalThemes.FONT_R_16);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField nameField = new JTextField(18);
        nameField.setBackground(UniversalThemes.BG_COMPONENT);
        nameField.setForeground(UniversalThemes.TXT_PRIMARY);
        nameField.setCaretColor(UniversalThemes.ACCENT_COLOR);
        nameField.setFont(UniversalThemes.FONT_R_18);
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UniversalThemes.BORDER_COLOR1, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        UniversalThemes.applySelectionTheme(nameField);
        UniversalThemes.applyCollapseSelectionNavigation(nameField);
        UniversalThemes.freeCtrlTabFromTraversal(nameField);
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, nameField.getPreferredSize().height));

        rd.body.add(label);
        rd.body.add(Box.createVerticalStrut(8));
        rd.body.add(nameField);
        rd.body.add(Box.createVerticalStrut(18));

        final String[] result = { null };

        Runnable submit = () -> {
            result[0] = nameField.getText().trim();
            rd.dialog.dispose();
        };

        InputMap im = nameField.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = nameField.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "confirm");
        am.put("confirm", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { submit.run(); }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        am.put("cancel", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                result[0] = null;
                rd.dialog.dispose();
            }
        });

        JButton createButton = UniversalThemes.createRoundedDialogButton("Create", UniversalThemes.ACCENT_COLOR,
                UniversalThemes.TXT_SELECTED, UniversalThemes.ACCENT_COLOR_DARK);
        JButton cancelButton = UniversalThemes.createRoundedDialogButton("Cancel", UniversalThemes.BG_COMPONENT,
                UniversalThemes.TXT_PRIMARY, UniversalThemes.BORDER_COLOR1);
        createButton.addActionListener(e -> submit.run());
        cancelButton.addActionListener(e -> {
            result[0] = null;
            rd.dialog.dispose();
        });

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonRow.setOpaque(false);
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonRow.add(createButton);
        UniversalThemes.wireDialogButtonNavigation(createButton, cancelButton);
        rd.body.add(buttonRow);

        rd.dialog.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { result[0] = null; }
        });

        UniversalThemes.finalizeRoundedDialog(rd.dialog, frame);
        SwingUtilities.invokeLater(nameField::requestFocusInWindow);
        rd.dialog.setVisible(true);

        return result[0];
    }

    private void switchToRegister(RegisterManager.RegisterEntry entry) {
        if (entry.id == currentRegisterId) return;

        // Autosave the register we're leaving
        deselectAll();
        saveTasks();

        // Clear in-memory state tied to the old register
        allTasks.clear();
        idToTaskMap.clear();
        taskPanel.removeAll();
        searchResults.clear();
        searchIndex = -1;
        highlightedSearchTask = null;
        lastSearchedQuery = "";
        searchBar.setText("");
        taskCounter = 1;

        // Point at the new register's file and load it
        currentRegisterId = entry.id;
        FILE_NAME = registerManager.getRegisterFilePath(entry);
        loadTasks();

        taskPanel.revalidate();
        taskPanel.repaint();

        refreshRegisterList();
    }

    private void cycleRegister(int direction) {
        List<RegisterManager.RegisterEntry> registers = registerManager.getRegisters(); // sorted by order, recognized only
        if (registers.isEmpty()) return;

        int index = -1;
        for (int i = 0; i < registers.size(); i++) {
            if (registers.get(i).id == currentRegisterId) {
                index = i;
                break;
            }
        }
        if (index == -1) return;

        int newIndex = (index + direction + registers.size()) % registers.size();
        switchToRegister(registers.get(newIndex));
    }

    private void showRegisterContextMenu(RegisterManager.RegisterEntry entry, Component invoker, MouseEvent e) {
        List<RegisterManager.RegisterEntry> sorted = registerManager.getRegisters();
        int index = sorted.indexOf(entry);
        boolean isFirst = index == 0;
        boolean isLast = index == sorted.size() - 1;
        boolean isDefault = entry.id == registerManager.getDefaultRegisterId();
        boolean canDelete = sorted.size() > 1;

        RegisterContextMenu.show(invoker, e.getX(), e.getY(), isFirst, isLast, isDefault, canDelete,
                new RegisterContextMenu.Handler() {
                    @Override
                    public void onRename() {
                        SwingUtilities.invokeLater(() -> startInlineRename(entry));
                    }

                    @Override
                    public void onMoveUp() {
                        List<RegisterManager.RegisterEntry> sorted = registerManager.getRegisters();
                        int idx = sorted.indexOf(entry);
                        int neighborId = (idx > 0) ? sorted.get(idx - 1).id : -1;
                        registerManager.reorder(entry.id, -1);
                        if (neighborId != -1) {
                            actionUndoManager.log(new Undo.ReorderRegister(0, entry.id, entry.id, neighborId));
                        }
                        refreshRegisterList();
                    }

                    @Override
                    public void onMoveDown() {
                        List<RegisterManager.RegisterEntry> sorted = registerManager.getRegisters();
                        int idx = sorted.indexOf(entry);
                        int neighborId = (idx < sorted.size() - 1) ? sorted.get(idx + 1).id : -1;
                        registerManager.reorder(entry.id, 1);
                        if (neighborId != -1) {
                            actionUndoManager.log(new Undo.ReorderRegister(0, entry.id, entry.id, neighborId));
                        }
                        refreshRegisterList();
                    }

                    @Override
                    public void onSetDefault() {
                        int previousDefault = registerManager.getDefaultRegisterId();
                        registerManager.setDefault(entry.id);
                        actionUndoManager.log(new Undo.SetDefaultRegister(0, entry.id, previousDefault));
                        refreshRegisterList();
                    }

                    @Override
                    public void onCopyPath() {
                        copyPathToClipboard(registerManager.getRegisterFilePath(entry));
                    }

                    @Override
                    public void onOpenInExplorer() {
                        openInSystemExplorer(registerManager.getRegisterFilePath(entry));
                    }

                    @Override
                    public void onDelete() {
                        SwingUtilities.invokeLater(() -> handleDeleteRegister(entry));
                    }
                });
    }

    private void handleDeleteRegister(RegisterManager.RegisterEntry entry) {
        if (registerManager.getRegisters().size() <= 1) {
            UniversalThemes.showPopup(frame, "You can't remove the only remaining register.", "Cannot Remove");
            return;
        }

        boolean confirmed = UniversalThemes.showDeleteConfirmPopup(
                frame, "Remove Register", entry.name,
                "The register file will be permanently removed. This cannot be undone.", "Remove");
        if (!confirmed) return;

        boolean wasCurrent = (entry.id == currentRegisterId);
        boolean wasDefault = (entry.id == registerManager.getDefaultRegisterId());
        String fileContent = registerManager.getRegisterFileContent(entry);

        actionUndoManager.log(new Undo.DeleteRegister(0, entry.id, entry.id, entry.name, entry.file, entry.order, wasDefault, fileContent));

        registerManager.deleteRegister(entry.id, false);

        if (wasCurrent) {
            RegisterManager.RegisterEntry fallback = registerManager.getDefaultRegister();
            if (fallback == null) fallback = registerManager.getRegisters().get(0);
            switchToRegister(fallback);
        } else {
            refreshRegisterList();
        }
    }

    private void copyPathToClipboard(String absolutePath) {
        java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(absolutePath);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        System.out.println("Calling showToast now");
        UniversalThemes.showToast(frame, "Path copied to clipboard");
    }

    private void openInSystemExplorer(String absolutePath) {
        try {
            File file = new File(absolutePath);
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                Desktop.getDesktop().open(file.getParentFile());
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", "-R", file.getAbsolutePath()).start();
            } else {
                Desktop.getDesktop().open(file.getParentFile());
            }
        } catch (Exception e) {
            UniversalThemes.showPopup(frame, "Could not open system explorer.\n" + e.getMessage(), "Error");
        }
    }

    ///==============================================================================================================
    ///== Undo Primitives (called only by ActionUndoManager via UndoCallbacks) — Phase 4 fills these in
    ///==============================================================================================================

    private void reinsertTask(Undo.TaskSnapshot task, List<Undo.TaskSnapshot> children, int anchorAfterTaskId) {
        TaskItem restored = new TaskItem(task.id(), task.text(), task.isDone(), task.isSub(), task.isCollapsed(), task.parentId());
        idToTaskMap.put(restored.getId(), restored);

        int allTasksIndex = (anchorAfterTaskId == -1) ? 0 : allTasks.indexOf(idToTaskMap.get(anchorAfterTaskId)) + 1;
        allTasks.add(allTasksIndex, restored);

        int panelIndex = (anchorAfterTaskId == -1) ? 0 : indexOfInPanel(idToTaskMap.get(anchorAfterTaskId)) + 1;
        taskPanel.add(restored, panelIndex);

        TaskItem lastInserted = restored;
        for (Undo.TaskSnapshot child : children) {
            TaskItem childItem = new TaskItem(child.id(), child.text(), child.isDone(), child.isSub(), child.isCollapsed(), child.parentId());
            idToTaskMap.put(childItem.getId(), childItem);
            allTasks.add(allTasks.indexOf(lastInserted) + 1, childItem);
            if (!restored.isCollapsed()) {
                taskPanel.add(childItem, indexOfInPanel(lastInserted) + 1);
            }
            lastInserted = childItem;
        }

        taskPanel.revalidate();
        taskPanel.repaint();
        renumberAllTasks();
        saveTasks();
    }

    private void deleteTaskById(int taskId) {
        TaskItem task = idToTaskMap.get(taskId);
        if (task == null) return;
        taskPanel.remove(task);
        allTasks.remove(task);
        idToTaskMap.remove(taskId);
        taskPanel.revalidate();
        taskPanel.repaint();
        renumberAllTasks();
        saveTasks();
    }

    private void editTaskText(int taskId, String oldText) {
        TaskItem task = idToTaskMap.get(taskId);
        if (task == null) return;
        task.rawText = oldText;
        Markdown.MarkdownRenderer.render(task.textArea.getStyledDocument(), oldText);
        saveTasks();
    }

    private void swapTasksById(int idA, int idB) {
        TaskItem a = idToTaskMap.get(idA);
        TaskItem b = idToTaskMap.get(idB);
        if (a == null || b == null) return;

        int indexA = indexOfInPanel(a);
        int indexB = indexOfInPanel(b);
        taskPanel.remove(a);
        taskPanel.remove(b);
        taskPanel.add(b, Math.min(indexA, indexB));
        taskPanel.add(a, Math.max(indexA, indexB));

        int allA = allTasks.indexOf(a);
        int allB = allTasks.indexOf(b);
        allTasks.set(allA, b);
        allTasks.set(allB, a);

        taskPanel.revalidate();
        taskPanel.repaint();
        renumberAllTasks();
        saveTasks();
    }

    /**
     * Register Level
     */
    private void deleteRegisterById(int registerId) {
        registerManager.deleteRegister(registerId, true);
        refreshRegisterList();
    }

    private void restoreRegister(int id, String name, String filename, int order, boolean wasDefault, String fileContentJson) {
        registerManager.restoreRegisterEntry(id, name, filename, order, wasDefault, fileContentJson);
        refreshRegisterList();
    }

    private void renameRegisterById(int registerId, String name) {
        registerManager.renameRegister(registerId, name);
        refreshRegisterList();
    }

    private void reorderRegistersById(int idA, int idB) {
        registerManager.swapOrder(idA, idB);
        refreshRegisterList();
    }

    private void setDefaultRegisterById(int registerId) {
        registerManager.setDefault(registerId);
        refreshRegisterList();
    }

    private void unrecognizeRegisterById(int registerId) {
        registerManager.unrecognize(registerId);
        refreshRegisterList();
    }

    private void expandIfCollapsed(int taskId) {
        Undo.UndoDebug.summary("[Stub] expandIfCollapsed called, id=" + taskId);
    }

    private void scrollToTaskById(int taskId) {
        Undo.UndoDebug.summary("[Stub] scrollToTaskById called, id=" + taskId);
    }


    ///==============================================================================================================
    ///== Task Lifecycle
    ///==============================================================================================================
    private void createTask() {
        String text = inputArea.getText().trim();
        if (!text.isEmpty()) {
            boolean added = addTaskFromInput(text);
            if (added) {
                inputArea.setText("");
            }
        }
    }

    private void deselectAll() {
        // Reset global selected task
        selectedTask = null;


        // Loop through all tasks and deselect each (stops flickers, resets borders, clears isSelected)
        for (TaskItem task : allTasks) {
            task.deselectThisTask();  // Calls stopFlicker(), sets isSelected=false, resets border
            // Note: We DON'T uncheck checkboxes here -- preserves "done" state if intended
            // If a checkbox was checked only for selection (not true "done"), it will stay checked on reload,
            // but since isSelected=false, moves won't work until re-interaction.
        }

        // Optional: Repaint panel to ensure visuals update immediately
        taskPanel.revalidate();
        taskPanel.repaint();
    }

    private void performUndo() {
        boolean didSomething = actionUndoManager.undo(currentRegisterId);
        if (!didSomething) {
            UndoDebug.summary("Nothing to undo.");
        }
    }

    private int countMainEntries() {
        int count = 0;
        for (TaskItem t : allTasks) {
            if (!t.isSubtask()) count++;
        }
        return count;
    }

    private int countSubEntries(int parentId) {
        int count = 0;
        for (TaskItem t : allTasks) {
            if (t.isSubtask() && t.getParentId() == parentId) count++;
        }
        return count;
    }

    private boolean addTaskFromInput(String text) {
        text = text.trim();
        if (!text.isEmpty()) {
            TaskItem task;
            if (selectedTask != null && !selectedTask.isSubtask()) {
                if (countSubEntries(selectedTask.getId()) >= 999) {
                    UniversalThemes.showPopup(frame,
                            "This Entry already has 999 sub-Entries, which is the maximum allowed.",
                            "Sub-Entry Limit Reached");
                    return false;
                }
                task = new TaskItem(taskCounter++, text, false, true, false, selectedTask.getId());
            }
            else {
                if (countMainEntries() >= 999) {
                    UniversalThemes.showPopup(frame,
                            "This register already has 999 Entries, which is the maximum allowed.",
                            "Entry Limit Reached");
                    return false;
                }
                task = new TaskItem(taskCounter++, text, false, false, false, -1);
            }
            idToTaskMap.put(task.getId(), task);
            allTasks.add(task);
            actionUndoManager.log(new Undo.CreateEntry(0, currentRegisterId, task.getId()));
            taskPanel.add(task);
            taskPanel.revalidate();
            renumberAllTasks();
            saveTasks();
            SwingUtilities.invokeLater(() -> scrollToTaskWithPadding(task));
            return true;
        }
        return false;
    }

    private void loadTasks() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }

                JsonArray array = JsonParser.parseString(sb.toString()).getAsJsonArray();
                allTasks.clear();
                idToTaskMap.clear();

                for (JsonElement el : array) {
                    JsonObject obj = el.getAsJsonObject();
                    int id            = obj.get("ID").getAsInt();                //ID - id
                    int parentId      = obj.get("parentID").getAsInt();             //parentID - parent id
                    String text       = obj.get("TXT").getAsString();             //TXT - text
                    boolean done      = obj.get("isDone").getAsBoolean();            //isDone - done
                    boolean isSubtask = obj.get("isSub").getAsBoolean();            //isSub - is Subtask
                    boolean isCollapsed = obj.get("isCollapsed").getAsBoolean();          //isCollapsed - is Collapsed

                    taskCounter = Math.max(taskCounter, id + 1);
                    TaskItem task = new TaskItem(id, text, done, isSubtask, isCollapsed, parentId);
                    allTasks.add(task);
                    idToTaskMap.put(id, task);
                }

                List<TaskItem> mainTasks = new ArrayList<>();
                for (TaskItem task : allTasks) {
                    if (!task.isSubtask()) {
                        mainTasks.add(task);
                        taskPanel.add(task);
                    }
                }

                for (TaskItem task : mainTasks) {
                    if (!task.isCollapsed()) {
                        showSubEntries(task);
                    }
                }
                renumberAllTasks();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "Error loading tasks");
                e.printStackTrace();
            }
        }
    }

    private void saveTasks() {
        try {
            JsonArray array = new JsonArray();
            for (TaskItem t : allTasks) {
                JsonObject obj = new JsonObject();
                obj.addProperty("ID", t.getId());
                obj.addProperty("parentID", t.getParentId());
                obj.addProperty("TXT", t.getRawText());
                obj.addProperty("isDone", t.isDone());
                obj.addProperty("isSub", t.isSubtask());
                obj.addProperty("isCollapsed", t.isCollapsed());
                array.add(obj);
            }
            try (PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(new FileOutputStream(FILE_NAME), StandardCharsets.UTF_8))) {
                writer.print(new GsonBuilder().setPrettyPrinting().create().toJson(array));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error saving tasks");
        }
    }

    private void renumberAllTasks() {
        // Main Entries and sub-Entries are numbered in independent 1-999 scopes:
        // the main counter increments per top-level Entry and resets the sub
        // counter, which then increments per sub-Entry belonging to that Entry.
        // This relies on sub-Entries always being rendered as a contiguous block
        // directly after their parent (guaranteed by showSubEntries/move up/down).
        int mainNumber = 0;
        int subNumber = 0;
        for (Component c : taskPanel.getComponents()) {
            if (c instanceof TaskItem) {
                TaskItem task = (TaskItem) c;
                if (!task.isSubtask()) {
                    mainNumber++;
                    subNumber = 0;
                    task.setNumberLabel(mainNumber);
                } else {
                    subNumber++;
                    task.setNumberLabel(subNumber);
                }
            }
        }
    }

    private int indexOfInPanel(TaskItem task) {
        for (int i = 0; i < taskPanel.getComponentCount(); i++) {
            if (taskPanel.getComponent(i) == task) return i;
        }
        return -1;
    }

    private void scrollToTaskWithPadding(TaskItem task) {
        Rectangle bounds = task.getBounds();
        int padding = 100; // extra space to reveal above and below the entry

        // Extend upward, clamped so we never request above the panel's top (y=0)
        int paddedY = Math.max(0, bounds.y - padding);
        int paddedHeight = (bounds.y - paddedY) + bounds.height + padding;

        Rectangle padded = new Rectangle(
                bounds.x,
                paddedY,
                bounds.width,
                paddedHeight
        );

        // Clamp so we never request beyond the panel's actual content height
        int maxY = taskPanel.getHeight();
        if (padded.y + padded.height > maxY) {
            padded.height = Math.max(bounds.height, maxY - padded.y);
        }

        taskPanel.scrollRectToVisible(padded);
    }

    ///==============================================================================================================
    ///== Sub-Entries
    ///==============================================================================================================
    private void hideSubEntries(TaskItem parent) {
        //System.out.println("[Move] hideSubEntries CALLED for entry " + parent.getId());
        //new Exception("trace").printStackTrace();
        for (TaskItem task : allTasks) {
            if (task.isSubtask() && task.getParentId() == parent.getId()) {
                taskPanel.remove(task);
            }
        }
        taskPanel.revalidate();
        taskPanel.repaint();
        renumberAllTasks();
    }

    private void showSubEntries(TaskItem parent) {
        int parentIndex = taskPanel.getComponentZOrder(parent);

        // Collect all subtasks of this parent IN THE ORDER THEY APPEAR IN allTasks (preserves moved/saved order)
        List<TaskItem> subtasks = new ArrayList<>();
        for (TaskItem task : allTasks) {  // Loop over allTasks to get saved order
            if (task.isSubtask() && task.getParentId() == parent.getId()) {
                subtasks.add(task);
                // NO SORTING BY ID HERE -- this was the bug causing revert!
            }
        }

        // Add them just after the parent (in the collected order)
        for (int i = 0; i < subtasks.size(); i++) {
            taskPanel.add(subtasks.get(i), parentIndex + i + 1);
        }

        taskPanel.revalidate();
        taskPanel.repaint();
        renumberAllTasks();
    }

    ///==============================================================================================================
    ///== Collapse / Expand
    ///==============================================================================================================
    private void collapseAll() {
        for (TaskItem task : allTasks) {
            if (!task.isSubtask() && !task.isCollapsed()) {
                task.isCollapsed = true;
                Border newOuter = BorderFactory.createMatteBorder(1, 0, 10, 0, UniversalThemes.BORDER_COLOR1);
                Border currentInner = (task.getBorder() instanceof CompoundBorder)
                        ? ((CompoundBorder) task.getBorder()).getInsideBorder()
                        : BorderFactory.createLineBorder(UniversalThemes.BG_PANEL, 2);
                task.setBorder(BorderFactory.createCompoundBorder(newOuter, currentInner));
                hideSubEntries(task);
            }
        }
        saveTasks();
    }

    private void expandAll() {
        for (TaskItem task : allTasks) {
            if (!task.isSubtask() && task.isCollapsed()) {
                task.isCollapsed = false;
                Border newOuter = BorderFactory.createMatteBorder(1, 0, 0, 0, UniversalThemes.BORDER_COLOR1);
                Border currentInner = (task.getBorder() instanceof CompoundBorder)
                        ? ((CompoundBorder) task.getBorder()).getInsideBorder()
                        : BorderFactory.createLineBorder(UniversalThemes.BG_PANEL, 2);
                task.setBorder(BorderFactory.createCompoundBorder(newOuter, currentInner));
                showSubEntries(task);
            }
        }
        saveTasks();
    }

    private void expandTask(TaskItem task) {
        if (task.isCollapsed()) {
            task.isCollapsed = false;
            Border newOuter = BorderFactory.createMatteBorder(1, 0, 0, 0, UniversalThemes.BORDER_COLOR1);
            Border currentInner = (task.getBorder() instanceof CompoundBorder)
                    ? ((CompoundBorder) task.getBorder()).getInsideBorder()
                    : BorderFactory.createLineBorder(UniversalThemes.BG_PANEL, 2);
            task.setBorder(BorderFactory.createCompoundBorder(newOuter, currentInner));
            showSubEntries(task);
            saveTasks();
        }
    }

    ///==============================================================================================================
    ///== Encryption
    ///==============================================================================================================
    private String encrypt(String plainText) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALT.getBytes(), 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivspec = new IvParameterSpec(IV.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
        byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    private String decrypt(String encryptedText) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALT.getBytes(), 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivspec = new IvParameterSpec(IV.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
        byte[] decodedEncrypted = Base64.getMimeDecoder().decode(encryptedText);
        byte[] decrypted = cipher.doFinal(decodedEncrypted);
        return new String(decrypted, "UTF-8");
    }

    private static class ScrollableTaskPanel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }
        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }
        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 100;
        }
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true; // ← the actual fix: never let taskPanel exceed the viewport's width
        }
        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false; // must stay false, or vertical scrolling breaks
        }
    }

    // ── Forces mid-word breaks for unbroken long tokens. LabelView's own
    // breakView() already knows how to break at a character boundary — the
    // only thing stopping it is getMinimumSpan() reporting "can't shrink,"
    // which tells FlowView's layout not to bother trying. ──────────────────
    private static class WrapLabelView extends LabelView {
        public WrapLabelView(Element elem) { super(elem); }

        @Override
        public float getMinimumSpan(int axis) {
            switch (axis) {
                case View.X_AXIS:
                    return 0;
                case View.Y_AXIS:
                    return super.getMinimumSpan(axis);
                default:
                    throw new IllegalStateException("Invalid axis: " + axis);
            }
        }
    }

    private static class WrapColumnFactory implements ViewFactory {
        @Override
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                switch (kind) {
                    case AbstractDocument.ContentElementName:
                        return new WrapLabelView(elem);
                    case AbstractDocument.ParagraphElementName:
                        return new ParagraphView(elem);
                    case AbstractDocument.SectionElementName:
                        return new BoxView(elem, View.Y_AXIS);
                    case StyleConstants.ComponentElementName:
                        return new ComponentView(elem);
                    case StyleConstants.IconElementName:
                        return new IconView(elem);
                }
            }
            return new LabelView(elem);
        }
    }

    private static class WrapEditorKit extends StyledEditorKit {
        private final ViewFactory factory = new WrapColumnFactory();
        @Override
        public ViewFactory getViewFactory() {
            return factory;
        }
    }

    ///==============================================================================================================
    ///== Entry Point
    ///==============================================================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ArkIV::new);
    }

    ///==============================================================================================================
    ///== TaskItem Inner Class
    ///==============================================================================================================
    public class TaskItem extends JPanel {
        private int id;
        private int parentId;
        private boolean isSubtask;
        private boolean isCollapsed;
        private JCheckBox checkBox;
        private JTextPane textArea;
        private int fixedTextWidth = -1;
        private Timer flickerTimer;
        private boolean isSelected = false;
        private boolean isSearchHighlighted = false;
        private JLabel numberLabel;
        private String rawText;

        public TaskItem(int id, String text, boolean done, boolean isSubtask, boolean isCollapsed, int parentId) {
            this.id = id;
            this.parentId = parentId;
            this.isSubtask = isSubtask;
            this.isCollapsed = isCollapsed;
            this.rawText = text;

            setLayout(new BorderLayout());
            Color cardBg = isSubtask
                    ? UniversalThemes.BG_PANEL
                    : UniversalThemes.BG_PANEL;

            setBackground(cardBg);

            // Outer border (same as original)
            int subtaskIndent = 30;
            Border outerBorder;
            if (isSubtask) {
                outerBorder = BorderFactory.createMatteBorder(5, subtaskIndent, 2, 30, UniversalThemes.BORDER_COLOR1);
            } else {
                outerBorder = BorderFactory.createMatteBorder(1, 0, isCollapsed ? 10 : 0, 0, UniversalThemes.BORDER_COLOR1);
            }

            // Inner border (2 pixels thick now)
            Border innerBorder;
            if (isSubtask) {
                innerBorder = BorderFactory.createLineBorder(cardBg, 2);
            } else {
                innerBorder = BorderFactory.createLineBorder(cardBg, 2);
            }

            // Combine borders
            setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));

            setOpaque(true);
            setAlignmentX(Component.LEFT_ALIGNMENT);

            checkBox = new JCheckBox();
            checkBox.setPreferredSize(new Dimension(30, 30));
            checkBox.setBackground(UniversalThemes.BG_MAIN);
            UniversalThemes.applyCheckBoxTheme(checkBox);
            checkBox.setSelected(done);

            // ActionListener for flicker effect and selection management
            checkBox.addActionListener(e -> {
                if (checkBox.isSelected()) {
                    selectThisTask();
                } else {
                    deselectThisTask();
                }
            });

            this.fixedTextWidth = isSubtask ? subEntryTextWidth : mainEntryTextWidth;

            textArea = new JTextPane() {
                @Override
                public Dimension getPreferredSize() {
                    Dimension d = super.getPreferredSize();
                    int minHeight = 68;
                    if (d.height < minHeight) d.height = minHeight;
                    if (fixedTextWidth > 0) d.width = fixedTextWidth;
                    return d;
                }
            };
            textArea.setEditorKit(new WrapEditorKit());
            textArea.setText(text);
            textArea.setFont(UniversalThemes.getCompositeFont(17));
            textArea.setForeground(UniversalThemes.TXT_PRIMARY);
            textArea.setCaretColor(UniversalThemes.ACCENT_COLOR);
            textArea.setOpaque(false);
            textArea.setEditable(false);
            textArea.setBorder(null);
            UniversalThemes.applySelectionTheme(textArea);
            UniversalThemes.applyCollapseSelectionNavigation(textArea);
            UniversalThemes.freeCtrlTabFromTraversal(textArea);

            MarkdownRenderer.render(textArea.getStyledDocument(), rawText);
            MarkdownDebug.summary("[TaskItem] Initial render complete for id=" + id);

            if (fixedTextWidth > 0) {
                textArea.setSize(fixedTextWidth, Short.MAX_VALUE);
            }

            if (done) {

            }



            numberLabel = new JLabel();
            numberLabel.setFont(UniversalThemes.FONT_R_10);
            numberLabel.setForeground(UniversalThemes.BG_LINENUM);
            numberLabel.setHorizontalAlignment(SwingConstants.CENTER);
            numberLabel.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 1));

            JPanel numberAndCheckPanel = new JPanel(new BorderLayout());
            numberAndCheckPanel.setOpaque(false);
            numberAndCheckPanel.add(numberLabel, BorderLayout.NORTH);
            numberAndCheckPanel.add(checkBox, BorderLayout.CENTER);

            JPanel leftPanel = new JPanel(new BorderLayout());
            leftPanel.setOpaque(false);
            leftPanel.add(numberAndCheckPanel, BorderLayout.WEST);
            leftPanel.add(textArea, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setOpaque(false);

            if (!isSubtask) {
                JButton createSubEntryButton = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        int cx = getWidth() / 2, cy = getHeight() / 2;
                        int arm = 7;

                        g2.setColor(UniversalThemes.TXT_SELECTED);
                        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.drawLine(cx - arm, cy, cx + arm, cy);
                        g2.drawLine(cx, cy - arm, cx, cy + arm);

                        g2.dispose();
                    }
                };
                createSubEntryButton.setBackground(UniversalThemes.ACCENT_COLOR);
                createSubEntryButton.setForeground(UniversalThemes.TXT_SELECTED);
                createSubEntryButton.setBorder(new LineBorder(UniversalThemes.ACCENT_COLOR_DARK, 2));
                createSubEntryButton.setPreferredSize(new Dimension(40, 29));
                createSubEntryButton.setUI(new UniversalThemes.NoPressedButtonUI());
                UniversalThemes.ClickEffect(createSubEntryButton);

                createSubEntryButton.addActionListener(e -> createSubEntry());
                buttonPanel.add(createSubEntryButton);

                addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            TaskItem.this.isCollapsed = !TaskItem.this.isCollapsed;
                            Border newOuter = BorderFactory.createMatteBorder(1, 0, TaskItem.this.isCollapsed ? 10 : 0, 0, UniversalThemes.BORDER_COLOR1);
                            Border currentBorder = getBorder();
                            Border currentInner;
                            if (currentBorder instanceof CompoundBorder) {
                                currentInner = ((CompoundBorder) currentBorder).getInsideBorder();
                            } else {
                                currentInner = innerBorder;
                            }
                            setBorder(BorderFactory.createCompoundBorder(newOuter, currentInner));
                            if (TaskItem.this.isCollapsed) hideSubEntries(TaskItem.this);
                            else showSubEntries(TaskItem.this);
                            saveTasks();
                        } else {
                            //Do nothing
                        }
                    }
                });
            } else {
                addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        //Do nothing
                    }
                });
            }

            add(leftPanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.EAST);


            //Shortcuts for convenience

            InputMap im = this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            ActionMap am = this.getActionMap();

            InputMap imWindow = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

            //Edit Task
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK), "Edit");
            am.put("Edit", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(isSelected){
                        editEntry();
                    }
                }
            });

            //Delete Task
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK), "Delete");
            am.put("Delete", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (isSelected) {
                        confirmDeleteTask();
                    }
                }
            });


            //add subEntry to Entry
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), "addSubEntry");
            am.put("addSubEntry", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (isSelected) {
                        createSubEntry();
                    }
                }
            });

            //Find
            imWindow.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "Find");
            am.put("Find", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    searchBar.requestFocus();
                    UniversalThemes.flashBorder(searchBar, UniversalThemes.ACCENT_COLOR, UniversalThemes.BORDER_COLOR2, 1);
                }
            });

            //Deselect selected tasks
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Deselect");
            am.put("Deselect", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (isSelected) {
                        deselectAll();
                    }
                }
            });

            //Move Up selected task
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK), "moveUp");
            am.put("moveUp", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (isSelected) {
                        moveTaskUp();
                    }
                }
            });

            //Move Down selected task
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK), "moveDown");
            am.put("moveDown", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (isSelected) {
                        moveTaskDown();
                    }
                }
            });

        }

        public void setNumberLabel(int n) { numberLabel.setText(String.valueOf(n)); }
        public int getId() { return id; }
        public int getParentId() { return parentId; }
        public boolean isSubtask() { return isSubtask; }
        public boolean isCollapsed() { return isCollapsed; }
        public boolean isDone() { return checkBox.isSelected(); }
        public String getRawText() { return rawText; }

        public void applyFixedTextWidth(int width) {
            if (width <= 0) return;
            this.fixedTextWidth = width;
            textArea.setSize(width, Short.MAX_VALUE);
        }

        private void selectThisTask() {
            if (selectedTask != null && selectedTask != this) {
                selectedTask.deselectThisTask();
            }
            selectedTask = this;
            isSelected = true;
            startFlicker();
            // request focus so key bindings on this panel can fire
            SwingUtilities.invokeLater(() -> this.requestFocusInWindow());
        }

        private void deselectThisTask() {
            isSelected = false;
            stopFlicker();
            checkBox.setSelected(false);
            if (selectedTask == this) {
                selectedTask = null;
            }
            resetInnerBorder();
        }

        private void startFlicker() {
            if (flickerTimer != null && flickerTimer.isRunning()) {
                return; // Already flickering
            }

            Border currentBorder = getBorder();
            if (!(currentBorder instanceof CompoundBorder)) return;
            Border outerBorder = ((CompoundBorder) currentBorder).getOutsideBorder();
            Border originalInner = ((CompoundBorder) currentBorder).getInsideBorder();

            Border flickerBorder = BorderFactory.createLineBorder(UniversalThemes.ACCENT_COLOR, 2);

            flickerTimer = new Timer(300, null); // 300 ms delay for slower flicker
            final int[] count = {0};
            flickerTimer.addActionListener(ev -> {
                if (!isSelected) {
                    // Stop flickering if deselected
                    setBorder(BorderFactory.createCompoundBorder(outerBorder, originalInner));
                    flickerTimer.stop();
                    return;
                }
                if (count[0] % 2 == 0) {
                    setBorder(BorderFactory.createCompoundBorder(outerBorder, flickerBorder));
                } else {
                    setBorder(BorderFactory.createCompoundBorder(outerBorder, originalInner));
                }
                count[0]++;
            });
            flickerTimer.start();
        }

        private void stopFlicker() {
            if (flickerTimer != null) {
                flickerTimer.stop();
                flickerTimer = null;
            }
        }

        private void resetInnerBorder() {
            Border currentBorder = getBorder();
            if (!(currentBorder instanceof CompoundBorder)) return;
            Border outerBorder = ((CompoundBorder) currentBorder).getOutsideBorder();

            Border innerBorder;
            if (isSubtask) {
                innerBorder = BorderFactory.createLineBorder(UniversalThemes.BG_COMPONENT, 2);
            } else {
                innerBorder = BorderFactory.createLineBorder(UniversalThemes.BG_COMPONENT, 2);
            }
            setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
        }




        private void editEntry() {
            UniversalThemes.RoundedDialog rd = UniversalThemes.createRoundedDialogShell(frame, "Edit");

            JTextArea field = new JTextArea(getRawText(), 4, 70);
            field.setBackground(UniversalThemes.BG_COMPONENT);
            field.setForeground(UniversalThemes.TXT_PRIMARY);
            field.setCaretColor(UniversalThemes.ACCENT_COLOR);
            field.setFont(UniversalThemes.getCompositeFont(17));
            field.setLineWrap(true);
            field.setWrapStyleWord(true);
            field.setMargin(new Insets(10, 10, 10, 10));
            field.setBorder(null);
            UniversalThemes.applySelectionTheme(field);
            UniversalThemes.applyCollapseSelectionNavigation(field);
            UniversalThemes.freeCtrlTabFromTraversal(field);
            Hotstring.attach(field);

            // Pre-size rows to fit existing content, capped at 10
            int existingLines = field.getLineCount();
            field.setRows(Math.min(Math.max(existingLines, 4), 10));

            field.addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { field.selectAll(); }
            });

            JScrollPane scrollPane = new JScrollPane(field);
            scrollPane.setBorder(BorderFactory.createLineBorder(UniversalThemes.BORDER_COLOR1, 1));
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            UniversalThemes.applyScrollbarTheme(scrollPane);
            scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
            scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

            rd.body.add(scrollPane);
            rd.body.add(Box.createVerticalStrut(18));

            // Auto-grow + auto-scroll-to-caret
            field.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { adjust(); scrollToCaret(); }
                public void removeUpdate(DocumentEvent e) { adjust(); scrollToCaret(); }
                public void changedUpdate(DocumentEvent e) { adjust(); scrollToCaret(); }

                private void adjust() {
                    int rows = field.getLineCount();
                    if (rows > field.getRows()) {
                        field.setRows(Math.min(rows, 10));
                        rd.dialog.pack();
                        UniversalThemes.finalizeRoundedDialog(rd.dialog, frame);
                    }
                }

                private void scrollToCaret() {
                    if (field.getCaretPosition() == field.getDocument().getLength()) {
                        SwingUtilities.invokeLater(() -> field.setCaretPosition(field.getDocument().getLength()));
                    }
                }
            });

            Runnable submit = () -> {
                String newText = field.getText(); // preserve whitespace/newlines, don't trim here
                if (!newText.trim().isEmpty()) {
                    String oldTextForUndo = rawText;
                    rawText = newText;
                    Markdown.MarkdownRenderer.render(textArea.getStyledDocument(), rawText);
                    MarkdownDebug.summary("[TaskItem] Re-rendered after edit for id=" + id);
                    actionUndoManager.log(new Undo.EditEntry(0, currentRegisterId, id, oldTextForUndo));
                    if (!checkBox.isSelected()) {
                        textArea.setForeground(UniversalThemes.TXT_PRIMARY);
                    }
                    deselectThisTask();
                    saveTasks();
                    rd.dialog.dispose();
                } else {
                    boolean confirmed = UniversalThemes.showDeleteConfirmPopup(
                            frame,
                            "Delete Entry",
                            "empty entry",
                            "The text was cleared. This cannot be undone."
                    );
                    if (confirmed) {
                        deselectThisTask();
                        rd.dialog.dispose();
                        DeleteEmptyTask();
                    }
                    // if not confirmed: leave dialog open, let them keep editing
                }
            };

            InputMap im = field.getInputMap(JComponent.WHEN_FOCUSED);
            ActionMap am = field.getActionMap();

            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK), "insert-newline");
            am.put("insert-newline", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    int caretPos = field.getCaretPosition();
                    String text = field.getText();
                    field.setText(text.substring(0, caretPos) + "\n" + text.substring(caretPos));
                    field.setCaretPosition(caretPos + 1);
                }
            });

            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "submit-edit");
            am.put("submit-edit", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) { submit.run(); }
            });

            JButton saveButton = UniversalThemes.createRoundedDialogButton("Save", UniversalThemes.ACCENT_COLOR,
                    UniversalThemes.TXT_SELECTED, UniversalThemes.ACCENT_COLOR_DARK);
            JButton cancelButton = UniversalThemes.createRoundedDialogButton("Cancel", UniversalThemes.BG_COMPONENT,
                    UniversalThemes.TXT_PRIMARY, UniversalThemes.BORDER_COLOR1);
            saveButton.addActionListener(e -> submit.run());
            cancelButton.addActionListener(e -> {
                deselectThisTask();
                rd.dialog.dispose();
            });

            JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            buttonRow.setOpaque(false);
            buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            buttonRow.add(saveButton);
            UniversalThemes.wireDialogButtonNavigation(saveButton, cancelButton);
            rd.body.add(buttonRow);

            rd.dialog.addWindowListener(new WindowAdapter() {
                @Override public void windowClosing(WindowEvent e) { deselectThisTask(); }
            });

            UniversalThemes.finalizeRoundedDialog(rd.dialog, frame);
            SwingUtilities.invokeLater(() -> { field.requestFocusInWindow(); field.selectAll(); });
            rd.dialog.setVisible(true);
        }


        private void createSubEntry() {
            if (countSubEntries(getId()) >= 999) {
                UniversalThemes.showPopup(frame,
                        "This Entry already has 999 sub-Entries, which is the maximum allowed.",
                        "Sub-Entry Limit Reached");
                return;
            }
            UniversalThemes.RoundedDialog rd = UniversalThemes.createRoundedDialogShell(frame, "Create Sub-Entry");

            JTextArea field = new JTextArea(4, 70);
            field.setBackground(UniversalThemes.BG_COMPONENT);
            field.setForeground(UniversalThemes.TXT_PRIMARY);
            field.setCaretColor(UniversalThemes.ACCENT_COLOR);
            field.setFont(UniversalThemes.getCompositeFont(17));
            field.setLineWrap(true);
            field.setWrapStyleWord(true);
            field.setMargin(new Insets(10, 10, 10, 10));
            field.setBorder(null);
            UniversalThemes.applySelectionTheme(field);
            UniversalThemes.applyCollapseSelectionNavigation(field);
            UniversalThemes.freeCtrlTabFromTraversal(field);
            Hotstring.attach(field);

            JScrollPane scrollPane = new JScrollPane(field);
            scrollPane.setBorder(BorderFactory.createLineBorder(UniversalThemes.BORDER_COLOR1, 1));
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            UniversalThemes.applyScrollbarTheme(scrollPane);
            scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
            scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

            rd.body.add(scrollPane);
            rd.body.add(Box.createVerticalStrut(18));

            // Auto-grow
            field.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { adjust(); }
                public void removeUpdate(DocumentEvent e) { adjust(); }
                public void changedUpdate(DocumentEvent e) { adjust(); }
                private void adjust() {
                    int rows = field.getLineCount();
                    if (rows > field.getRows()) {
                        field.setRows(Math.min(rows, 10));
                        rd.dialog.pack();
                        UniversalThemes.finalizeRoundedDialog(rd.dialog, frame);
                    }
                }
            });

            Runnable submit = () -> {
                String subtaskText = field.getText().trim();
                if (!subtaskText.isEmpty()) {
                    rd.dialog.dispose();
                    TaskItem subtask = new TaskItem(taskCounter++, subtaskText, false, true, false, getId());
                    idToTaskMap.put(subtask.getId(), subtask);
                    allTasks.add(subtask);
                    actionUndoManager.log(new Undo.CreateEntry(0, currentRegisterId, subtask.getId()));

                    int insertIndex = -1;
                    for (int i = 0; i < taskPanel.getComponentCount(); i++) {
                        Component comp = taskPanel.getComponent(i);
                        if (comp == TaskItem.this) {
                            insertIndex = i;
                        } else if (insertIndex != -1 && comp instanceof TaskItem) {
                            TaskItem t = (TaskItem) comp;
                            if (!t.isSubtask() || t.getParentId() != TaskItem.this.id) break;
                            insertIndex = i;
                        }
                    }
                    taskPanel.add(subtask, insertIndex + 1);
                    taskPanel.revalidate();
                    taskPanel.repaint();
                    renumberAllTasks();
                    saveTasks();
                    SwingUtilities.invokeLater(() -> scrollToTaskWithPadding(subtask));
                }
            };

            InputMap im = field.getInputMap(JComponent.WHEN_FOCUSED);
            ActionMap am = field.getActionMap();

            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK), "insert-newline");
            am.put("insert-newline", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    int caretPos = field.getCaretPosition();
                    String text = field.getText();
                    field.setText(text.substring(0, caretPos) + "\n" + text.substring(caretPos));
                    field.setCaretPosition(caretPos + 1);
                }
            });

            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "submit-subtask");
            am.put("submit-subtask", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) { submit.run(); }
            });

            JButton addButton = UniversalThemes.createRoundedDialogButton("Create", UniversalThemes.ACCENT_COLOR,
                    UniversalThemes.TXT_SELECTED, UniversalThemes.ACCENT_COLOR_DARK);
            JButton cancelButton = UniversalThemes.createRoundedDialogButton("Cancel", UniversalThemes.BG_COMPONENT,
                    UniversalThemes.TXT_PRIMARY, UniversalThemes.BORDER_COLOR1);
            addButton.addActionListener(e -> submit.run());
            cancelButton.addActionListener(e -> rd.dialog.dispose());

            JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            buttonRow.setOpaque(false);
            buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            buttonRow.add(addButton);
            UniversalThemes.wireDialogButtonNavigation(addButton, cancelButton);
            rd.body.add(buttonRow);

            UniversalThemes.finalizeRoundedDialog(rd.dialog, frame);
            SwingUtilities.invokeLater(field::requestFocusInWindow);
            rd.dialog.setVisible(true);
        }
        // New method: Move this task up within its allowed range, allowing repeated moves while selected
        private void moveTaskUp() {
            // If main task and open, collapse it first
            if (!isSubtask && !isCollapsed) {
                isCollapsed = true;
                Border newOuter = BorderFactory.createMatteBorder(1, 0, 20, 0, UniversalThemes.BORDER_COLOR1);
                Border currentBorder = getBorder();
                Border currentInner;
                if (currentBorder instanceof CompoundBorder) {
                    currentInner = ((CompoundBorder) currentBorder).getInsideBorder();
                } else {
                    currentInner = BorderFactory.createLineBorder(UniversalThemes.BG_COMPONENT, 2);
                }
                setBorder(BorderFactory.createCompoundBorder(newOuter, currentInner));
                hideSubEntries(this);
            }

            int currentIndex = indexOfInPanel(this);
            if (currentIndex <= 0) return; // Already at top or not found

            // Find the index of the task above to swap with, respecting main/subtask boundaries
            int swapIndex = -1;
            for (int i = currentIndex - 1; i >= 0; i--) {
                Component comp = taskPanel.getComponent(i);
                if (comp instanceof TaskItem) {
                    TaskItem t = (TaskItem) comp;
                    if (this.isSubtask) {
                        if (t.isSubtask() && t.getParentId() == this.parentId) {
                            swapIndex = i;
                            break;
                        } else if (!t.isSubtask()) {
                            break;
                        }
                    } else { // main task
                        if (!t.isSubtask()) {
                            swapIndex = i;
                            break;
                        }
                    }
                }
            }

            if (swapIndex == -1) return; // No valid task above to swap with

            Component aboveComp = taskPanel.getComponent(swapIndex);
            actionUndoManager.log(new Undo.MoveEntry(0, currentRegisterId, this.id, ((TaskItem) aboveComp).id));

            // If the swap partner is itself an expanded main entry, collapse it too --
            // otherwise its sub-entries get left behind at the old panel position.
            if (aboveComp instanceof TaskItem) {
                TaskItem aboveTask = (TaskItem) aboveComp;
                if (!aboveTask.isSubtask() && !aboveTask.isCollapsed) {
                    aboveTask.isCollapsed = true;
                    Border newOuter2 = BorderFactory.createMatteBorder(1, 0, 20, 0, UniversalThemes.BORDER_COLOR1);
                    Border currentBorder2 = aboveTask.getBorder();
                    Border currentInner2 = (currentBorder2 instanceof CompoundBorder)
                            ? ((CompoundBorder) currentBorder2).getInsideBorder()
                            : BorderFactory.createLineBorder(UniversalThemes.BG_COMPONENT, 2);
                    aboveTask.setBorder(BorderFactory.createCompoundBorder(newOuter2, currentInner2));
                    hideSubEntries(aboveTask);

                    // hideSubEntries changed the panel's component indices -- recompute both.
                    currentIndex = indexOfInPanel(this);
                    swapIndex = indexOfInPanel(aboveTask);
                }
            }

            // Swap components in taskPanel
            taskPanel.remove(this);
            taskPanel.remove(aboveComp);

            taskPanel.add(this, swapIndex);
            taskPanel.add(aboveComp, currentIndex);

            taskPanel.revalidate();
            taskPanel.repaint();
            renumberAllTasks();

            // Update allTasks list to keep consistent
            int allTasksIndexThis = allTasks.indexOf(this);
            int allTasksIndexAbove = allTasks.indexOf(aboveComp);
            if (allTasksIndexThis != -1 && allTasksIndexAbove != -1) {
                allTasks.set(allTasksIndexThis, (TaskItem) aboveComp);
                allTasks.set(allTasksIndexAbove, this);
            }

            saveTasks();

            // Ensure this task remains selected and focused for continuous moves
            if (!isSelected) {
                selectThisTask();
            }
            // Request focus on checkbox or text area to keep key events firing
            SwingUtilities.invokeLater(() -> {
                if (isSelected) {
                    textArea.requestFocusInWindow();
                }
                scrollToTaskWithPadding(this);
            });
        }

        // New method: Move this task down within its allowed range, allowing repeated moves while selected

        // New method: Move this task down within its allowed range, allowing repeated moves while selected
        private void moveTaskDown() {
// If main task and open, collapse it first
            if (!isSubtask && !isCollapsed) {
                isCollapsed = true;
                Border newOuter = BorderFactory.createMatteBorder(1, 0, 20, 0, UniversalThemes.BORDER_COLOR1);
                Border currentBorder = getBorder();
                Border currentInner;
                if (currentBorder instanceof CompoundBorder) {
                    currentInner = ((CompoundBorder) currentBorder).getInsideBorder();
                } else {
                    currentInner = BorderFactory.createLineBorder(UniversalThemes.BG_COMPONENT, 2);
                }
                setBorder(BorderFactory.createCompoundBorder(newOuter, currentInner));
                hideSubEntries(this);
            }

            int currentIndex = indexOfInPanel(this);
            int count = taskPanel.getComponentCount();
            if (currentIndex == -1 || currentIndex >= count - 1) return; // Already at bottom or not found

            // Find the index of the task below to swap with, respecting main/subtask boundaries
            int swapIndex = -1;
            for (int i = currentIndex + 1; i < count; i++) {
                Component comp = taskPanel.getComponent(i);
                if (comp instanceof TaskItem) {
                    TaskItem t = (TaskItem) comp;
                    if (this.isSubtask) {
                        if (t.isSubtask() && t.getParentId() == this.parentId) {
                            swapIndex = i;
                            break;
                        } else if (!t.isSubtask()) {
                            break;
                        }
                    } else { // main task
                        if (!t.isSubtask()) {
                            swapIndex = i;
                            break;
                        }
                    }
                }
            }

            if (swapIndex == -1) return; // No valid task below to swap with

            Component belowComp = taskPanel.getComponent(swapIndex);
            actionUndoManager.log(new Undo.MoveEntry(0, currentRegisterId, this.id, ((TaskItem) belowComp).id));

            // If the swap partner is itself an expanded main entry, collapse it too --
            // otherwise its sub-entries get left behind at the old panel position.
            if (belowComp instanceof TaskItem) {
                TaskItem belowTask = (TaskItem) belowComp;
                if (!belowTask.isSubtask() && !belowTask.isCollapsed) {
                    belowTask.isCollapsed = true;
                    Border newOuter2 = BorderFactory.createMatteBorder(1, 0, 20, 0, UniversalThemes.BORDER_COLOR1);
                    Border currentBorder2 = belowTask.getBorder();
                    Border currentInner2 = (currentBorder2 instanceof CompoundBorder)
                            ? ((CompoundBorder) currentBorder2).getInsideBorder()
                            : BorderFactory.createLineBorder(UniversalThemes.BG_COMPONENT, 2);
                    belowTask.setBorder(BorderFactory.createCompoundBorder(newOuter2, currentInner2));
                    hideSubEntries(belowTask);

                    // hideSubEntries changed the panel's component indices -- recompute both.
                    currentIndex = indexOfInPanel(this);
                    swapIndex = indexOfInPanel(belowTask);
                }
            }

            // Swap components in taskPanel
            taskPanel.remove(this);
            taskPanel.remove(belowComp);

            taskPanel.add(belowComp, currentIndex);
            taskPanel.add(this, swapIndex);

            taskPanel.revalidate();
            taskPanel.repaint();
            renumberAllTasks();

            // Update allTasks list to keep consistent
            int allTasksIndexThis = allTasks.indexOf(this);
            int allTasksIndexBelow = allTasks.indexOf(belowComp);
            if (allTasksIndexThis != -1 && allTasksIndexBelow != -1) {
                allTasks.set(allTasksIndexThis, (TaskItem) belowComp);
                allTasks.set(allTasksIndexBelow, this);
            }

            saveTasks();

            // Ensure this task remains selected and focused for continuous moves
            if (!isSelected) {
                selectThisTask();
            }
            // Request focus on checkbox or text area to keep key events firing
            SwingUtilities.invokeLater(() -> {
                if (isSelected) {
                    textArea.requestFocusInWindow();
                }
                scrollToTaskWithPadding(this);
            });
        }


        private void DeleteEmptyTask() {
            List<Component> toRemove = new ArrayList<>();
            toRemove.add(this); // Always remove the clicked task
            boolean hasSubtasks = false;

            if (!isSubtask) {
                // It's a main task, so find and mark its subtasks
                for (TaskItem task : allTasks) {
                    if (task.isSubtask() && task.getParentId() == this.id) {
                        toRemove.add(task);
                        hasSubtasks = true;
                    }
                }
            }

            for (Component c : toRemove) {
                taskPanel.remove(c);
                allTasks.remove(c);
            }
            saveTasks();
            taskPanel.revalidate();
            taskPanel.repaint();
            renumberAllTasks();

        }

        private void confirmDeleteTask() {
            List<Component> toRemove = new ArrayList<>();
            toRemove.add(this); // Always remove the clicked task
            boolean hasSubtasks = false;

            if (!isSubtask) {
                // It's a main task, so find and mark its subtasks
                for (TaskItem task : allTasks) {
                    if (task.isSubtask() && task.getParentId() == this.id) {
                        toRemove.add(task);
                        hasSubtasks = true;
                    }
                }
            }

            String entryPreview = getRawText().length() > 40
                    ? getRawText().substring(0, 40) + "..."
                    : getRawText();
            String subMessage = hasSubtasks
                    ? "This will also delete its sub-Entries. "
                    : "";

            boolean confirmed = UniversalThemes.showDeleteConfirmPopup(
                    frame,
                    "Delete Entry",
                    entryPreview,
                    subMessage
            );

            if (confirmed) {
                int anchorId = -1;
                int myIndex = allTasks.indexOf(this);
                for (int i = myIndex - 1; i >= 0; i--) {
                    if (!isSubtask && !allTasks.get(i).isSubtask) { anchorId = allTasks.get(i).getId(); break; }
                    if (isSubtask && allTasks.get(i).isSubtask && allTasks.get(i).getParentId() == this.parentId) { anchorId = allTasks.get(i).getId(); break; }
                    if (isSubtask && !allTasks.get(i).isSubtask) break;
                }

                Undo.TaskSnapshot mySnapshot = new Undo.TaskSnapshot(id, parentId, getRawText(), isDone(), isSubtask, isCollapsed);
                List<Undo.TaskSnapshot> childSnapshots = new ArrayList<>();
                for (Component c : toRemove) {
                    if (c != this && c instanceof TaskItem t) {
                        childSnapshots.add(new Undo.TaskSnapshot(t.id, t.parentId, t.getRawText(), t.isDone(), t.isSubtask, t.isCollapsed));
                    }
                }

                actionUndoManager.log(new Undo.DeleteEntry(0, currentRegisterId, mySnapshot, childSnapshots, anchorId));
                for (Component c : toRemove) {
                    taskPanel.remove(c);
                    allTasks.remove(c);
                }
                saveTasks();
                taskPanel.revalidate();
                taskPanel.repaint();
                renumberAllTasks();
            }

        }

        public void applySearchHighlight() {
            isSearchHighlighted = true;
            Border currentBorder = getBorder();
            Border outerBorder = (currentBorder instanceof CompoundBorder)
                    ? ((CompoundBorder) currentBorder).getOutsideBorder()
                    : BorderFactory.createEmptyBorder();
            Border highlightInner = BorderFactory.createLineBorder(UniversalThemes.SEARCH_HIGHLIGHT_COLOR, 2);
            setBorder(BorderFactory.createCompoundBorder(outerBorder, highlightInner));
        }

        public void clearSearchHighlight() {
            isSearchHighlighted = false;
            resetInnerBorder();
        }

    }
}