package principal;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import vista.VentanaPrincipal;

/** Punto de entrada de la aplicación. */
public class Main {
    public static void main(String[] args) {
        System.setProperty("swing.boldMetal", "false");
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e) {
                // se usa el aspecto por defecto
            }
            new VentanaPrincipal().setVisible(true);
        });
    }
}
