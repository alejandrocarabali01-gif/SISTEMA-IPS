package vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import javax.swing.Icon;

/** Iconos simples dibujados con Java2D (no requieren archivos de imagen). */
public class Iconos implements Icon {

    public enum Tipo { MAS, DISCO, LAPIZ, PAPELERA, ESCOBA, CHECK, EQUIS, LUPA, FLECHA,
        CALENDARIO, PERSONA, AYUDA, CASA, MALETIN, CHEVRON }

    private final Tipo tipo;
    private final Color color;
    private final int tam;

    public Iconos(Tipo tipo, Color color, int tam) {
        this.tipo = tipo;
        this.color = color;
        this.tam = tam;
    }

    @Override
    public void paintIcon(Component c, Graphics g0, int x, int y) {
        Graphics2D g = (Graphics2D) g0.create();
        Tema.suavizar(g);
        g.setColor(color);
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.translate(x, y);
        int s = tam;
        int m = s / 2;
        switch (tipo) {
            case MAS -> {
                g.drawLine(m, 2, m, s - 2);
                g.drawLine(2, m, s - 2, m);
            }
            case DISCO -> {
                g.drawRoundRect(2, 2, s - 4, s - 4, 3, 3);
                g.drawRect(5, 2, s - 10, 4);
                g.drawLine(5, s - 3, 5, m + 1);
                g.drawLine(s - 5, s - 3, s - 5, m + 1);
            }
            case LAPIZ -> {
                g.drawPolygon(new int[]{s - 4, s - 2, 5, 2}, new int[]{2, 4, s - 2, s - 2}, 4);
                g.drawLine(s - 6, 4, s - 3, 7);
            }
            case PAPELERA -> {
                g.drawLine(2, 4, s - 2, 4);
                g.drawLine(m - 2, 2, m + 2, 2);
                g.drawRoundRect(4, 5, s - 8, s - 7, 2, 2);
                g.drawLine(m, 7, m, s - 4);
            }
            case ESCOBA -> {
                g.drawPolygon(new int[]{2, m, s - 2, m - 4}, new int[]{m + 2, 2, m - 1, s - 3}, 4);
                g.drawLine(2, s - 2, s - 2, s - 2);
            }
            case CHECK -> {
                g.drawLine(2, m, m - 1, s - 3);
                g.drawLine(m - 1, s - 3, s - 2, 3);
            }
            case EQUIS -> {
                g.drawLine(3, 3, s - 3, s - 3);
                g.drawLine(s - 3, 3, 3, s - 3);
            }
            case LUPA -> {
                g.drawOval(2, 2, s - 7, s - 7);
                g.drawLine(s - 5, s - 5, s - 2, s - 2);
            }
            case FLECHA -> {
                g.drawLine(2, m, s - 3, m);
                g.drawLine(s - 6, m - 3, s - 3, m);
                g.drawLine(s - 6, m + 3, s - 3, m);
            }
            case CALENDARIO -> {
                g.drawRoundRect(2, 3, s - 4, s - 5, 3, 3);
                g.drawLine(2, 7, s - 2, 7);
                g.drawLine(5, 1, 5, 4);
                g.drawLine(s - 5, 1, s - 5, 4);
            }
            case PERSONA -> {
                g.drawOval(m - 3, 1, 6, 6);
                g.drawArc(3, 9, s - 6, s - 6, 0, 180);
            }
            case AYUDA -> {
                g.drawOval(1, 1, s - 2, s - 2);
                g.setFont(new Font("SansSerif", Font.BOLD, s - 4));
                FontMetrics fm = g.getFontMetrics();
                g.drawString("?", (s - fm.stringWidth("?")) / 2, (s + fm.getAscent() - fm.getDescent()) / 2);
            }
            case CASA -> {
                g.drawPolyline(new int[]{2, m, s - 2}, new int[]{m, 2, m}, 3);
                g.drawPolyline(new int[]{4, 4, s - 4, s - 4}, new int[]{m, s - 2, s - 2, m}, 4);
            }
            case MALETIN -> {
                g.drawRoundRect(2, 5, s - 4, s - 7, 3, 3);
                g.drawRoundRect(m - 3, 2, 6, 4, 2, 2);
                g.drawLine(2, m + 1, s - 2, m + 1);
            }
            case CHEVRON -> {
                g.drawLine(2, m - 1, m, m + 2);
                g.drawLine(m, m + 2, s - 2, m - 1);
            }
            default -> {
            }
        }
        g.dispose();
    }

    @Override
    public int getIconWidth() {
        return tam;
    }

    @Override
    public int getIconHeight() {
        return tam;
    }
}
