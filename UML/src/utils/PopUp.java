package utils;

import javax.swing.JOptionPane;

public class PopUp {

    public static void error(Exception ex) {
        System.out.println("ERROR: " + ex.toString());
        JOptionPane.showMessageDialog(null, ex.toString(), "UML", JOptionPane.ERROR_MESSAGE);
    }

    public static void error(String msg) {
        System.out.println("ERROR: " + msg);
        JOptionPane.showMessageDialog(null, msg, "UML", JOptionPane.ERROR_MESSAGE);
    }

    private PopUp() {
    }
}
