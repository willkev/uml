/*
 * @copyright Copyright (c) 2014 Animati Sistemas de Informática Ltda.
 * (http://www.animati.com.br)
 */
package ui;

import com.sun.javafx.application.PlatformImpl;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import static ui.Starter.URL;

public class Browser extends JFrame {

    private JFXPanel fxPanel;
    private WebEngine engine;

    public Browser() {
        super();
        Platform.setImplicitExit(false);

        fxPanel = new JFXPanel();
        fxPanel.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
            }
        });
        getContentPane().add(fxPanel);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
            }
        });
        createWebPanel();
        setExtendedState(JFrame.NORMAL);
        setBounds(50, 50, 1000, 800);
        setVisible(true);
    }

    private void createWebPanel() {
        PlatformImpl.runLater(new Runnable() {
            @Override
            public void run() {
                WebView browser = new WebView();
                engine = browser.getEngine();
                engine.load(URL);
                StackPane sp = new StackPane();
                sp.getChildren().add(browser);
                fxPanel.setScene(new Scene(sp));
            }
        });
    }

    public void addUMLtext(String umlText) {
        //document.querySelectorAll('textarea')[0].innerHTML="<textarea name='text' cols='120' rows='10'>oioioioi sasasasa</textarea>"
        String html = "document.querySelectorAll('textarea')[0].value=\"" + umlText + "\";";
        execJavaScript(html);
    }

    private void execJavaScript(String function) {
        PlatformImpl.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    engine.executeScript(function);
                } catch (Exception ex) {
                    System.out.println("Erro ao chamar função javascript: \"" + function + "\"");
                    ex.printStackTrace();
                }
            }
        });
    }
}
