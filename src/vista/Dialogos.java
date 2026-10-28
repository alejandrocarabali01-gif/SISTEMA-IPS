package vista;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import javax.swing.JOptionPane;

/** Mensajes claros para el usuario. */
public final class Dialogos {
    private Dialogos() {
    }

    public static void aviso(Component padre, String mensaje) {
        mostrar(padre, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    public static void error(Component padre, String mensaje) {
        mostrar(padre, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component padre, String mensaje) {
        mostrar(padre, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirmar(Component padre, String mensaje) {
        if (GraphicsEnvironment.isHeadless()) {
            return true;
        }
        return JOptionPane.showConfirmDialog(padre, mensaje, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private static void mostrar(Component padre, String mensaje, String titulo, int tipo) {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println(titulo + ": " + mensaje);
            return;
        }
        JOptionPane.showMessageDialog(padre, mensaje, titulo, tipo);
    }
}
