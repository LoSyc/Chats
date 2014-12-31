package org.jluzhjsj.maksyc.javadesign;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ChatClient implements Runnable {

    @SuppressWarnings("serial")
    class ChatClientGui extends JFrame implements ActionListener {

        private int clientpost;

        boolean flag = true;
        DatagramPacket packet;
        DatagramSocket ds;
        FileInputStream fis;
        DataInputStream dis;
        DataOutputStream dos;

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss yyyy-MM-dd :");
        JTextArea view = new JTextArea(23, 46);
        JScrollPane viewtextpane = new JScrollPane(view);
        JTextArea edit = new JTextArea(8, 46);
        JScrollPane edittextpane = new JScrollPane(edit);
        JLabel label = new JLabel("jluzh.cst.java.design.work");
        JButton send = new JButton("Send");
        JButton sendfile = new JButton("发送文件");
        JFileChooser chooser = new JFileChooser();

        public ChatClientGui(int clientpost) {
            super("ChatClient");
            addWindowListener(new MyWindosAdapter());
            this.clientpost = clientpost;
            Container c = this.getContentPane();
            c.setLayout(new FlowLayout());
            view.setText("Hello Client!");
            view.setLineWrap(true);
            view.setEditable(false);
            edit.setLineWrap(true);
            edittextpane.setWheelScrollingEnabled(true);
            view.setBackground(new Color(225, 225, 225));
            edit.setBackground(new Color(215, 215, 215));
            label.setPreferredSize(new Dimension(340, 30));

            c.add(viewtextpane);
            c.add(edittextpane);
            c.add(label);
            c.add(sendfile);
            c.add(send);
            send.addActionListener(this);
            sendfile.addActionListener(this);

        }

        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("Send")) {
                try {
                    String data = sdf.format(new Date());
                    String str = name + "      " + data + '\n' + edit.getText() + '\n';
                    byte[] datas = str.getBytes();
                    packet = new DatagramPacket(datas, datas.length,
                            new InetSocketAddress("127.0.0.1", 6000));
                    ds.send(packet);
                } catch (SocketException e2) {
                    e2.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                edit.setText("");
            }
            if (e.getActionCommand().equals("发送文件")) {
                chooser.setMultiSelectionEnabled(true);
                chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                chooser.setCurrentDirectory(new File("."));
                int result = chooser.showOpenDialog(this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String name = chooser.getSelectedFile().getPath();
                    System.out.println("文件路径：" + name);
                    File file = new File(name);
                    new Thread(new Uploadfile(file)).start();
                }
            }

        }

        class MyWindosAdapter extends WindowAdapter {

            public void windowOpened(WindowEvent e) {
                try {
                    ds = new DatagramSocket(clientpost);
                    byte[] datas = "link--91n".getBytes();
                    packet = new DatagramPacket(datas, datas.length,
                            new InetSocketAddress("127.0.0.1", 6000));
                    ds.send(packet);
                    new Thread(new Client()).start();
                } catch (SocketException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            public void windowClosing(WindowEvent e) {
                try {
                    byte[] datas = "exit--91n".getBytes();
                    packet = new DatagramPacket(datas, datas.length,
                            new InetSocketAddress("127.0.0.1", 6000));
                    ds.send(packet);
                    flag = false;
                    dos.close();
                } catch (SocketException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                } finally {
                    try {
                        dis.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } finally {
                        ds.close();
                        System.exit(0);
                    }
                }
            }
        }

        class Uploadfile implements Runnable {

            private File file;
            private Socket socket = null;

            public Uploadfile(File file) {
                this.file = file;
                try {
                    socket = new Socket("127.0.0.1", 6000);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            public void run() {
                try {
                    fis = new FileInputStream(file);
                    dis = new DataInputStream(fis);
                    dos = new DataOutputStream(socket.getOutputStream());
                    dos.writeUTF(file.getName());
                    dos.flush();
                    dos.writeLong((long) file.length());
                    dos.flush();
                    byte[] buf = new byte[1024];
                    while (true) {
                        int read = 0;
                        if (dis != null) {
                            read = dis.read(buf);
                        }
                        if (read == -1) {
                            break;
                        }
                        dos.write(buf, 0, read);
                    }
                    dos.flush();
                    System.out.println("文件发送完成！");
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                } finally {
                    try {
                        dis.close();
                        dos.close();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        class Client implements Runnable {

            public void run() {
                while (flag) {
                    try {
                        byte[] datas = new byte[1024];
                        packet = new DatagramPacket(datas, datas.length);
                        ds.receive(packet);
                        int len = packet.getLength();
                        String str = new String(packet.getData(), 0, len);
                        view.setText(view.getText() + "\n" + str);
                    } catch (IOException e) {
                        System.out.println("程序退出！");
                    }
                }
            }
        }
    }

    private int post;
    private String name;

    public ChatClient(int post, String name) {
        this.post = post;
        this.name = name;
    }

    @Override
    public void run() {
        ChatClientGui app = new ChatClientGui(post);
        app.setSize(525, 580);
        app.setVisible(true);
        app.setResizable(false);
    }
}
