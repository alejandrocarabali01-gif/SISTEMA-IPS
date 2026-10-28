package vista;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import modelo.EstadoCita;

/** Etiqueta redondeada: insignia verde de cada pantalla y estado de la cita. */
public class Chip extends JLabel {
    private Color fondo = Tema.VERDE_CLARO;
    private final boolean punto;

    public Chip(boolean punto) {
        this.punto = punto;
        setOpaque(false);
        setFont(Tema.f(Font.BOLD, 11));
        setForeground(Tema.VERDE_OSCURO);
        setBorder(new EmptyBorder(3, punto ? 22 : 12, 3, 12));
    }

    public static Chip insignia(String texto) {
        Chip c = new Chip(false);
        c.setText(texto);
        return c;
    }

    /** Recibe el estado tal como esta en la base de datos (PROGRAMADA, ATENDIDA, CANCELADA). */
    public void setEstado(String estado) {
        switch (estado == null ? "" : estado) {
            case EstadoCita.CANCELADA -> {
                fondo = new Color(0xFEE2E2);
                setForeground(new Color(0xB91C1C));
            }
            case EstadoCita.ATENDIDA -> {
                fondo = new Color(0xDBEAFE);
                setForeground(new Color(0x1D4ED8));
            }
            default -> {
                fondo = Tema.VERDE_CLARO;
                setForeground(Tema.VERDE_OSCURO);
            }
        }
        setText(EstadoCita.visible(estado));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Tema.suavizar(g);
        g.setColor(fondo);
        g.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
        if (punto) {
            g.setColor(getForeground());
            g.fillOval(10, (getHeight() - 6) / 2, 6, 6);
        }
        g.dispose();
        super.paintComponent(g0);
    }
}
