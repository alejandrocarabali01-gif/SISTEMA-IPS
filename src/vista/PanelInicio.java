package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.IntConsumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/** Pantalla "Ventana principal" del mockup. */
public class PanelInicio extends JPanel {

    public PanelInicio(IntConsumer irA) {
        super(new BorderLayout());
        JPanel caja = Tema.caja();
        Tema.agregar(caja, Tema.encabezadoPagina("Inicio",
                "Un espacio para administrar pacientes, médicos y citas.", "Ventana principal"), 16);
        Tema.agregar(caja, hero(), 22);
        Tema.agregar(caja, Tema.texto("¿Qué desea gestionar?", Font.BOLD, 14, Tema.TEXTO), 10);

        JPanel modulos = new JPanel(new GridLayout(1, 3, 16, 0));
        modulos.setOpaque(false);
        modulos.add(modulo(Iconos.Tipo.PERSONA, "Pacientes",
                "Registre y consulte los datos de identificación y contacto de sus pacientes.",
                "Ir a pacientes", () -> irA.accept(1)));
        modulos.add(modulo(Iconos.Tipo.MALETIN, "Médicos",
                "Administre el directorio médico y las especialidades de los profesionales.",
                "Ir a médicos", () -> irA.accept(2)));
        modulos.add(modulo(Iconos.Tipo.CALENDARIO, "Citas médicas",
                "Programe citas y consulte la agenda por fecha o estado de atención.",
                "Ir a citas médicas", () -> irA.accept(3)));
        Tema.agregar(caja, modulos, 16);
        Tema.agregar(caja, ayuda(), 0);
        add(Tema.pagina(caja), BorderLayout.CENTER);
    }

    private JPanel hero() {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Tema.suavizar(g);
                g.setColor(Tema.VERDE_CLARO);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g.setColor(new Color(0xD3EBDB));
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g.dispose();
            }

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(26, 28, 26, 28));

        JPanel texto = new JPanel();
        texto.setOpaque(false);
        texto.setLayout(new BoxLayout(texto, BoxLayout.Y_AXIS));
        texto.add(Tema.texto("SISTEMA DE GESTIÓN IPS", Font.BOLD, 10, Tema.VERDE_OSCURO));
        texto.add(Box.createVerticalStrut(8));
        JLabel titulo = Tema.texto("<html><div style='width:380px'>La atención comienza con una buena gestión.</div></html>",
                Font.BOLD, 28, Tema.TEXTO);
        texto.add(titulo);
        texto.add(Box.createVerticalStrut(8));
        texto.add(Tema.texto("<html><div style='width:380px'>Seleccione una de las pestañas o acceda a un módulo "
                + "para registrar información y organizar la atención médica.</div></html>",
                Font.PLAIN, 12, Tema.TEXTO_SUAVE));
        p.add(texto, BorderLayout.WEST);

        JComponent tarjetaIps = new JComponent() {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                Tema.suavizar(g);
                g.setColor(Color.WHITE);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g.setColor(new Color(0xD3EBDB));
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g.setColor(Tema.VERDE);
                g.fillRoundRect(16, 18, 30, 30, 8, 8);
                new Iconos(Iconos.Tipo.MAS, Color.WHITE, 16).paintIcon(this, g, 23, 25);
                g.setColor(Tema.TEXTO);
                g.setFont(Tema.f(Font.BOLD, 13));
                g.drawString("IPS", 58, 38);
                g.setColor(Tema.BORDE);
                g.fillRoundRect(16, 64, 140, 6, 6, 6);
                g.fillRoundRect(16, 78, 100, 6, 6, 6);
                g.dispose();
            }
        };
        tarjetaIps.setPreferredSize(new Dimension(190, 100));
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 22));
        derecha.setOpaque(false);
        derecha.add(tarjetaIps);
        p.add(derecha, BorderLayout.EAST);
        return p;
    }

    private Tarjeta modulo(Iconos.Tipo icono, String titulo, String descripcion, String enlace, Runnable accion) {
        Tarjeta t = new Tarjeta();
        JPanel v = new JPanel();
        v.setOpaque(false);
        v.setLayout(new BoxLayout(v, BoxLayout.Y_AXIS));
        Medallon m = new Medallon(icono);
        m.setAlignmentX(LEFT_ALIGNMENT);
        v.add(m);
        v.add(Box.createVerticalStrut(10));
        JLabel t1 = Tema.texto(titulo, Font.BOLD, 14, Tema.TEXTO);
        t1.setAlignmentX(LEFT_ALIGNMENT);
        v.add(t1);
        v.add(Box.createVerticalStrut(4));
        JLabel d = Tema.texto("<html><div style='width:230px'>" + descripcion + "</div></html>",
                Font.PLAIN, 12, Tema.TEXTO_SUAVE);
        d.setAlignmentX(LEFT_ALIGNMENT);
        v.add(d);
        v.add(Box.createVerticalStrut(10));
        JPanel link = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        link.setOpaque(false);
        link.setAlignmentX(LEFT_ALIGNMENT);
        link.add(Tema.texto(enlace + "  ", Font.BOLD, 12, Tema.VERDE_OSCURO));
        link.add(new JLabel(new Iconos(Iconos.Tipo.FLECHA, Tema.VERDE_OSCURO, 14)));
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                accion.run();
            }
        });
        v.add(link);
        t.add(v, BorderLayout.CENTER);
        return t;
    }

    private Tarjeta ayuda() {
        Tarjeta t = new Tarjeta();
        t.setBorder(new EmptyBorder(12, 18, 12, 18));
        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        izq.setOpaque(false);
        izq.add(new Medallon(Iconos.Tipo.AYUDA));
        izq.add(Box.createHorizontalStrut(12));
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(Tema.texto("¿Necesita orientación?", Font.BOLD, 13, Tema.TEXTO));
        textos.add(Tema.texto("Consulte la ayuda para conocer los campos y acciones de cada módulo.",
                Font.PLAIN, 11, Tema.TEXTO_SUAVE));
        izq.add(textos);
        t.add(izq, BorderLayout.WEST);

        Boton b = new Boton("Abrir ayuda", Boton.Estilo.BLANCO, Iconos.Tipo.AYUDA);
        b.addActionListener(e -> Dialogos.info(this,
                "Pacientes: registre, busque, modifique o elimine pacientes.\n"
                + "Médicos: administre el directorio y las especialidades.\n"
                + "Citas médicas: programe, modifique, cancele o marque como atendida una cita.\n\n"
                + "Los campos marcados con * son obligatorios."));
        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        der.setOpaque(false);
        der.add(b);
        t.add(der, BorderLayout.EAST);
        return t;
    }

    /** Circulo verde claro con un icono. */
    private static class Medallon extends JComponent {
        private final Iconos.Tipo tipo;

        Medallon(Iconos.Tipo tipo) {
            this.tipo = tipo;
            setPreferredSize(new Dimension(34, 34));
            setMaximumSize(new Dimension(34, 34));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Tema.suavizar(g);
            g.setColor(Tema.VERDE_CLARO);
            g.fillOval(0, 0, 33, 33);
            new Iconos(tipo, Tema.VERDE_OSCURO, 16).paintIcon(this, g, 9, 9);
            g.dispose();
        }
    }
}
