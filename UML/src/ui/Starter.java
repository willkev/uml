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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import utils.CtrlC;
import utils.PopUp;

public class Starter extends JFrame {

    public static final String URL = "http://www.plantuml.com/plantuml/form";
    //public static final Browser browser = new Browser();

    public static void main(String[] args) {
        new Starter();
    }
    
    private static final String STATUS_OK = "Copied to clipboard!";
    private Uml uml;
    private JTextField find = new JTextField(25);
    private JLabel statusBar = new JLabel();

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
    // uml.useListIgnore=true;  CheckBox para este!

    private JPanel getMainPanel() {
        JPanel pActions = new JPanel(new GridLayout(0, 2));
        pActions.add(buttonFindAll("UML - All"));
        pActions.add(buttonFindAllRecursive("UML - All recursive"));
        pActions.add(buttonFindDown("UML - Down"));
        pActions.add(buttonFindUp("UML - Up"));
        pActions.add(buttonGit("Git URL"));
        pActions.add(buttonOpenURL("Open Plantuml"));

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

    private JButton buttonFindAllRecursive(String str) {
        JButton btn = new JButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    statusClear();
                    JavaClass java = uml.get(find.getText());
                    uml.findAllRecursive(java);
                    statusOk();
                } catch (BadAttributeValueExpException ex) {
                    PopUp.error(ex);
                }
            }
        });
        btn.setText(str);
        btn.setName(str);
        return btn;
    }

    private JButton buttonFindAll(String str) {
        JButton btn = new JButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    statusClear();
                    JavaClass java = uml.get(find.getText());
                    uml.findAll(java);
                    statusOk();
                } catch (BadAttributeValueExpException ex) {
                    PopUp.error(ex);
                }
            }
        });
        btn.setText(str);
        btn.setName(str);
        return btn;
    }

    private JButton buttonFindDown(String str) {
        JButton btn = new JButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    statusClear();
                    JavaClass java = uml.get(find.getText());
                    uml.findDown(java);
                    statusOk();
                } catch (BadAttributeValueExpException ex) {
                    PopUp.error(ex);
                }
            }
        });
        btn.setText(str);
        btn.setName(str);
        return btn;
    }

    private JButton buttonFindUp(String str) {
        JButton btn = new JButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    statusClear();
                    JavaClass java = uml.get(find.getText());
                    uml.findUp(java);
                    statusOk();
                } catch (BadAttributeValueExpException ex) {
                    PopUp.error(ex);
                }
            }
        });
        btn.setText(str);
        btn.setName(str);
        return btn;
    }

    private JButton buttonGit(String str) {
        JButton btn = new JButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    statusClear();
                    gitURL(uml.get(find.getText()));
                    statusOk();
                } catch (BadAttributeValueExpException ex) {
                    PopUp.error(ex);
                }
            }
        });
        btn.setText(str);
        btn.setName(str);
        return btn;
    }

    private JButton buttonOpenURL(String str) {
        JButton btn = new JButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(URL));
                } catch (Exception ex) {
                    PopUp.error("Função não suportada pelo Sistema Operacional, abra manualmente:\n" + URL);
                }
            }
        });
        btn.setText(str);
        btn.setName(str);
        return btn;
    }

    private void gitURL(JavaClass j) {
        String git = "https://git.animati.com.br/root/workstation/blob/master/";
        String path = j.getSource().getURL().getPath();
        int x = path.indexOf("workstation");
        CtrlC.copy(git + path.substring(x + 12));
    }

    private void statusClear() {
        statusBar.setText("");
        statusBar.validate();
    }

    private void statusOk() {
        statusBar.setText(STATUS_OK);
        statusBar.setForeground(Color.blue);
        statusBar.validate();
    }
}
