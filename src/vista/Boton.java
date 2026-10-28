package vista;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JButton;

/** Boton redondeado con los cuatro estilos del mockup. */
public class Boton extends JButton {

    public enum Estilo { BLANCO, VERDE, AZUL, ROJO }

    private final Estilo estilo;

    public Boton(String texto, Estilo estilo, Iconos.Tipo icono) {
        super(texto);
        this.estilo = estilo;
        Color fg = estilo == Estilo.BLANCO ? Tema.TEXTO : Color.WHITE;
        setFont(Tema.f(Font.BOLD, 12));
        setForeground(fg);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (icono != null) {
            setIcon(new Iconos(icono, fg, 14));
            setIconTextGap(6);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width + 14, 34);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Tema.suavizar(g);
        boolean encima = getModel().isRollover();
        Color fondo = switch (estilo) {
            case VERDE -> encima ? Tema.VERDE_OSCURO : Tema.VERDE;
            case AZUL -> encima ? new Color(0x1D4ED8) : Tema.AZUL;
            case ROJO -> encima ? new Color(0xB91C1C) : Tema.ROJO;
            default -> encima ? new Color(0xF3F4F6) : Color.WHITE;
        };
        g.setColor(fondo);
        g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
        if (estilo == Estilo.BLANCO) {
            g.setColor(Tema.BORDE_CAMPO);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
        }
        g.dispose();
        super.paintComponent(g0);
    }
}
