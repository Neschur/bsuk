import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MainWindow extends JFrame{
    private JComboBox petList = null;
    private JTable table = null;
    private DefaultTableModel tableModel = null;
    private List<String> columnNames = null;
    private List<String> columnTypes = null;
    private String selectedTable = null;

    private JTable tableSchema = null;
    private String selectedTableSchema = null;
    private List<String> columnOldNamesSchema = null;

    private JFrame frame = null;

    private boolean flag=false;

    public MainWindow() {
        super("Интерфейс бд");
        frame = this;
        setBounds(100, 100, 650, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabbedPane = new JTabbedPane();

        JComponent panel1 = makeBasePanel();
        tabbedPane.addTab("База данных", null, panel1);
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        JComponent panel2 = makeSchemaPanel();
        tabbedPane.addTab("Схема", null, panel2);
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_2);

        this.add(tabbedPane);
    }

    private DefaultTableModel getTableModel() {
        columnNames = new ArrayList<>();
        columnTypes = new ArrayList<>();
        try {
            ResultSet rs = Main.conn.createStatement().executeQuery("SHOW COLUMNS FROM " + selectedTable);
            while(rs.next()) {
                columnNames.add(rs.getString(1));
                columnTypes.add(rs.getString(2));
            }

        } catch (SQLException e1) {
            JOptionPane.showMessageDialog(frame, e1.getMessage(), "Ошибка!", JOptionPane.ERROR_MESSAGE);
            e1.printStackTrace();
        }

        List<Object> data0 = new ArrayList<>();

        try {
            ResultSet rs = Main.conn.createStatement().executeQuery("SELECT * FROM " + selectedTable);
            while(rs.next()) {
                Object data1[] = new Object[columnNames.size()];
                for (int i = 1; i < columnNames.size() + 1; i++) {
                    data1[i-1] = rs.getString(i);
                }
                data0.add(data1);
            }

        } catch (SQLException e1) {
            JOptionPane.showMessageDialog(frame, e1.getMessage(), "Ошибка!", JOptionPane.ERROR_MESSAGE);
            e1.printStackTrace();
        }


        Object[][] data = new Object[data0.size()][columnNames.size()];
        for (int i = 0; i < data0.size(); i++) {
            data[i] = (Object[])data0.get(i);
        }

        tableModel = new DefaultTableModel(data, columnNames.toArray());

        tableModel.addTableModelListener(e -> {
            try {
                System.out.println(flag);
                if(flag) {
                    flag = false;
                    return;
                }
                String value = (String) tableModel.getValueAt(e.getFirstRow(), e.getColumn());
                if(columnTypes.get(e.getColumn()).equals("int(11)") && !isInteger(value)) {
                    System.out.println("XX");
                    value = "NULL";
                    flag = true;
                    tableModel.setValueAt("", e.getFirstRow(), e.getColumn());
                } else {
                    value = "'" + value + "'";
                }

                System.out.println("UPDATE " + selectedTable + " SET " +
                        columnNames.get(e.getColumn()) + " = " + value +
                        " WHERE id=" + tableModel.getValueAt(e.getFirstRow(), 0));
                Main.conn.createStatement().executeUpdate("UPDATE " + selectedTable + " SET " +
                            columnNames.get(e.getColumn()) + " = " + value +
                            " WHERE id=" + tableModel.getValueAt(e.getFirstRow(), 0));

            } catch (SQLException e1) {
                flag=true;
                tableModel.setValueAt("", e.getFirstRow(), e.getColumn());
                JOptionPane.showMessageDialog(frame, e1.getMessage(), "Ошибка!", JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }
        });

        return tableModel;
    }

    private JPanel makeBasePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel selectPanel = new JPanel();
        selectPanel.setLayout(new BoxLayout(selectPanel, BoxLayout.X_AXIS));
        selectPanel.setMaximumSize(new Dimension(2600,24));

        List<String> petStrings = new ArrayList<>();
        try {
            ResultSet rs = Main.conn.createStatement().executeQuery("SHOW TABLES");
            while(rs.next()) {
                petStrings.add(rs.getString(1));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, e.getMessage(), "Ошибка!", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        petList = new JComboBox(petStrings.toArray());
        petList.setMaximumSize(new Dimension(122, 30));
//        petList.setSelectedIndex(0);
        selectPanel.add(petList);

        table = new JTable();

        JButton buttonAdd = new JButton("Add new line");
        JButton buttonRemove = new JButton("Remove selected lines");
        buttonRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    for(int id : table.getSelectedRows()) {

                        Main.conn.createStatement().executeUpdate("DELETE from " + selectedTable + " WHERE id=" +
                                table.getModel().getValueAt(id, 0)
                                );
                    }

                } catch (SQLException e1) {
                    JOptionPane.showMessageDialog(frame, e1.getMessage(), "Ошибка!", JOptionPane.ERROR_MESSAGE);
                    e1.printStackTrace();
                }
                table.setModel(getTableModel());
            }
        });

        JButton selectButton = new JButton("select");
        selectButton.addActionListener(e -> {
            if(petList.getSelectedItem() == null)
                return;
            selectedTable = petList.getSelectedItem().toString();
            table.setModel(getTableModel());
            buttonAdd.setEnabled(true);

        });
        selectPanel.add(selectButton);

        selectPanel.add(Box.createHorizontalStrut(120));

        buttonAdd.setEnabled(false);
        selectPanel.add(buttonAdd);
        selectPanel.add(buttonRemove);
        buttonAdd.addActionListener(e -> {
            try {
                Main.conn.createStatement().executeUpdate("INSERT INTO " + selectedTable + " VALUES()");
                table.setModel(getTableModel());

            } catch (SQLException e1) {
                JOptionPane.showMessageDialog(frame, e1.getMessage(), "Ошибка!", JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }
        });

        panel.add(selectPanel);
        panel.add(Box.createVerticalStrut(20));

        panel.add(new JScrollPane(table));
        return panel;
    }

    private JPanel makeSchemaPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel selectPanel = new JPanel();
        selectPanel.setLayout(new BoxLayout(selectPanel, BoxLayout.X_AXIS));
        selectPanel.setMaximumSize(new Dimension(2600,24));

        JComboBox petListSchema = new JComboBox(petList.getModel());
        petListSchema.setMaximumSize(new Dimension(122, 30));
        selectPanel.add(petListSchema);

        panel.add(selectPanel);

        JButton removeTablesBtn = new JButton("Remove selected TABLE!");
        JButton addBtn = new JButton("Add new line");
        JButton removeBtn = new JButton("Remvoe selected");
        JButton selectButton = new JButton("Select");
        selectButton.addActionListener(e -> {
            if(petList.getSelectedItem() == null)
                return;
            selectedTableSchema = petListSchema.getSelectedItem().toString();
            tableSchema.setModel(getTableSchemaModel());
            removeTablesBtn.setEnabled(true);
            addBtn.setEnabled(true);
            removeBtn.setEnabled(true);

        });
        selectPanel.add(selectButton);

        selectPanel.add(Box.createHorizontalStrut(60));

        removeTablesBtn.setEnabled(false);
        selectPanel.add(removeTablesBtn);
        removeTablesBtn.addActionListener(e -> {
            try {
                Main.conn.createStatement().executeUpdate("DROP TABLE " + selectedTableSchema);
                tableSchema.setModel(new DefaultTableModel());
                updateComboBox(petListSchema);
                updateComboBox(petList);
                addBtn.setEnabled(false);
                removeBtn.setEnabled(false);

            } catch (SQLException e1) {
                JOptionPane.showMessageDialog(frame, e1.getMessage(), "Ошибка!", JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }
        });

        JPanel addNewTable = new JPanel();
        addNewTable.setLayout(new BoxLayout(addNewTable, BoxLayout.X_AXIS));
        addNewTable.setMaximumSize(new Dimension(2600, 24));
        JTextField tableNamefield = new JTextField();
        JButton newtableBtn = new JButton("Create new table with name:");
        addNewTable.add(newtableBtn);
        addNewTable.add(tableNamefield);

        newtableBtn.addActionListener(e -> {
            try {
                if(tableNamefield.getText().equals(""))
                    return;

                Main.conn.createStatement().executeUpdate("CREATE TABLE "+tableNamefield.getText() +"(id int NOT NULL AUTO_INCREMENT, PRIMARY KEY (id))");
                tableNamefield.setText("");
                updateComboBox(petListSchema);
                updateComboBox(petList);

            } catch (SQLException e1) {
                JOptionPane.showMessageDialog(frame, e1.getMessage(), "Ошибка!", JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }
        });

        panel.add(Box.createVerticalStrut(10));
        panel.add(addNewTable);
        panel.add(Box.createVerticalStrut(10));



        tableSchema = new JTable();

        panel.add(new JScrollPane(tableSchema));

        panel.add(Box.createVerticalStrut(20));
        JPanel addRemovePanel = new JPanel();
        addRemovePanel.setLayout(new BoxLayout(addRemovePanel, BoxLayout.X_AXIS));

        addBtn.setEnabled(false);
        addBtn.addActionListener(e -> {
            try {
                Main.conn.createStatement().executeUpdate("ALTER TABLE "+selectedTableSchema+
                        " ADD COLUMN new_column"+(int)(Math.random()*1e9)+" VARCHAR(255)");
            } catch (SQLException e1) {
                JOptionPane.showMessageDialog(frame, e1.getMessage(), "Ошибка!", JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }
            tableSchema.setModel(getTableSchemaModel());

        });




        removeBtn.setEnabled(false);
        removeBtn.addActionListener(e -> {
            try {
                for(int id : tableSchema.getSelectedRows()) {
                    Main.conn.createStatement().executeUpdate("ALTER TABLE "+selectedTableSchema+
                            " DROP COLUMN "+tableSchema.getModel().getValueAt(id, 0));
                }

            } catch (SQLException e1) {
                JOptionPane.showMessageDialog(frame, e1.getMessage(), "Ошибка!", JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }
            tableSchema.setModel(getTableSchemaModel());

        });

        addRemovePanel.add(addBtn);
        addRemovePanel.add(Box.createHorizontalGlue());
        addRemovePanel.add(removeBtn);

        panel.add(addRemovePanel);

        return panel;
    }

    private DefaultTableModel getTableSchemaModel() {
        List<Object> data0 = new ArrayList<>();
        columnOldNamesSchema = new ArrayList<>();
        try {
            ResultSet rs = Main.conn.createStatement().executeQuery("SHOW COLUMNS FROM " + selectedTableSchema);
            while(rs.next()) {
                Object arr[] = new Object[2];
                columnOldNamesSchema.add(rs.getString(1));
                arr[0] = rs.getString(1);
                arr[1] = rs.getString(2);
                data0.add(arr);
            }

        } catch (SQLException e1) {
            JOptionPane.showMessageDialog(frame, e1.getMessage(), "Ошибка!", JOptionPane.ERROR_MESSAGE);
            e1.printStackTrace();
        }

        Object[][] data = new Object[data0.size()][2];
        for (int i = 0; i < data0.size(); i++) {
            data[i] = (Object[])data0.get(i);
        }
        Integer[] ia = {1,2};
        DefaultTableModel tableModel = new DefaultTableModel(data, ia);

        tableModel.addTableModelListener(e -> {
            try {
                String value = tableModel.getValueAt(e.getFirstRow(), 0).toString();

                String qq = "ALTER TABLE "+selectedTableSchema+
                        " CHANGE " +columnOldNamesSchema.get(e.getFirstRow())+ " "
                        +tableModel.getValueAt(e.getFirstRow(), 0)+" "
                        + tableModel.getValueAt(e.getFirstRow(), 1);

                Main.conn.createStatement().executeUpdate(qq);
                columnOldNamesSchema.set(e.getFirstRow(), tableModel.getValueAt(e.getFirstRow(), 0).toString());

            } catch (SQLException e1) {
                JOptionPane.showMessageDialog(frame, e1.getMessage(), "Ошибка!", JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }
        });

        return tableModel;
    }

    public void updateComboBox(JComboBox box) {
        List<String> petStrings = new ArrayList<>();
        try {
            ResultSet rs = Main.conn.createStatement().executeQuery("SHOW TABLES");
            while(rs.next()) {
                petStrings.add(rs.getString(1));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, e.getMessage(), "Ошибка!", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        box.removeAllItems();
        for (String a:petStrings){
            box.addItem(a);
        }

    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        return true;
    }
}
