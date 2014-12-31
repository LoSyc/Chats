package org.jluzhjsj.maksyc.javadesign;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ChatServer implements Runnable {

    List<InetSocketAddress> socketaddress = new ArrayList<InetSocketAddress>();
    String str;
    DatagramSocket s;
    ServerSocket ss;
    Socket socket;

    class Client implements Runnable {

        public void DataOutput(InetSocketAddress socketadd) {
            try {
                System.out.println("send:" + str);
                byte[] datas = str.getBytes();
                DatagramPacket packet = new DatagramPacket(datas, datas.length,
                        socketadd);
                s.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            System.out.println("启动线程客户端数：" + socketaddress.size());
            for (int i = 0; i < socketaddress.size(); i++) {
                DataOutput(socketaddress.get(i));
            }
        }
    }

    @SuppressWarnings("serial")
    class ChatServerGui extends JFrame {

        JTextArea view = new JTextArea(34, 24);
        JScrollPane viewtextpane = new JScrollPane(view);

        public ChatServerGui() {
            super("ChatServer");

            Container c = this.getContentPane();
            c.setLayout(new FlowLayout());
            view.setText("jluzh.cst.java.design.work!" + "\n"
                    + "Service opens successfully!");
            view.setLineWrap(true);
            view.setEditable(false);
            view.setBackground(new Color(225, 225, 225));
            c.add(viewtextpane);

        }
    }

    class ReceiveReady implements Runnable {

        private Socket socket;
        private ServerSocket ss;

        public ReceiveReady(ServerSocket ss) {
            this.ss = ss;
        }

        public void run() {
            while (true) {
                try {
                    socket = ss.accept();
                    new Thread(new Receivefile(socket)).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class Receivefile implements Runnable {

        @SuppressWarnings("unused")
        private Socket socket;
        private DataInputStream dis;
        private DataOutputStream dos;

        public Receivefile(Socket socket) {
            this.socket = socket;
            try {
                dis = new DataInputStream(new BufferedInputStream(
                        socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                String file = dis.readUTF();
                dos = new DataOutputStream(new FileOutputStream(new File(file)));
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
                dos.close();
                System.out.println("文件接收完成！");
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void run() {
        ChatServerGui app = new ChatServerGui();
        app.setSize(280, 585);
        app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        app.setVisible(true);
        app.setResizable(false);
        Integer post;
        InetAddress add;

        try {
            ss = new ServerSocket(6000);
            s = new DatagramSocket(6000);
        } catch (SocketException e2) {
            e2.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(new ReceiveReady(ss)).start();

        while (true) {
            try {
                byte[] datas = new byte[1024];
                DatagramPacket packet = new DatagramPacket(datas, datas.length);
                s.receive(packet);
                add = packet.getAddress();
                System.out.println("接收地址：" + add.getHostAddress());

                post = packet.getPort();
                System.out.println("接收端口：" + post);

                int len = packet.getLength();
                str = new String(packet.getData(), 0, len);
                System.out.println("receive:" + str);
                InetSocketAddress sockadd = new InetSocketAddress(add, post);

                if (str.equals("link--91n")) {
                    socketaddress.add(sockadd);
                    System.out.println("初始化客户端成功！");
                    continue;
                }
                if (str.equals("exit--91n")) {
                    for (int i = 0; i < socketaddress.size(); i++) {
                        if (socketaddress.get(i).equals(sockadd)) {
                            socketaddress.remove(i);
                            break;
                        }
                    }
                    continue;
                }
                new Thread(new Client()).start();
                System.out.println("线程启动成功");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
