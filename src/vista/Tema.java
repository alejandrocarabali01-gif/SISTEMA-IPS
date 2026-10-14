package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;

/** Colores, fuentes y utilidades de diseño comunes a todas las pantallas. */
public final class Tema {
    public static final Color FONDO = new Color(0xF3F4F6);
    public static final Color VERDE = new Color(0x1E9E4A);
    public static final Color VERDE_OSCURO = new Color(0x15803D);
    public static final Color VERDE_CLARO = new Color(0xE8F5EC);
    public static final Color AZUL = new Color(0x2563EB);
    public static final Color ROJO = new Color(0xDC2626);
    public static final Color TEXTO = new Color(0x111827);
    public static final Color TEXTO_SUAVE = new Color(0x6B7280);
    public static final Color BORDE = new Color(0xE5E7EB);
    public static final Color BORDE_CAMPO = new Color(0xD1D5DB);

    private Tema() {
    }

    public static Font f(int estilo, int tam) {
        return new Font("Segoe UI", estilo, tam);
    }

    public static void suavizar(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    public static JLabel texto(String t, int estilo, int tam, Color color) {
        JLabel l = new JLabel(t);
        l.setFont(f(estilo, tam));
        l.setForeground(color);
        return l;
    }

    /** Etiqueta pequeña encima de un componente de entrada. */
    public static JPanel campo(String titulo, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        p.add(texto(titulo, Font.BOLD, 11, TEXTO), BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    /** Grilla de 2 columnas. Una fila con un solo elemento ocupa las dos columnas. */
    public static JPanel grilla(JComponent[][] filas) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        for (int r = 0; r < filas.length; r++) {
            for (int c = 0; c < filas[r].length; c++) {
                if (filas[r][c] == null) {
                    continue;
                }
                gbc.gridx = c;
                gbc.gridy = r;
                gbc.gridwidth = filas[r].length == 1 ? 2 : 1;
                gbc.insets = new Insets(0, c == 0 ? 0 : 24, 12, 0);
                p.add(filas[r][c], gbc);
            }
        }
        return p;
    }

    public static JPanel encabezadoPagina(String titulo, String subtitulo, String insignia) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JPanel izq = new JPanel();
        izq.setOpaque(false);
        izq.setLayout(new BoxLayout(izq, BoxLayout.Y_AXIS));
        izq.add(texto(titulo, Font.BOLD, 24, TEXTO));
        izq.add(Box.createVerticalStrut(4));
        izq.add(texto(subtitulo, Font.PLAIN, 12, TEXTO_SUAVE));
        p.add(izq, BorderLayout.WEST);
        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        der.setOpaque(false);
        der.add(Chip.insignia(insignia));
        p.add(der, BorderLayout.EAST);
        return p;
    }

    public static JPanel tituloTarjeta(String titulo, String derecha) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 12, 0));
        p.add(texto(titulo, Font.BOLD, 13, TEXTO), BorderLayout.WEST);
        if (derecha != null) {
            p.add(texto(derecha, Font.PLAIN, 11, TEXTO_SUAVE), BorderLayout.EAST);
        }
        return p;
    }

    public static JPanel fila(Component... comps) {
        return fila(8, comps);
    }

    /** Fila horizontal alineada a la izquierda, sin sangria, con separacion entre elementos. */
    public static JPanel fila(int espacio, Component... comps) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        for (int i = 0; i < comps.length; i++) {
            if (comps[i] instanceof JComponent jc) {
                jc.setAlignmentY(Component.CENTER_ALIGNMENT);
                jc.setMaximumSize(jc.getPreferredSize());
            }
            if (i > 0) {
                p.add(Box.createHorizontalStrut(espacio));
            }
            p.add(comps[i]);
        }
        p.add(Box.createHorizontalGlue());
        return p;
    }

    /** Contenedor vertical para apilar tarjetas. */
    public static JPanel caja() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(22, 28, 22, 28));
        return p;
    }

    public static void agregar(JPanel caja, JComponent c, int espacioDespues) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        caja.add(c);
        if (espacioDespues > 0) {
            caja.add(Box.createVerticalStrut(espacioDespues));
        }
    }

    /** Envuelve el contenido en un scroll con el fondo gris de la aplicacion. */
    public static JScrollPane pagina(JPanel contenido) {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(FONDO);
        raiz.add(contenido, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(raiz);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getViewport().setBackground(FONDO);
        return sp;
    }

    public static void alCambiar(JTextComponent campo, Runnable accion) {
        campo.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                accion.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                accion.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                accion.run();
            }
        });
    }

    public static void estilizarTabla(JTable t) {
        t.setRowHeight(34);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(BORDE);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setFont(f(Font.PLAIN, 12));
        t.setForeground(TEXTO);
        t.setBackground(Color.WHITE);
        t.setSelectionBackground(VERDE_CLARO);
        t.setSelectionForeground(TEXTO);
        t.setFillsViewportHeight(true);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean sel,
                                                           boolean foco, int fila, int col) {
                super.getTableCellRendererComponent(tabla, valor, sel, false, fila, col);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return this;
            }
        });
        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 34));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean sel,
                                                           boolean foco, int fila, int col) {
                super.getTableCellRendererComponent(tabla, valor, false, false, fila, col);
                setBackground(Color.WHITE);
                setForeground(TEXTO_SUAVE);
                setFont(f(Font.BOLD, 11));
                setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, BORDE), new EmptyBorder(0, 10, 0, 10)));
                return this;
            }
        });
    }

    public static JScrollPane scrollTabla(JTable t, int alto) {
        estilizarTabla(t);
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(new LineBorder(BORDE));
        sp.getViewport().setBackground(Color.WHITE);
        sp.setPreferredSize(new Dimension(0, alto));
        return sp;
    }
}
