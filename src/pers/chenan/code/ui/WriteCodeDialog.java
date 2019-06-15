package pers.chenan.code.ui;

import pers.chenan.code.util.Utils;
import com.intellij.openapi.ui.DialogBuilder;
import org.jetbrains.annotations.NotNull;
import pers.chenan.code.util.Utils;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;

public class WriteCodeDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textFieldName;
    private JTextArea textAreaCode;
    private JTextPane TextPane;
    private OnClickListener listener;

    public WriteCodeDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        String name = textFieldName.getText();
        String content = textAreaCode.getText();
        if (name == null || name.equals("")) {
            Utils.showError("请输入模板名称");
            return;
        }
        if (content == null || content.equals("")) {
            Utils.showError("请输入模板内容");
            return;
        }
        listener.onOk(name, content);
    }

    private void onCancel() {
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

    public void showDialog(String name, String content) {
        textFieldName.setText(name);
        textAreaCode.setText(content);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public interface OnClickListener {
        void onOk(@NotNull String name, @NotNull String content);
    }
}
