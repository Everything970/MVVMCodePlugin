package com.chenan.mvvm.ui;

import com.chenan.mvvm.setting.MVVMSetting;
import com.chenan.mvvm.util.Utils;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;

public class CreateCodeDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox<String> boxActivity;
    private JButton btActivity;
    private JComboBox<String> boxViewModel;
    private JButton btViewModel;
    private JButton btLayout;
    private JComboBox<String> boxLayout;

    private MVVMSetting config;
    private List<File> activities;
    private List<File> viewModels;
    private List<File> layouts;
    private OnClickListener listener;

    public CreateCodeDialog(Project project) {
        config = MVVMSetting.getInstance(project);
        activities = Utils.getActivityFiles();
        viewModels = Utils.getViewModelFiles();
        layouts = Utils.getLayoutFiles();
        int i;
        int indexActivity = 0;
        int indexViewModel = 0;
        int indexLayout = 0;
        for (i = 0; i < activities.size(); i++) {
            String a = activities.get(i).getName().replace(".txt", "");
            if (a.equals(config.getActivity())) {
                indexActivity = i;
            }
            boxActivity.addItem(a);
        }
        for (i = 0; i < viewModels.size(); i++) {
            String v = viewModels.get(i).getName().replace(".txt", "");
            if (v.equals(config.getViewModel())) {
                indexViewModel = i;
            }
            boxViewModel.addItem(v);
        }
        for (i = 0; i < layouts.size(); i++) {
            String v = layouts.get(i).getName().replace(".txt", "");
            if (v.equals(config.getLayout())) {
                indexLayout = i;
            }
            boxLayout.addItem(v);
        }
        boxActivity.setSelectedIndex(indexActivity);
        boxViewModel.setSelectedIndex(indexViewModel);
        boxLayout.setSelectedIndex(indexLayout);

        setTitle("MVVM");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        btActivity.addActionListener(e -> listener.onAddActivity());
        btViewModel.addActionListener(e -> listener.onAddViewModel());
        btLayout.addActionListener(e -> listener.onAddLayout());

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
        // add your code here
        int indexActivity = boxActivity.getSelectedIndex();
        int indexViewModel = boxViewModel.getSelectedIndex();
        int indexLayout = boxLayout.getSelectedIndex();
        System.out.println("index a:" + indexActivity + " v:" + indexViewModel);
        if (indexActivity < 0 && indexViewModel < 0 && indexLayout < 0) {
            Utils.showError("请选择模板代码");
            return;
        }
        if (listener != null) {
            listener.onOk(activities.get(indexActivity), viewModels.get(indexViewModel), layouts.get(indexLayout));
        }
        String strActivity = (String) boxActivity.getSelectedItem();
        String strViewModel = (String) boxViewModel.getSelectedItem();
        String strLayout = (String) boxLayout.getSelectedItem();
        System.out.println("string a:" + strActivity + " v:" + strViewModel + " l:" + strLayout);
        config.setActivity(strActivity == null ? Utils.defaultActivity : strActivity);
        config.setViewModel(strViewModel == null ? Utils.defaultViewModel : strViewModel);
        config.setLayout(strLayout == null ? Utils.defaultLayout : strLayout);
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        if (listener != null) listener.onCancel();
        dispose();
    }

    public void addActivity(File file) {
        activities.add(file);
        boxActivity.addItem(file.getName().replace(".txt", ""));
    }

    public void addViewModel(File file) {
        viewModels.add(file);
        boxViewModel.addItem(file.getName().replace(".txt", ""));
    }

    public void addLayout(File file) {
        layouts.add(file);
        boxLayout.addItem(file.getName().replace(".txt", ""));
    }

    public void setListener(OnClickListener listener) {
        this.listener = listener;
    }

    public void showDialog() {
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public interface OnClickListener {

        void onAddActivity();

        void onAddViewModel();

        void onAddLayout();

        void onOk(@NotNull File activityFile, @NotNull File viewModelFile, @NotNull File layoutFile);

        void onCancel();
    }
}
