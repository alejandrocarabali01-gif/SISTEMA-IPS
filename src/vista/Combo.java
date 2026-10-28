package vista;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;

/**
 * Lista desplegable. El primer elemento (indice 0) es el texto de ayuda
 * ("Seleccione un paciente"); los elementos reales empiezan en el indice 1.
 */
public class Combo extends JComboBox<Object> {
    private final String pista;

    public Combo(String pista) {
        this(pista, true);
    }

    public Combo(String pista, boolean atenuarPista) {
        this.pista = pista;
        addItem(pista);
        setFont(Tema.f(java.awt.Font.PLAIN, 12));
        setBackground(Color.WHITE);
        setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton b = new JButton(new Iconos(Iconos.Tipo.CHEVRON, Tema.TEXTO_SUAVE, 12));
                b.setBorder(new EmptyBorder(0, 6, 0, 10));
                b.setContentAreaFilled(false);
                b.setFocusPainted(false);
                b.setOpaque(false);
                return b;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                // sin fondo azul de seleccion
            }

            @Override
            public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
                Component c = comboBox.getRenderer().getListCellRendererComponent(
                        listBox, comboBox.getSelectedItem(), -1, false, false);
                c.setFont(comboBox.getFont());
                currentValuePane.paintComponent(g, c, comboBox, bounds.x, bounds.y,
                        bounds.width, bounds.height, false);
            }
        });
        setBorder(new LineBorder(Tema.BORDE_CAMPO, 1, true));
        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                          boolean sel, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, sel, foco);
                setBorder(new EmptyBorder(6, 10, 6, 10));
                setFont(Tema.f(java.awt.Font.PLAIN, 12));
                boolean apagado = (atenuarPista && pista.equals(valor))
                        || (valor instanceof Deshabilitable d && d.deshabilitado());
                if (apagado) {
                    setForeground(new Color(0x9CA3AF));
                }
                if (valor instanceof Deshabilitable d && d.deshabilitado()) {
                    setBackground(Color.WHITE);
                }
                return this;
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }
        Dimension d = super.getPreferredSize();
        return new Dimension(Math.max(d.width, 120), 34);
    }

    /** Ignora los elementos deshabilitados (por ejemplo, una hora ocupada). */
    @Override
    public void setSelectedItem(Object o) {
        if (o instanceof Deshabilitable d && d.deshabilitado()) {
            return;
        }
        super.setSelectedItem(o);
    }

    /** Reemplaza los elementos (conserva la ayuda en el indice 0) y la deja seleccionada. */
    public void reiniciar(List<?> elementos) {
        removeAllItems();
        addItem(pista);
        for (Object o : elementos) {
            addItem(o);
        }
        setSelectedIndex(0);
    }

    public boolean haySeleccion() {
        return getSelectedIndex() > 0;
    }

    public void limpiar() {
        setSelectedIndex(0);
    }

    public Opcion opcion() {
        return getSelectedItem() instanceof Opcion o ? o : null;
    }

    public void seleccionarPorId(int id) {
        for (int i = 1; i < getItemCount(); i++) {
            if (getItemAt(i) instanceof Opcion o && o.id() == id) {
                setSelectedIndex(i);
                return;
            }
        }
        setSelectedIndex(0);
    }

    public void seleccionarPorTexto(String texto) {
        for (int i = 1; i < getItemCount(); i++) {
            if (getItemAt(i).toString().equals(texto)) {
                setSelectedIndex(i);
                return;
            }
        }
        setSelectedIndex(0);
    }
}
