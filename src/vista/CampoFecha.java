package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.Consumer;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

/** Campo dd/mm/aaaa con icono de calendario que abre un selector de fecha. */
public class CampoFecha extends CampoTexto {
    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final int ZONA_ICONO = 34;

    private boolean noPasadas = false;
    private boolean noFuturas = false;
    private boolean maxUnAnio = false;

    public CampoFecha() {
        super("dd/mm/aaaa");
        setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Tema.BORDE_CAMPO, 1, true), new EmptyBorder(7, 10, 7, ZONA_ICONO)));
        ((AbstractDocument) getDocument()).setDocumentFilter(new FiltroFecha());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getX() > getWidth() - ZONA_ICONO) {
                    abrirCalendario();
                }
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(
                        e.getX() > getWidth() - ZONA_ICONO ? Cursor.HAND_CURSOR : Cursor.TEXT_CURSOR));
            }
        });
    }

    /** Bloquea en el calendario los dias anteriores a hoy y/o posteriores a hoy. */
    public void setLimites(boolean noPasadas, boolean noFuturas) {
        this.noPasadas = noPasadas;
        this.noFuturas = noFuturas;
    }

    /** Bloquea en el calendario los dias posteriores a un año desde hoy (citas). */
    public void setMaximoUnAnio(boolean maxUnAnio) {
        this.maxUnAnio = maxUnAnio;
    }

    /** Devuelve la fecha escrita o null si esta vacia o no es valida. */
    public LocalDate getFecha() {
        try {
            return LocalDate.parse(getText().trim(), FORMATO);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public void setFecha(LocalDate fecha) {
        setText(fecha == null ? "" : fecha.format(FORMATO));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        new Iconos(Iconos.Tipo.CALENDARIO, Tema.TEXTO_SUAVE, 14)
                .paintIcon(this, g, getWidth() - 26, (getHeight() - 14) / 2);
    }

    private void abrirCalendario() {
        LocalDate base = getFecha() != null ? getFecha() : LocalDate.now();
        JPopupMenu menu = new JPopupMenu();
        menu.setBorder(new LineBorder(Tema.BORDE_CAMPO));
        menu.add(new PanelCalendario(base, noPasadas, noFuturas, maxUnAnio, f -> {
            setFecha(f);
            menu.setVisible(false);
        }));
        menu.show(this, 0, getHeight());
    }

    /** Solo permite digitos y "/", maximo 10 caracteres, y agrega las barras automaticamente. */
    private static class FiltroFecha extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int off, String s, AttributeSet a) throws BadLocationException {
            replace(fb, off, 0, s, a);
        }

        @Override
        public void replace(FilterBypass fb, int off, int len, String s, AttributeSet a) throws BadLocationException {
            if (s == null) {
                super.replace(fb, off, len, s, a);
                return;
            }
            String nuevo = s.replaceAll("[^0-9/]", "");
            Document doc = fb.getDocument();
            String actual = doc.getText(0, doc.getLength());
            String resultado = actual.substring(0, off) + nuevo + actual.substring(off + len);
            if (nuevo.length() > 1) {
                // Texto pegado o asignado completo: se normaliza a dd/mm/aaaa
                String digitos = resultado.replaceAll("[^0-9]", "");
                if (digitos.length() > 8) {
                    return;
                }
                resultado = formatear(digitos);
            } else {
                if (resultado.length() > 10) {
                    return;
                }
                boolean alFinal = off + len == actual.length();
                if (alFinal && !nuevo.isEmpty() && (resultado.length() == 2 || resultado.length() == 5)) {
                    resultado += "/";
                }
            }
            super.replace(fb, 0, doc.getLength(), resultado, a);
        }

        /** "15052000" -> "15/05/2000" (formatea de forma progresiva). */
        private static String formatear(String d) {
            StringBuilder sb = new StringBuilder(d.substring(0, Math.min(2, d.length())));
            if (d.length() > 2) {
                sb.append('/').append(d, 2, Math.min(4, d.length()));
            }
            if (d.length() > 4) {
                sb.append('/').append(d.substring(4));
            }
            return sb.toString();
        }
    }

    /** Calendario mensual sencillo. */
    private static class PanelCalendario extends JPanel {
        private YearMonth mes;
        private final Consumer<LocalDate> alElegir;
        private final boolean noPasadas;
        private final boolean noFuturas;
        private final boolean maxUnAnio;
        private final JLabel titulo = new JLabel("", SwingConstants.CENTER);
        private final JPanel dias = new JPanel(new GridLayout(7, 7, 2, 2));

        PanelCalendario(LocalDate base, boolean noPasadas, boolean noFuturas, boolean maxUnAnio,
                        Consumer<LocalDate> alElegir) {
            super(new BorderLayout(0, 6));
            this.alElegir = alElegir;
            this.noPasadas = noPasadas;
            this.noFuturas = noFuturas;
            this.maxUnAnio = maxUnAnio;
            this.mes = YearMonth.from(base);
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(8, 8, 8, 8));
            setPreferredSize(new Dimension(266, 252));
            titulo.setFont(Tema.f(Font.BOLD, 12));
            JButton ant = navegacion("<", -1);
            JButton sig = navegacion(">", 1);
            JPanel cabecera = new JPanel(new BorderLayout());
            cabecera.setOpaque(false);
            cabecera.add(ant, BorderLayout.WEST);
            cabecera.add(titulo, BorderLayout.CENTER);
            cabecera.add(sig, BorderLayout.EAST);
            dias.setOpaque(false);
            add(cabecera, BorderLayout.NORTH);
            add(dias, BorderLayout.CENTER);
            armar();
        }

        private JButton navegacion(String texto, int delta) {
            JButton b = new JButton(new AbstractAction(texto) {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    mes = mes.plusMonths(delta);
                    armar();
                }
            });
            estilo(b);
            b.setPreferredSize(new Dimension(30, 26));
            return b;
        }

        private void estilo(JButton b) {
            b.setFont(Tema.f(Font.PLAIN, 11));
            b.setFocusPainted(false);
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
            b.setOpaque(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.setMargin(new java.awt.Insets(0, 0, 0, 0));
        }

        private void armar() {
            dias.removeAll();
            titulo.setText(mes.getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-CO"))
                    + " " + mes.getYear());
            for (String n : new String[]{"L", "M", "M", "J", "V", "S", "D"}) {
                JLabel l = new JLabel(n, SwingConstants.CENTER);
                l.setFont(Tema.f(Font.BOLD, 10));
                l.setForeground(Tema.TEXTO_SUAVE);
                dias.add(l);
            }
            int desfase = mes.atDay(1).getDayOfWeek().getValue() - 1;
            for (int i = 0; i < desfase; i++) {
                dias.add(new JLabel(""));
            }
            for (int d = 1; d <= mes.lengthOfMonth(); d++) {
                LocalDate fecha = mes.atDay(d);
                JButton b = new JButton(String.valueOf(d));
                estilo(b);
                if (fecha.equals(LocalDate.now())) {
                    b.setForeground(Tema.VERDE);
                    b.setFont(Tema.f(Font.BOLD, 11));
                }
                boolean bloqueado = (noPasadas && fecha.isBefore(LocalDate.now()))
                        || (noFuturas && fecha.isAfter(LocalDate.now()))
                        || (maxUnAnio && fecha.isAfter(LocalDate.now().plusYears(1)));
                if (bloqueado) {
                    b.setEnabled(false);
                } else {
                    b.addActionListener(e -> alElegir.accept(fecha));
                }
                dias.add(b);
            }
            int total = desfase + mes.lengthOfMonth();
            for (int i = total; i < 42; i++) {
                dias.add(new JLabel(""));
            }
            dias.revalidate();
            dias.repaint();
        }
    }
}
