package ui;

import com.thoughtworks.qdox.model.JavaClass;
import core.Uml;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.net.URI;
import javax.management.BadAttributeValueExpException;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import utils.CtrlC;
import utils.PopUp;

public class Starter extends JFrame {

    private static final String GIT_URL_MASTER = "http://git.animati.com.br/root/workstation/blob/master/";
    public static final String PLANTUML_URL = "http://www.plantuml.com/plantuml/form";
    private static final String STATUS_OK = "Copied to clipboard!";
    private static final int BTN_FIND_ALL = 0;
    private static final int BTN_FIND_RECURSIVE = 1;
    private static final int BTN_FIND_DOWN = 2;
    private static final int BTN_FIND_UP = 3;
    private static final int BTN_GIT_URL = 4;
    private static final int BTN_OPEN_PLANTUML = 5;

    //public static final Browser browser = new Browser();
    public static void main(String[] args) {
        Starter starter = new Starter();
    }

    private Uml uml;
    private final JTextField find = new JTextField(25);
    private final JLabel statusBar = new JLabel();
    private final JCheckBox useTestClasses = new JCheckBox("Use Test Classes");

    public Starter() {
        super("UML");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(getMainPanel());
        setResizable(false);
        setSize(300, 250);
        try {
            int x = Toolkit.getDefaultToolkit().getScreenSize().width - getWidth();
            setLocation(x, 5);
            setVisible(true);
            // load all project
            uml = new Uml();
        } catch (Exception ex) {
            PopUp.error(ex);
        }
    }

    private JPanel getMainPanel() {
        JPanel pActions = new JPanel(new GridLayout(0, 2));
        pActions.add(createButton("UML - All", BTN_FIND_ALL));
        pActions.add(createButton("UML - All recursive", BTN_FIND_RECURSIVE));
        pActions.add(createButton("UML - Down", BTN_FIND_DOWN));
        pActions.add(createButton("UML - Up", BTN_FIND_UP));
        pActions.add(createButton("Git URL", BTN_GIT_URL));
        pActions.add(createButton("Open Plantuml", BTN_OPEN_PLANTUML));
        useTestClasses.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof JCheckBox) {
                    uml.useTestClasses = ((JCheckBox) e.getSource()).isSelected();
                }
            }
        });
        pActions.add(useTestClasses);

        JPanel pInfo = new JPanel();
        pInfo.add(new JLabel("Digite apenas o nome da classe"));
        JPanel pStatus = new JPanel();
        pStatus.add(statusBar);
        JPanel p = new JPanel();
        p.add(pInfo);
        p.add(find);
        p.add(pActions);
        p.add(pStatus);
        return p;
    }

    private JButton createButton(String textAndName, int buttonID) {
        JButton btn = new JButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                execButton(buttonID);
            }
        });
        btn.setText(textAndName);
        btn.setName(textAndName);
        return btn;
    }

    private void execButton(int buttonID) {
        statusClear();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (buttonID == BTN_OPEN_PLANTUML) {
                    openURL(PLANTUML_URL);
                } else {
                    try {
                        JavaClass java = uml.get(find.getText());
                        switch (buttonID) {
                            case BTN_FIND_RECURSIVE:
                                uml.findAllRecursive(java);
                                break;
                            case BTN_FIND_ALL:
                                uml.findAll(java);
                                break;
                            case BTN_FIND_DOWN:
                                uml.findDown(java);
                                break;
                            case BTN_FIND_UP:
                                uml.findUp(java);
                                break;
                            case BTN_GIT_URL:
                                String gitURL = gitURL(java);
                                openURL(gitURL);
                        }
                        statusOk();
                    } catch (BadAttributeValueExpException ex) {
                        PopUp.error(ex);
                    }
                }
            }
        });
    }

    private void openURL(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            PopUp.error("Função não suportada pelo Sistema Operacional, abra manualmente:\n" + PLANTUML_URL);
        }
    }

    private String gitURL(JavaClass j) {
        String path = j.getSource().getURL().getPath();
        int x = path.indexOf("workstation");
        String urlgit = GIT_URL_MASTER + path.substring(x + 12);
        CtrlC.copy(urlgit);
        return urlgit;
    }

    private void statusClear() {
        statusBar.setText("loading...");
        statusBar.validate();
    }

    private void statusOk() {
        statusBar.setText(STATUS_OK);
        statusBar.setForeground(Color.blue);
        statusBar.validate();
    }
}
