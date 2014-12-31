package org.jluzhjsj.maksyc.javadesign;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class ChatLogin {

    class MyWindosAdapter extends WindowAdapter {

        private Login loginapp;

        public MyWindosAdapter(Login loginapp) {
            this.loginapp = loginapp;
        }

        public void windowClosing(WindowEvent e) {
            loginapp.setVisible(true);
        }
    }

    @SuppressWarnings("serial")
    class MyJDialog extends JDialog implements ActionListener {

        @SuppressWarnings("unused")
        private String labelStr;

        public MyJDialog(String labelStr) {
            this.labelStr = labelStr;

            JLabel label = new JLabel(labelStr);
            label.setPreferredSize(new Dimension(180, 35));
            JButton ok = new JButton(" OK ");
            Container c = this.getContentPane();
            c.setLayout(new FlowLayout());
            c.add(label);
            c.add(ok);
            ok.addActionListener(this);
        }

        public void ShowMyDialog() {
            setSize(250, 115);
            setVisible(true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setVisible(false);
        }

    }

    MyJDialog postDialog = new MyJDialog("The post doesn't match!");
    MyJDialog pwdDialog = new MyJDialog("The password doesn't match!");
    MyJDialog userDialog = new MyJDialog("The user doesn't exist!");
    MyJDialog regDialog = new MyJDialog("Registered successfully!");
    JDBCManager database = new JDBCManager();


    @SuppressWarnings("serial")
    class Login extends JFrame implements ActionListener, DocumentListener {

        JLabel loginlabel = new JLabel("用户名:");
        JLabel passwdlabel = new JLabel("密码:");
        JLabel postlabel = new JLabel("客户端用端口号(6001-65535):");
        JLabel unused = new JLabel("            ");
        JTextField logintext = new JTextField(14);
        JPasswordField passwdtext = new JPasswordField(14);
        JTextField posttext = new JTextField(5);
        JButton loginbutton = new JButton("Login");
        JButton register = new JButton("Register");
        Panel textpanel = new Panel();
        Panel loginpanel = new Panel();
        int post;

        public Login() {
            super("聊天室登陆窗口");
            Container c = this.getContentPane();

            textpanel.setPreferredSize(new Dimension(320, 120));
            loginpanel.setPreferredSize(new Dimension(320, 60));

            loginlabel.setPreferredSize(new Dimension(85, 35));
            passwdlabel.setPreferredSize(new Dimension(85, 35));
            postlabel.setPreferredSize(new Dimension(190, 35));

            loginbutton.setPreferredSize(new Dimension(100, 25));
            register.setPreferredSize(new Dimension(100, 25));

            c.setLayout(new FlowLayout());

            c.add(textpanel);
            c.add(loginpanel);
            textpanel.add(loginlabel);
            textpanel.add(logintext);
            textpanel.add(passwdlabel);
            textpanel.add(passwdtext);
            textpanel.add(postlabel);
            textpanel.add(posttext);
            loginpanel.add(loginbutton);
            loginpanel.add(unused);
            loginpanel.add(register);
            loginbutton.addActionListener(this);
            register.addActionListener(this);
            Document doc = logintext.getDocument();
            doc.addDocumentListener(this);

        }

        public void CosnelDocument(Document doc) {
            String str = null;
            try {
                str = doc.getText(0, doc.getLength());
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
            if (str.equals("admin")) {
                posttext.setText("");
                posttext.setEditable(false);
            } else {
                posttext.setEditable(true);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("Register")) {
                Register app = new Register(this);
                app.setSize(320, 220);
                app.setVisible(true);
                app.setResizable(false);
                this.setVisible(false);
            } else {
                try {
                    Integer post = Integer.parseInt(posttext.getText());
                    if (post < 6001 || post > 65535) {
                        posttext.setText("");
                    } else {
                        this.post = post;
                    }
                } catch (Exception e2) {
                    posttext.setText("");
                }
                if (e.getActionCommand().equals("Login")) {
                    String user = logintext.getText();
                    String password = String.valueOf(passwdtext.getPassword());
                    int i = database.SqlJDBC("search", user, password);
                    if (i == 1 || i == 2) {
                        if (i == 1) {
                            new Thread(new ChatServer()).start();
                        } else {
                            new Thread(new ChatClient(post, user)).start();
                        }
                        this.setVisible(false);
                    } else {
                        if (!user.equals("admin") && post == 0)
                            postDialog.ShowMyDialog();
                        if (i == -1)
                            pwdDialog.ShowMyDialog();
                        if (i == -2)
                            userDialog.ShowMyDialog();
                    }
                }
            }
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            Document doc = e.getDocument();
            CosnelDocument(doc);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            Document doc = e.getDocument();
            CosnelDocument(doc);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            Document doc = e.getDocument();
            CosnelDocument(doc);
        }
    }

    @SuppressWarnings("serial")
    class Register extends JFrame implements ActionListener {

        JLabel loginlabel = new JLabel("用户名:");
        JLabel passwdlabel1 = new JLabel("密码:");
        JLabel passwdlabel2 = new JLabel("确认密码:");
        JLabel postlabel = new JLabel("登陆用端口号(6001-65535):");
        JLabel unused = new JLabel("             ");
        JTextField logintext = new JTextField(14);
        JPasswordField passwdtext = new JPasswordField(14);
        JPasswordField repasswdtext = new JPasswordField(14);
        JTextField posttext = new JTextField(5);
        JButton registerlogin = new JButton("Register");
        JButton reset = new JButton("ReSet");
        Panel textpanel = new Panel();
        Panel loginpanel = new Panel();
        int post;

        public Register(Login login) {
            super("聊天室注册窗口");
            MyWindosAdapter myWindos = new MyWindosAdapter(login);
            addWindowListener(myWindos);
            Container c = this.getContentPane();

            textpanel.setPreferredSize(new Dimension(320, 140));
            loginpanel.setPreferredSize(new Dimension(320, 65));

            loginlabel.setPreferredSize(new Dimension(85, 30));
            passwdlabel1.setPreferredSize(new Dimension(85, 30));
            passwdlabel2.setPreferredSize(new Dimension(85, 30));
            postlabel.setPreferredSize(new Dimension(190, 30));

            registerlogin.setPreferredSize(new Dimension(100, 25));
            reset.setPreferredSize(new Dimension(100, 25));

            c.setLayout(new FlowLayout());

            c.add(textpanel);
            c.add(loginpanel);
            textpanel.add(loginlabel);
            textpanel.add(logintext);
            textpanel.add(passwdlabel1);
            textpanel.add(passwdtext);
            textpanel.add(passwdlabel2);
            textpanel.add(repasswdtext);
            textpanel.add(postlabel);
            textpanel.add(posttext);
            loginpanel.add(registerlogin);
            loginpanel.add(unused);
            loginpanel.add(reset);

            registerlogin.addActionListener(this);
            reset.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("ReSet")) {
                logintext.setText("");
                passwdtext.setText("");
                repasswdtext.setText("");
                posttext.setText("");
            } else {
                try {
                    Integer post = Integer.parseInt(posttext.getText());
                    if (post < 6001 || post > 65535) {
                        posttext.setText("");
                    } else {
                        this.post = post;
                    }
                } catch (Exception e2) {
                    posttext.setText("");
                }
                if (e.getActionCommand().equals("Register")) {
                    String user = logintext.getText();
                    String password = String.valueOf(passwdtext.getPassword());
                    String repassword = String.valueOf(repasswdtext
                            .getPassword());
                    if (password.equals(repassword) && !password.equals("")) {
                        database.SqlJDBC("insert", user, password);
                        logintext.setText("");
                        passwdtext.setText("");
                        repasswdtext.setText("");
                        posttext.setText("");
                    } else {
                        pwdDialog.ShowMyDialog();
                        passwdtext.setText("");
                        repasswdtext.setText("");
                    }
                }
            }
        }

    }

    class JDBCManager {
        private Properties properties = new Properties();
        private String driverclass;
        private String jdbcUrl;
        private String user;
        private String password;

        public JDBCManager() {
            InputStream in = getClass().getClassLoader().getResourceAsStream(
                    "jdbc.properties");

            try {
                properties.load(in);
            } catch (IOException e) {
                e.printStackTrace();
            }

            driverclass = properties.getProperty("driver");
            jdbcUrl = properties.getProperty("jdbcUrl");
            user = properties.getProperty("user");
            password = properties.getProperty("password");

            try {
                Class.forName(driverclass);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }

        public Connection getConnection() {

            Connection connection = null;
            try {
                connection = DriverManager.getConnection(jdbcUrl, user,
                        password);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return connection;
        }

        public int SqlJDBC(String flag, String user, String password) {
            Connection connection = this.getConnection();
            ResultSet rs = null;
            String sql = null;
            Statement statement = null;

            try {
                statement = connection.createStatement();

                if (flag.equals("insert")) {
                    sql = "insert into user values ('" + user + "', '" + password + "')";
                    statement.executeUpdate(sql);
                    regDialog.ShowMyDialog();
                    return 0;
                }

                if (flag.equals("search")) {
                    sql = "select * from user where user='" + user + "'";
                    rs = statement.executeQuery(sql);
                    if (rs.next()) {
                        String pwd = rs.getString("password");
                        if (pwd.equals(password)) {
                            if (user.equals("admin")) {
                                return 1; // 登陆服务器
                            } else {
                                return 2; // 登陆客户端
                            }
                        } else {
                            return -1; // 密码不匹配
                        }
                    } else {
                        return -2; // 不存在此用户
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return -1;
        }
    }

    public void start() {
        Login app = new Login();
        app.setSize(320, 210);
        app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        app.setVisible(true);
        app.setResizable(false);
    }

    public static void main(String[] args) {
        new ChatLogin().start();
    }

}
