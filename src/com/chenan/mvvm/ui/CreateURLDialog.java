package com.chenan.mvvm.ui;

import com.chenan.mvvm.util.BeanCodeHelper;
import com.chenan.mvvm.util.FunCodeHelper;
import com.chenan.mvvm.util.Utils;
import com.intellij.openapi.project.Project;
import org.apache.http.util.TextUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.*;
import java.util.Objects;

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
    private JScrollPane jScrollPaneRequest;
    private JScrollPane jScrollPaneResult;
    private JTextArea textAreaRequest;
    private JTextArea textAreaResult;
    private JTextArea textAreaFunCode;
    private JPanel jPanelRequestBean;
    private JPanel jPanelResultBean;
    private FunCodeHelper codeHelper;

    public CreateURLDialog(Project project) {
        setTitle("新建接口方法（Beta）");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        codeHelper = new FunCodeHelper(project);

        comboBoxRequest.addActionListener(e -> {
            String item = (String) comboBoxRequest.getSelectedItem();
            if (item == null) {
                jScrollPaneRequest.setVisible(false);
                jPanelRequestBean.setVisible(false);
                textFieldRequestName.setEditable(false);
            } else if (item.equals("Bean")) {
                jScrollPaneRequest.setVisible(true);
                jPanelRequestBean.setVisible(true);
                textFieldRequestName.setEditable(true);
            } else if (item.equals("Body") || item.equals("Field")) {
                jScrollPaneRequest.setVisible(true);
                jPanelRequestBean.setVisible(false);
                textFieldRequestName.setEditable(false);
            } else {
                jScrollPaneRequest.setVisible(false);
                jPanelRequestBean.setVisible(false);
                textFieldRequestName.setEditable(false);
            }
        });

        comboBoxResult.addActionListener(e -> {
            String item = (String) comboBoxResult.getSelectedItem();
            if (item == null) {
                jScrollPaneResult.setVisible(false);
                jPanelResultBean.setVisible(false);
                textFieldResultName.setEditable(false);
            } else if (item.equals("Bean")) {
                jScrollPaneResult.setVisible(true);
                jPanelResultBean.setVisible(true);
                textFieldResultName.setEditable(true);
            } else if (item.equals("Map")) {
                jScrollPaneResult.setVisible(true);
                jPanelResultBean.setVisible(false);
                textFieldResultName.setEditable(false);
            } else {
                jScrollPaneResult.setVisible(false);
                jPanelResultBean.setVisible(false);
                textFieldResultName.setEditable(false);
            }
        });

        for (String request : codeHelper.getListRequest()) {
            comboBoxRequest.addItem(request);
        }
        for (String result : codeHelper.getListResult()) {
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
        // add your code here
        if (TextUtils.isEmpty(textFieldURL.getText())) {
            Utils.showError("请输入Request URL");
            return;
        }
        String name = textFieldFunName.getText();
        if (TextUtils.isEmpty(name)) {
            Utils.showError("请输入接口名");
            return;
        }
        String requestType = Objects.requireNonNull(comboBoxRequest.getSelectedItem()).toString();
        String requestName = textFieldRequestName.getText();

        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public void showDialog() {
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        System.out.println("jTextField:" + textFieldURL + " " + textFieldURL == null);
        codeHelper.setURLDocumentListener(textFieldURL);
        codeHelper.setRequestTypeListener(comboBoxRequest);
        codeHelper.setResultTypeListener(comboBoxResult);
        codeHelper.setRequestDocumentListener(textAreaRequest);
        codeHelper.setRequestDocumentListener(textAreaResult);

        codeHelper.bindTextArea(textAreaFunCode, textAreaRequestBean, textAreaResultBean);
        codeHelper.bindTextField(textFieldFunName, textFieldRequestName, textFieldResultName);

    }
}
