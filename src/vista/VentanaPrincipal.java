package vista;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/** Ventana principal: encabezado, pestañas, contenido y barra de mensajes. */
public class VentanaPrincipal extends JFrame {
    private static final String[] NOMBRES = {"Inicio", "Pacientes", "Médicos", "Citas médicas"};
    private static final String[] TITULOS = {"Ventana principal", "Pacientes", "Médicos", "Citas médicas"};
    private static final String[] MENSAJES = {
        "Bienvenido. Seleccione un módulo para comenzar.",
        "Seleccione un registro para consultar o actualizar sus datos.",
        "Seleccione un registro para consultar o actualizar sus datos.",
        "Al crear una cita, su estado se asigna automáticamente como Programada."
    };

    private final CardLayout cartas = new CardLayout();
    private final JPanel contenedor = new JPanel(cartas);
    private final JLabel lblMensaje = Tema.texto(MENSAJES[0], Font.PLAIN, 11, Tema.TEXTO_SUAVE);
    private final JPanel[] paneles = new JPanel[4];
    private final BarraPestanas barra;

    public VentanaPrincipal() {
        super("IPS Unicatólica de la gente - Ventana principal");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 780);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Tema.FONDO);
        setLayout(new BorderLayout());

        barra = new BarraPestanas(NOMBRES,
                new Iconos.Tipo[]{Iconos.Tipo.CASA, Iconos.Tipo.PERSONA, Iconos.Tipo.MALETIN, Iconos.Tipo.CALENDARIO},
                this::mostrar);

        JPanel norte = new JPanel();
        norte.setLayout(new BoxLayout(norte, BoxLayout.Y_AXIS));
        norte.setBackground(Color.WHITE);
        norte.add(encabezado());
        norte.add(barra);
        add(norte, BorderLayout.NORTH);

        paneles[0] = new PanelInicio(this::mostrar);
        paneles[1] = new PanelPacientes(this::setMensaje);
        paneles[2] = new PanelMedicos(this::setMensaje);
        paneles[3] = new PanelCitas(this::setMensaje);
        for (int i = 0; i < paneles.length; i++) {
            contenedor.add(paneles[i], String.valueOf(i));
        }
        add(contenedor, BorderLayout.CENTER);
        add(pie(), BorderLayout.SOUTH);

        mostrar(0);
    }

    private void mostrar(int indice) {
        cartas.show(contenedor, String.valueOf(indice));
        barra.seleccionar(indice);
        setTitle("IPS Unicatólica de la gente - " + TITULOS[indice]);
        if (paneles[indice] instanceof Refrescable r) {
            r.refrescar();
        }
        setMensaje(MENSAJES[indice]);
    }

    private void setMensaje(String texto) {
        lblMensaje.setText(texto);
    }

    private JPanel encabezado() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Tema.BORDE), new EmptyBorder(10, 20, 10, 20)));

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        izq.setOpaque(false);
        izq.add(new Logo(36));
        izq.add(Tema.texto("Nuxus Soluciones", Font.BOLD, 15, Tema.TEXTO));
        p.add(izq, BorderLayout.WEST);

        JPanel der = new JPanel();
        der.setOpaque(false);
        der.setLayout(new BoxLayout(der, BoxLayout.Y_AXIS));
        JLabel nombre = Tema.texto("IPS Unicatólica de la gente", Font.BOLD, 12, Tema.TEXTO);
        JLabel sub = Tema.texto("Sistema de gestión IPS", Font.PLAIN, 11, Tema.TEXTO_SUAVE);
        nombre.setAlignmentX(RIGHT_ALIGNMENT);
        sub.setAlignmentX(RIGHT_ALIGNMENT);
        der.add(nombre);
        der.add(sub);
        p.add(der, BorderLayout.EAST);
        return p;
    }

    private JPanel pie() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, Tema.BORDE), new EmptyBorder(7, 20, 7, 20)));
        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        izq.setOpaque(false);
        izq.add(new JLabel(new Iconos(Iconos.Tipo.CHECK, Tema.VERDE, 14)));
        izq.add(Tema.texto("Mensaje:", Font.BOLD, 11, Tema.TEXTO));
        izq.add(lblMensaje);
        p.add(izq, BorderLayout.WEST);

        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        der.setOpaque(false);
        der.add(new Logo(16));
        der.add(Tema.texto("Nuxus Soluciones", Font.PLAIN, 11, Tema.TEXTO_SUAVE));
        p.add(der, BorderLayout.EAST);
        return p;
    }
}
