package p2p;

import javax.swing.SwingUtilities;

public class Main {
        public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Gui().setVisible(true);
        });
    }
}
