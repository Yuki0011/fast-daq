package com.mhaq.collect.common;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

import java.awt.*;

import java.awt.event.*;

public class DesktopApp extends JFrame {

    private SystemTray tray;

    private TrayIcon trayIcon;

    private JPanel jp;

    private JTextArea jta;

    private JScrollPane jsp;


    public DesktopApp() {



        // 设置窗口的初始大小和位置

        setSize(400, 300);

        jp = new JPanel();
        jta = new JTextArea("日志打印..."+"\n",18,50);
        jta.setLineWrap(true);
        jta.setForeground(Color.BLACK);

        jsp = new JScrollPane(jta);
        Dimension size = jta.getPreferredSize();
        jsp.setBounds(110,90,size.width,size.height);


        jp.add(jsp);
        add(jp);

        setLocation(100,100);
        setSize(600,400);

        DefaultStyledDocument document = new DefaultStyledDocument();
        jta.setDocument(document);
        document.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkTestLimit(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {

            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });

        // 添加窗口监听器
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                minimizeToTray(); // 最小化到托盘
            }
        });


        // 添加窗口状态监听器

        addWindowStateListener(new WindowStateListener() {

            @Override

            public void windowStateChanged(WindowEvent e) {

                if (e.getNewState() == JFrame.ICONIFIED) {

                    minimizeToTray(); // 最小化到托盘

                } else if (e.getNewState() == JFrame.NORMAL) {

                    restoreFromTray(); // 从托盘还原

                }

            }

        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension size = getSize();
                jta.setSize(size.width,size.height);
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int option = JOptionPane.showConfirmDialog(DesktopApp.this,"确定要关闭程序吗？","确认关闭",JOptionPane.YES_NO_OPTION);
                if(option==JOptionPane.YES_NO_OPTION){
                    System.exit(0);
                }
            }
        });




        setLocationRelativeTo(null);

        LocalCacheUtil.txt = jta;

    }

    private void checkTestLimit(DocumentEvent e){
        int maxLines = 100;
        DefaultStyledDocument document = (DefaultStyledDocument) e.getDocument();
        int lines = document.getDefaultRootElement().getElementCount();
        if(lines>maxLines){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int start = 0;
                        int end = document.getDefaultRootElement().getElement(0).getEndOffset();
                        document.remove(start,end);
                    }catch (BadLocationException ex){
                        ex.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private void minimizeToTray() {
        if (SystemTray.isSupported()) {
            setVisible(false); // 隐藏窗口
            if (tray == null) {
                createTrayIcon(); // 创建系统托盘图标
            }
            try {
                tray.add(trayIcon);
            } catch (AWTException ex) {
                ex.printStackTrace();
            }
        } else {
            System.out.println("退出");
            System.exit(0);
        }
    }


    private void restoreFromTray() {
        if (SystemTray.isSupported()) {
            setVisible(true); // 显示窗口
            setExtendedState(JFrame.NORMAL); // 还原窗口大小

            if (tray != null) {

                tray.remove(trayIcon);

            }

        }

    }


    private void createTrayIcon() {

        if (SystemTray.isSupported()) {

            tray = SystemTray.getSystemTray();


            // 创建托盘图标的点击事件监听器

            trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage("icon.png"));

            trayIcon.addActionListener(e -> restoreFromTray());

        }

    }




}
