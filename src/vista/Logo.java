package vista;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

/**
 * Logo de Nuxus Soluciones. Si existe el archivo src/recursos/logo.png se usa esa imagen;
 * si no, se dibuja un logo provisional.
 */
public class Logo extends JComponent {
    private Image imagen;

    public Logo(int tam) {
        setPreferredSize(new Dimension(tam, tam));
        URL url = Logo.class.getResource("/imagenes/logo.jpeg");
        if (url != null) {
            imagen = new ImageIcon(url).getImage();
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Tema.suavizar(g);
        if (imagen != null) {
            g.drawImage(imagen, 0, 0, getWidth(), getHeight(), null);
        } else {
            g.setColor(Tema.VERDE);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g.setColor(Color.WHITE);
            g.setFont(Tema.f(Font.BOLD, getHeight() / 2 + 2));
            FontMetrics fm = g.getFontMetrics();
            g.drawString("N", (getWidth() - fm.stringWidth("N")) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
        }
        g.dispose();
    }
}
