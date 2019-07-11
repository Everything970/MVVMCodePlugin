package pers.chenan.code.ui;

import org.jetbrains.annotations.NotNull;
import pers.chenan.code.util.Utils;

import javax.swing.*;
import java.awt.event.*;

public class CreateBeanDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textFieldName;
    private JTextArea textAreaJson;
    private JComboBox<String> algorithm;
    private OnClickListener listener = null;

    public CreateBeanDialog() {
        algorithm.addItem(Utils.Gson);
        algorithm.addItem(Utils.fastJson);
        algorithm.setSelectedIndex(0);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        // add your code here
        String name = textFieldName.getText();
        String json = textAreaJson.getText();
        if (name == null || name.equals("")) {
            Utils.showError("请输入类名");
            return;
        }
        if (json == null || json.equals("")) {
            Utils.showError("请输入json字符串");
            return;
        }
        if (listener != null) listener.onOK(name, json,algorithm.getSelectedIndex());
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        if (listener != null) listener.onCancel();
        dispose();
    }

    public void setListener(OnClickListener listener) {
        this.listener = listener;
    }

    public void showDialog() {
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void showDialog(@NotNull String name) {
        textFieldName.setText(name);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public interface OnClickListener {
        void onOK(@NotNull String beanName, @NotNull String json,int al);

        void onCancel();
    }
}
