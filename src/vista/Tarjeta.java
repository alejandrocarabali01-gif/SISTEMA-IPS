package vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/** Panel blanco con borde redondeado (las "tarjetas" del mockup). */
public class Tarjeta extends JPanel {

    public Tarjeta() {
        super(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(16, 18, 16, 18));
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Tema.suavizar(g);
        g.setColor(java.awt.Color.WHITE);
        g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        g.setColor(Tema.BORDE);
        g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        g.dispose();
    }
}
