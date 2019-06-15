package com.chenan.mvvm.ui;

import com.chenan.mvvm.util.FunCodeHelper;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.*;

public class CreateURLDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textFieldURL;
    private JTextField textFieldFunName;
    private JTextArea textAreaRequestBean;
    private JTextArea textAreaResultBean;
    private JComboBox<String> comboBoxRequest;
    private JComboBox<String> comboBoxResult;
    private JTextField textFieldRequestName;
    private JTextField textFieldResultName;
    private JScrollPane scrollPaneRequest;
    private JScrollPane scrollPaneResult;
    private JTextArea textAreaRequest;
    private JTextArea textAreaResult;
    private JTextArea textAreaFunCode;
    private JPanel panelRequestBean;
    private JPanel panelResultBean;
    private JLabel labelRequestName;
    private JLabel labelResultName;
    private FunCodeHelper codeHelper;
    private OnClickListener listener;

    public CreateURLDialog(Project project) {
        setTitle("新建接口方法（Beta）");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        codeHelper = new FunCodeHelper(project);

        comboBoxRequest.addActionListener(e -> {
            String item = (String) comboBoxRequest.getSelectedItem();
            if (item == null) {
                scrollPaneRequest.setVisible(false);
                panelRequestBean.setVisible(false);
                labelRequestName.setVisible(false);
                textFieldRequestName.setVisible(false);
            } else if (item.equals("Bean")) {
                scrollPaneRequest.setVisible(true);
                panelRequestBean.setVisible(true);
                labelRequestName.setVisible(true);
                textFieldRequestName.setVisible(true);
            } else if (item.equals("Body") || item.equals("Field")) {
                scrollPaneRequest.setVisible(true);
                panelRequestBean.setVisible(false);
                labelRequestName.setVisible(false);
                textFieldRequestName.setVisible(false);
            } else {
                scrollPaneRequest.setVisible(false);
                panelRequestBean.setVisible(false);
                labelRequestName.setVisible(false);
                textFieldRequestName.setVisible(false);
            }
        });

        comboBoxResult.addActionListener(e -> {
            String item = (String) comboBoxResult.getSelectedItem();
            if (item == null) {
                scrollPaneResult.setVisible(false);
                panelResultBean.setVisible(false);
                labelResultName.setVisible(false);
                textFieldResultName.setVisible(false);
            } else if (item.equals("Bean")) {
                scrollPaneResult.setVisible(true);
                panelResultBean.setVisible(true);
                labelResultName.setVisible(true);
                textFieldResultName.setVisible(true);
            } else if (item.equals("Map")) {
                scrollPaneResult.setVisible(true);
                panelResultBean.setVisible(false);
                labelResultName.setVisible(false);
                textFieldResultName.setVisible(false);
            } else {
                scrollPaneResult.setVisible(false);
                panelResultBean.setVisible(false);
                labelResultName.setVisible(false);
                textFieldResultName.setVisible(false);
            }
        });

        for (String request : codeHelper.getListRequest()) {
            comboBoxRequest.addItem(request);
        }

        for (String request:codeHelper.getListBean()){
            comboBoxRequest.addItem(request);
        }

        for (String result : codeHelper.getListResult()) {
            comboBoxResult.addItem(result);
        }

        for (String result:codeHelper.getListBean()){
            comboBoxResult.addItem(result);
        }


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
        if (codeHelper.check()) {
            if (listener != null) listener.onOK(codeHelper);
            dispose();
        }
    }

    private void onCancel() {
        if (listener != null) listener.onCancel();
        dispose();
    }

    public void setOnClickListener(OnClickListener listener) {
        this.listener = listener;
    }

    public void showDialog() {
        codeHelper.bindTextArea(textAreaRequest, textAreaResult,
                textAreaFunCode, textAreaRequestBean, textAreaResultBean);
        codeHelper.setURLDocumentListener(textFieldURL, textFieldFunName, textFieldRequestName, textFieldResultName);
        codeHelper.setTypeListener(comboBoxRequest, comboBoxResult);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public interface OnClickListener {
        void onOK(@NotNull FunCodeHelper helper);

        void onCancel();
    }
}
