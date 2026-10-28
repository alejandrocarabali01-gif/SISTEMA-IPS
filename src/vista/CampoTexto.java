package vista;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/** Campo de texto con borde redondeado y texto de ayuda (placeholder). */
public class CampoTexto extends JTextField {
    private final String pista;

    public CampoTexto(String pista) {
        this.pista = pista;
        setFont(Tema.f(java.awt.Font.PLAIN, 12));
        setForeground(Tema.TEXTO);
        setCaretColor(Tema.TEXTO);
        setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Tema.BORDE_CAMPO, 1, true), new EmptyBorder(7, 10, 7, 10)));
    }

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }
        Dimension d = super.getPreferredSize();
        return new Dimension(Math.max(d.width, 120), 34);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getText().isEmpty() && pista != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            Tema.suavizar(g2);
            g2.setColor(new Color(0x9CA3AF));
            g2.setFont(getFont());
            Insets in = getInsets();
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(pista, in.left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}
