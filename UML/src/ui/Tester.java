package ui;

import com.thoughtworks.qdox.model.JavaClass;
import core.Uml;
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

public class Tester extends JFrame {

    public static void main(String[] args) {
        new Tester();
    }
    private static final String STATUS_OK = "Copied to clipboard!";
    private Uml uml;
    private JTextField find = new JTextField(25);
    private JLabel statusBar = new JLabel();

    public Tester() {
        super("UML");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(getMainPanel());
        setResizable(false);
        setSize(300, 170);
        try {
            int x = Toolkit.getDefaultToolkit().getScreenSize().width - getWidth();
            setLocation(x, 0);
            setVisible(true);
            // load all project
            uml = new Uml();
        } catch (Exception ex) {
            PopUp.error(ex);
        }
    }
//  uml.useListIgnore=true;  CheckBox para este!
//  para as buscas um botão

    private JPanel getMainPanel() {
        JPanel p = new JPanel();
        p.add(find);
        JPanel pActions = new JPanel(new GridLayout(0, 2));
        pActions.add(buttonFindAll("UML - All"));
        pActions.add(buttonFindAllRecursive("UML - All recursive"));
        pActions.add(new JButton("UML - Up"));
        pActions.add(new JButton("UML - Down"));
        pActions.add(buttonGit("Git URL"));
        pActions.add(buttonOpenURL("Open Plantuml"));
        pActions.add(statusBar);
        p.add(pActions);
        return p;
    }

    private JButton buttonOpenURL(String str) {
        JButton btn = new JButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String url = "http://www.plantuml.com/plantuml/form";
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception ex) {
                    PopUp.error("Função não suportada pelo Sistema Operacional, abra manualmente:\n" + url);
                }
            }
        });
        btn.setText(str);
        btn.setName(str);
        return btn;
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
    }
}
