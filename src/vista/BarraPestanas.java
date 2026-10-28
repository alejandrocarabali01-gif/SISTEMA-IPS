package vista;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/** Pestañas superiores: Inicio, Pacientes, Médicos y Citas médicas. */
public class BarraPestanas extends JPanel {
    private final List<Pestana> pestanas = new ArrayList<>();

    public BarraPestanas(String[] nombres, Iconos.Tipo[] iconos, IntConsumer alElegir) {
        super(new FlowLayout(FlowLayout.LEFT, 4, 0));
        setBackground(Color.WHITE);
        setBorder(new MatteBorder(0, 0, 1, 0, Tema.BORDE));
        for (int i = 0; i < nombres.length; i++) {
            Pestana p = new Pestana(nombres[i], iconos[i]);
            int indice = i;
            p.addActionListener(e -> alElegir.accept(indice));
            pestanas.add(p);
            add(p);
        }
    }

    public void seleccionar(int indice) {
        for (int i = 0; i < pestanas.size(); i++) {
            pestanas.get(i).activar(i == indice);
        }
    }

    private static class Pestana extends JButton {
        private final Iconos.Tipo tipo;
        private boolean activa;

        Pestana(String texto, Iconos.Tipo tipo) {
            super(texto);
            this.tipo = tipo;
            setFont(Tema.f(Font.PLAIN, 12));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(new EmptyBorder(10, 14, 10, 14));
            setIconTextGap(7);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            activar(false);
        }

        void activar(boolean activa) {
            this.activa = activa;
            Color c = activa ? Tema.VERDE_OSCURO : Tema.TEXTO_SUAVE;
            setForeground(c);
            setFont(Tema.f(activa ? Font.BOLD : Font.PLAIN, 12));
            setIcon(new Iconos(tipo, c, 14));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            if (activa) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setColor(Tema.VERDE_CLARO);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Tema.VERDE);
                g.fillRect(0, getHeight() - 2, getWidth(), 2);
                g.dispose();
            }
            super.paintComponent(g0);
        }
    }
}
