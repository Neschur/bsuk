import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.DriverManager;
import java.sql.SQLException;

public class LoginWindow extends JFrame {
    public LoginWindow(){
        super("Login");

        setBounds(100, 100, 250, 220);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        add(mainPanel);

        JPanel dbPanel = new JPanel();
        dbPanel.setLayout(new BoxLayout(dbPanel, BoxLayout.X_AXIS));
        dbPanel.setMaximumSize(new Dimension(160,30));
        JLabel dbLabel = new JLabel("Базаданных:");
        dbPanel.add(dbLabel);
        JTextField dbField = new JTextField("bsu");
        dbPanel.add(dbField);

        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));

        JPanel localPanel = new JPanel();
        localPanel.setMaximumSize(new Dimension(130,30));
        localPanel.setLayout(new BoxLayout(localPanel, BoxLayout.X_AXIS));
        radioPanel.add(localPanel);
        JPanel netPanel = new JPanel();
        netPanel.setMaximumSize(new Dimension(130,30));
        netPanel.setLayout(new BoxLayout(netPanel, BoxLayout.X_AXIS));
        radioPanel.add(netPanel);

        JRadioButton localRadio = new JRadioButton();
        localPanel.add(localRadio);
        JLabel localLabel = new JLabel("localhost");
        localPanel.add(localLabel);

        JRadioButton networkRadio = new JRadioButton();
        netPanel.add(networkRadio);
        JTextField addressText = new JTextField("siarhei.by:3306");
        addressText.setMaximumSize(new Dimension(100,24));
        netPanel.add(addressText);

        ButtonGroup group = new ButtonGroup();
        group.add(localRadio);
        group.add(networkRadio);
        group.setSelected(localRadio.getModel(), true);

        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel("login");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);
        JTextField loginField = new JTextField();
        loginField.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginField.setMaximumSize(new Dimension(150,24));
        panel.add(loginField);

        JLabel passwordLabel = new JLabel("password");
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(passwordLabel);
        JTextField passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(150,24));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(passwordField);

        JButton start = new JButton("login");
        start.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(start);

        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(dbPanel);
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(radioPanel);
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(panel);
        mainPanel.add(Box.createVerticalGlue());

        JFrame frame = this;

        start.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String host = "localhost";
                    if(networkRadio.isSelected()) {
                        host = addressText.getText();
                    }
                    String login = loginField.getText();
                    String password = passwordField.getText();
                    String basename = dbField.getText();

                    String dbpath = "jdbc:mysql://" + host + "/" + basename + "?" +
                            "user=" + login + "&password=" + password + "&useUnicode=true&characterEncoding=UTF8";

                    Main.conn = DriverManager.getConnection(dbpath);
                    frame.setVisible(false);
                    Main.main2();
                } catch (SQLException e1) {
                    JOptionPane.showMessageDialog(frame, "Проверте логин и пароль, имя базы данных и адрес сервера",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}
