import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Professional-looking Cricket Management System using Stack (LIFO)
 * Clean version — starts empty
 * Dynamic field: "Runs" changes to "Wickets" if Role = Bowler
 */
public class CricketManagementSystem extends JFrame {

    // --------------------- Model ---------------------
    static class Player {
        int id;
        String name;
        String role;
        int matches;
        int stat; // runs or wickets depending on role

        Player(int id, String name, String role, int matches, int stat) {
            this.id = id;
            this.name = name;
            this.role = role;
            this.matches = matches;
            this.stat = stat;
        }

        Object[] toRow() {
            String statName = role.equalsIgnoreCase("Bowler") ? "Wickets" : "Runs";
            return new Object[]{id, name, role, matches, stat + " " + statName};
        }
    }

    // --------------------- Stack Manager ---------------------
    static class PlayerStackManager {
        private final Stack<Player> stack = new Stack<>();

        void push(Player p) { stack.push(p); }

        Player pop() {
            if (stack.isEmpty()) return null;
            return stack.pop();
        }

        List<Player> getAllPlayersTopToBottom() {
            List<Player> list = new ArrayList<>(stack);
            Collections.reverse(list);
            return list;
        }

        Player findById(int id) {
            for (Player p : stack) if (p.id == id) return p;
            return null;
        }

        boolean deleteById(int id) {
            if (stack.isEmpty()) return false;
            Stack<Player> temp = new Stack<>();
            boolean found = false;
            while (!stack.isEmpty()) {
                Player p = stack.pop();
                if (p.id == id) {
                    found = true;
                    break;
                } else {
                    temp.push(p);
                }
            }
            while (!temp.isEmpty()) stack.push(temp.pop());
            return found;
        }

        boolean updateById(int id, String name, String role, int matches, int stat) {
            if (stack.isEmpty()) return false;
            Stack<Player> temp = new Stack<>();
            boolean found = false;
            while (!stack.isEmpty()) {
                Player p = stack.pop();
                if (p.id == id) {
                    p.name = name;
                    p.role = role;
                    p.matches = matches;
                    p.stat = stat;
                    found = true;
                }
                temp.push(p);
            }
            while (!temp.isEmpty()) stack.push(temp.pop());
            return found;
        }

        boolean isEmpty() { return stack.isEmpty(); }
        int size() { return stack.size(); }
    }

    // --------------------- UI Components ---------------------
    private final PlayerStackManager manager = new PlayerStackManager();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Role", "Matches", "Runs/Wickets"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    private final JTextField txtId = new JTextField();
    private final JTextField txtName = new JTextField();
    private final JComboBox<String> comboRole;
    private final JTextField txtMatches = new JTextField();
    private final JTextField txtStat = new JTextField();
    private final JLabel lblStat = new JLabel("Runs:");
    private final JTextField txtSearch = new JTextField();
    private final JLabel lblStatus = new JLabel("Ready");

    public CricketManagementSystem() {
        setTitle("Cricket Management System — Professional (Light Theme)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 640);
        setLocationRelativeTo(null);

        comboRole = new JComboBox<>(new String[]{"Batsman", "Bowler", "All-Rounder", "Wicket-Keeper", "Coach"});
        initUI();
        refreshTable();
    }

    private void initUI() {
        Color bg = new Color(245, 247, 250);
        Color panel = Color.WHITE;
        Color accent = new Color(29, 112, 184);
        Color text = new Color(30, 30, 30);

        getContentPane().setBackground(bg);
        setLayout(new BorderLayout(12, 12));

        // ---------- TOP ----------
        JPanel topBar = new JPanel(new BorderLayout(12, 12));
        topBar.setBorder(new EmptyBorder(12, 12, 0, 12));
        topBar.setBackground(bg);

        JLabel lblTitle = new JLabel("Cricket Management System");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(accent);
        topBar.add(lblTitle, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new BorderLayout(6, 6));
        searchPanel.setBackground(bg);
        txtSearch.setPreferredSize(new Dimension(260, 30));
        txtSearch.setToolTipText("Search by ID or Name");
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        JButton btnClearSearch = new JButton("Clear");
        searchPanel.add(btnClearSearch, BorderLayout.EAST);
        topBar.add(searchPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // ---------- MAIN ----------
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(0, 12, 12, 12));
        mainPanel.setBackground(bg);

        // Left form
        JPanel left = new JPanel();
        left.setBackground(panel);
        left.setPreferredSize(new Dimension(360, 0));
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel formTitle = new JLabel("Player Details");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        left.add(formTitle);
        left.add(Box.createRigidArea(new Dimension(0, 10)));

        left.add(fieldRow("ID:", txtId));
        left.add(fieldRow("Name:", txtName));
        left.add(fieldRow("Role:", comboRole));
        left.add(fieldRow("Matches:", txtMatches));

        // Dynamic label for Runs/Wickets
        JPanel statPanel = new JPanel(new BorderLayout(6, 6));
        statPanel.setBackground(panel);
        lblStat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStat.setPreferredSize(new Dimension(70, 24));
        statPanel.add(lblStat, BorderLayout.WEST);
        txtStat.setPreferredSize(new Dimension(220, 30));
        txtStat.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        statPanel.add(txtStat, BorderLayout.CENTER);
        left.add(statPanel);

        comboRole.addActionListener(e -> updateStatLabel());
        left.add(Box.createRigidArea(new Dimension(0, 8)));

        // Buttons
        JPanel btnGroup = new JPanel(new GridLayout(3, 2, 8, 8));
        btnGroup.setBackground(panel);
        JButton btnPush = styledButton("Add Player (Push)", new Color(37, 150, 190));
        JButton btnPop = styledButton("Remove Last (Pop)", new Color(220, 65, 65));
        JButton btnUpdate = styledButton("Update by ID", new Color(100, 180, 100));
        JButton btnDelete = styledButton("Delete by ID", new Color(200, 100, 60));
        JButton btnClear = styledButton("Clear Fields", new Color(150, 150, 150));

        btnGroup.add(btnPush);
        btnGroup.add(btnPop);
        btnGroup.add(btnUpdate);
        btnGroup.add(btnDelete);
        btnGroup.add(btnClear);

        left.add(btnGroup);
        left.add(Box.createRigidArea(new Dimension(0, 8)));
        left.add(lblStatus);

        mainPanel.add(left, BorderLayout.WEST);

        // Table
        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.setBackground(panel);
        right.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel lblList = new JLabel("Players (Top → Bottom)");
        lblList.setFont(new Font("Segoe UI", Font.BOLD, 16));
        right.add(lblList, BorderLayout.NORTH);

        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        right.add(new JScrollPane(table), BorderLayout.CENTER);
        mainPanel.add(right, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        // ---------- Actions ----------
        btnPush.addActionListener(e -> addPlayer());
        btnPop.addActionListener(e -> popPlayer());
        btnUpdate.addActionListener(e -> updatePlayer());
        btnDelete.addActionListener(e -> deletePlayer());
        btnClear.addActionListener(e -> clearFields());
        btnClearSearch.addActionListener(e -> txtSearch.setText(""));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void insertUpdate(DocumentEvent e) { filter(); }
            private void filter() {
                String q = txtSearch.getText().trim();
                if (q.isEmpty()) refreshTable(); else filterTable(q);
            }
        });
    }

    private void updateStatLabel() {
        String role = comboRole.getSelectedItem().toString();
        lblStat.setText(role.equalsIgnoreCase("Bowler") ? "Wickets:" : "Runs:");
    }

    private JPanel fieldRow(String label, Component field) {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBackground(Color.WHITE);
        JLabel l = new JLabel(label);
        l.setPreferredSize(new Dimension(70, 24));
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        p.add(l, BorderLayout.WEST);
        if (field instanceof JComponent c) {
            c.setPreferredSize(new Dimension(220, 26));
            c.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
            p.add(c, BorderLayout.CENTER);
        }
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        p.setBorder(new EmptyBorder(6, 0, 6, 0));
        return p;
    }

    private JButton styledButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return b;
    }

    private void addPlayer() {
        try {
            int id = Integer.parseInt(txtId.getText().trim());
            if (manager.findById(id) != null) { showError("ID already exists."); return; }
            String name = txtName.getText().trim();
            String role = comboRole.getSelectedItem().toString();
            int matches = Integer.parseInt(txtMatches.getText().trim());
            int stat = Integer.parseInt(txtStat.getText().trim());
            Player p = new Player(id, name, role, matches, stat);
            manager.push(p);
            refreshTable();
            clearFields();
            setStatus("Player added. Total: " + manager.size());
        } catch (Exception e) { showError("Please enter valid numeric values."); }
    }

    private void popPlayer() {
        if (manager.isEmpty()) { showWarning("Stack is empty."); return; }
        Player removed = manager.pop();
        refreshTable();
        setStatus("Popped: " + removed.name);
    }

    private void updatePlayer() {
        try {
            int id = Integer.parseInt(txtId.getText().trim());
            String name = txtName.getText().trim();
            String role = comboRole.getSelectedItem().toString();
            int matches = Integer.parseInt(txtMatches.getText().trim());
            int stat = Integer.parseInt(txtStat.getText().trim());
            boolean found = manager.updateById(id, name, role, matches, stat);
            if (found) { refreshTable(); setStatus("Updated: ID " + id); clearFields(); }
            else showWarning("Player not found.");
        } catch (Exception e) { showError("Enter valid values."); }
    }

    private void deletePlayer() {
        try {
            int id = Integer.parseInt(txtId.getText().trim());
            if (manager.deleteById(id)) {
                refreshTable();
                setStatus("Deleted ID: " + id);
            } else showWarning("Not found.");
        } catch (Exception e) { showError("Enter valid ID."); }
    }

    private void refreshTable() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            for (Player p : manager.getAllPlayersTopToBottom())
                tableModel.addRow(p.toRow());
        });
    }

    private void filterTable(String q) {
        String lower = q.toLowerCase();
        tableModel.setRowCount(0);
        for (Player p : manager.getAllPlayersTopToBottom()) {
            if (String.valueOf(p.id).contains(lower) || p.name.toLowerCase().contains(lower))
                tableModel.addRow(p.toRow());
        }
    }

    private void clearFields() {
        txtId.setText(""); txtName.setText(""); txtMatches.setText(""); txtStat.setText("");
        comboRole.setSelectedIndex(0); lblStat.setText("Runs:");
    }

    private void setStatus(String s) { lblStatus.setText(s); }
    private void showError(String s) { JOptionPane.showMessageDialog(this, s, "Error", JOptionPane.ERROR_MESSAGE); }
    private void showWarning(String s) { JOptionPane.showMessageDialog(this, s, "Warning", JOptionPane.WARNING_MESSAGE); }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new CricketManagementSystem().setVisible(true));
    }
}
