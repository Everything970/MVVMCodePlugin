package pers.chenan.code.ui;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import pers.chenan.code.setting.PluginSetting;
import pers.chenan.code.util.Utils;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CreateCodeDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox<String> boxActivity;
    private JButton btAddActivity;
    private JComboBox<String> boxViewModel;
    private JButton btAddViewModel;
    private JButton btAddLayout;
    private JComboBox<String> boxLayout;
    private JButton btEditActivity;
    private JButton btDeleteActivity;
    private JButton btEditViewModel;
    private JButton btDeleteViewModel;
    private JButton btEditLayout;
    private JButton btDeleteLayout;

    private PluginSetting setting;
    private OnClickListener listener;

    public CreateCodeDialog(Project project) {
        setting = PluginSetting.getInstance(project);
        boxActivity.addItem(Utils.defaultActivity);
        boxViewModel.addItem(Utils.defaultViewModel);
        boxLayout.addItem(Utils.defaultLayout);
        for (String activity : setting.getMvvm().getActivityMap().keySet()) {
            boxActivity.addItem(activity);
        }
        for (String viewModel : setting.getMvvm().getViewModelMap().keySet()) {
            boxViewModel.addItem(viewModel);
        }
        for (String layout : setting.getMvvm().getLayoutMap().keySet()) {
            boxLayout.addItem(layout);
        }
        boxActivity.setSelectedItem(setting.getMvvm().getActivity());
        boxViewModel.setSelectedItem(setting.getMvvm().getViewModel());
        boxLayout.setSelectedItem(setting.getMvvm().getLayout());

        setTitle("MVVM");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        btEditActivity.addActionListener(e -> setting.getMvvm().editTemplateCode(boxActivity, 0));
        btAddActivity.addActionListener(e -> setting.getMvvm().addTemplateCode(boxActivity, 0));
        btDeleteActivity.addActionListener(e -> setting.getMvvm().deleteTemplateCode(boxActivity, 0));
        btEditViewModel.addActionListener(e -> setting.getMvvm().editTemplateCode(boxViewModel, 1));
        btAddViewModel.addActionListener(e -> setting.getMvvm().addTemplateCode(boxViewModel, 1));
        btDeleteViewModel.addActionListener(e -> setting.getMvvm().deleteTemplateCode(boxViewModel, 1));
        btEditLayout.addActionListener(e -> setting.getMvvm().editTemplateCode(boxLayout, 2));
        btAddLayout.addActionListener(e -> setting.getMvvm().addTemplateCode(boxLayout, 2));
        btDeleteLayout.addActionListener(e -> setting.getMvvm().deleteTemplateCode(boxLayout, 2));

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
        Object activity = boxActivity.getSelectedItem();
        Object viewModel = boxViewModel.getSelectedItem();
        Object layout = boxLayout.getSelectedItem();
        if (activity == null || viewModel == null || layout == null) {
            Utils.showError("请选择模板代码");
            return;
        }
        if (listener != null) {
            listener.onOk(activity.toString(), viewModel.toString(), layout.toString());
        }
        String strActivity = (String) boxActivity.getSelectedItem();
        String strViewModel = (String) boxViewModel.getSelectedItem();
        String strLayout = (String) boxLayout.getSelectedItem();
        setting.getMvvm().setActivity(strActivity == null ? Utils.defaultActivity : strActivity);
        setting.getMvvm().setViewModel(strViewModel == null ? Utils.defaultViewModel : strViewModel);
        setting.getMvvm().setLayout(strLayout == null ? Utils.defaultLayout : strLayout);
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

    public interface OnClickListener {

        void onOk(@NotNull String activity, @NotNull String viewModel, @NotNull String layout);

        void onCancel();
    }
}
