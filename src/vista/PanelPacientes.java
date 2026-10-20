package vista;

import dao.PacienteDAO;
import modelo.Paciente;
import util.Validador;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;

/** Pantalla "Gestión de pacientes" (RF01 - RF04). */
public class PanelPacientes extends JPanel implements Refrescable {
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final PacienteDAO dao = new PacienteDAO();
    private final Consumer<String> mensaje;

    private final CampoTexto txtIdentificacion = new CampoTexto("Ingrese el número de identificación");
    private final CampoTexto txtNombre = new CampoTexto("Ingrese el nombre completo");
    private final CampoTexto txtTelefono = new CampoTexto("Ingrese el número de teléfono");
    private final CampoTexto txtCorreo = new CampoTexto("Ingrese el correo electrónico");
    private final CampoFecha txtFecha = new CampoFecha();
    private final CampoTexto txtBuscar = new CampoTexto("Nombre o identificación");

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Identificación", "Nombre completo", "Teléfono", "Correo", "Fecha de nacimiento"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final JLabel lblTotal = Tema.texto("Pacientes encontrados: 0", Font.PLAIN, 11, Tema.TEXTO_SUAVE);

    private List<Paciente> datos = new ArrayList<>();
    private Integer seleccionado = null;
    private boolean cargando = false;

    public PanelPacientes(Consumer<String> mensaje) {
        super(new BorderLayout());
        this.mensaje = mensaje;
        JPanel caja = Tema.caja();
        Tema.agregar(caja, Tema.encabezadoPagina("Gestión de pacientes",
                "Registre, consulte y actualice la información de sus pacientes.", "Registro de pacientes"), 16);
        Tema.agregar(caja, tarjetaFormulario(), 16);
        Tema.agregar(caja, tarjetaDirectorio(), 0);
        add(Tema.pagina(caja), BorderLayout.CENTER);
        txtFecha.setLimites(false, true);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || cargando) {
                return;
            }
            int fila = tabla.getSelectedRow();
            if (fila >= 0 && fila < datos.size()) {
                cargarFormulario(datos.get(fila));
            }
        });
    }

    // ------------------------------------------------------------------ diseño

    private Tarjeta tarjetaFormulario() {
        Tarjeta t = new Tarjeta();
        t.add(Tema.tituloTarjeta("Datos del paciente", "* Campos obligatorios"), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 4));
        centro.setOpaque(false);
        centro.add(Tema.grilla(new JComponent[][]{
                {Tema.campo("Identificación *", txtIdentificacion), Tema.campo("Nombre completo *", txtNombre)},
                {Tema.campo("Teléfono *", txtTelefono), Tema.campo("Correo *", txtCorreo)},
                {Tema.campo("Fecha de nacimiento", txtFecha), null}
        }), BorderLayout.NORTH);

        Boton nuevo = new Boton("Nuevo", Boton.Estilo.BLANCO, Iconos.Tipo.MAS);
        Boton guardar = new Boton("Guardar", Boton.Estilo.VERDE, Iconos.Tipo.DISCO);
        Boton modificar = new Boton("Modificar", Boton.Estilo.AZUL, Iconos.Tipo.LAPIZ);
        Boton eliminar = new Boton("Eliminar", Boton.Estilo.ROJO, Iconos.Tipo.PAPELERA);
        Boton limpiar = new Boton("Limpiar", Boton.Estilo.BLANCO, Iconos.Tipo.ESCOBA);
        nuevo.addActionListener(e -> nuevo());
        guardar.addActionListener(e -> guardar());
        modificar.addActionListener(e -> modificar());
        eliminar.addActionListener(e -> eliminar());
        limpiar.addActionListener(e -> limpiar(true));
        JPanel botones = Tema.fila(nuevo, guardar, modificar, eliminar, limpiar);
        botones.setBorder(new javax.swing.border.CompoundBorder(
                new MatteBorder(1, 0, 0, 0, Tema.BORDE), new EmptyBorder(14, 0, 0, 0)));
        centro.add(botones, BorderLayout.SOUTH);
        t.add(centro, BorderLayout.CENTER);
        return t;
    }

    private Tarjeta tarjetaDirectorio() {
        Tarjeta t = new Tarjeta();
        JPanel norte = new JPanel();
        norte.setOpaque(false);
        norte.setLayout(new BoxLayout(norte, BoxLayout.Y_AXIS));
        JPanel titulo = Tema.tituloTarjeta("Directorio de pacientes", null);
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        norte.add(titulo);

        txtBuscar.setPreferredSize(new Dimension(320, 34));
        txtBuscar.addActionListener(e -> buscar());
        Boton buscar = new Boton("Buscar", Boton.Estilo.VERDE, null);
        Boton verTodos = new Boton("Ver todos", Boton.Estilo.BLANCO, null);
        buscar.addActionListener(e -> buscar());
        verTodos.addActionListener(e -> {
            txtBuscar.setText("");
            cargarTabla("");
        });
        JPanel busqueda = Tema.fila(Tema.texto("Buscar paciente", Font.BOLD, 11, Tema.TEXTO),
                txtBuscar, buscar, verTodos);
        busqueda.setBorder(new EmptyBorder(0, 0, 12, 0));
        busqueda.setAlignmentX(LEFT_ALIGNMENT);
        norte.add(busqueda);
        t.add(norte, BorderLayout.NORTH);

        t.add(Tema.scrollTabla(tabla, 190), BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        pie.setBorder(new EmptyBorder(10, 0, 0, 0));
        pie.add(lblTotal, BorderLayout.WEST);
        pie.add(Tema.texto("Página 1", Font.PLAIN, 11, Tema.TEXTO_SUAVE), BorderLayout.EAST);
        t.add(pie, BorderLayout.SOUTH);
        return t;
    }

    // ------------------------------------------------------------------ acciones

    @Override
    public void refrescar() {
        cargarTabla(txtBuscar.getText().trim());
    }

    private void nuevo() {
        limpiar(false);
        txtIdentificacion.requestFocusInWindow();
        mensaje.accept("Formulario listo para registrar un nuevo paciente.");
    }

    private void limpiar(boolean avisar) {
        txtIdentificacion.setText("");
        txtNombre.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
        txtFecha.setText("");
        seleccionado = null;
        tabla.clearSelection();
        if (avisar) {
            mensaje.accept("Formulario limpiado.");
        }
    }

    private void guardar() {
        if (seleccionado != null) {
            Dialogos.aviso(this, "Hay un paciente seleccionado.\nUse «Modificar» para actualizarlo "
                    + "o «Nuevo» para registrar otro paciente.");
            return;
        }
        Paciente p = leer(0);
        if (p == null) {
            return;
        }
        try {
            dao.insertar(p);
            limpiar(false);
            txtBuscar.setText("");
            cargarTabla("");
            mensaje.accept("Paciente registrado correctamente.");
        } catch (SQLException e) {
            errorBD(e, p.identificacion());
        }
    }

    private void modificar() {
        if (seleccionado == null) {
            Dialogos.aviso(this, "Seleccione un paciente del directorio para modificarlo.");
            return;
        }
        Paciente p = leer(seleccionado);
        if (p == null) {
            return;
        }
        try {
            dao.actualizar(p);
            limpiar(false);
            cargarTabla(txtBuscar.getText().trim());
            mensaje.accept("Datos del paciente actualizados correctamente.");
        } catch (SQLException e) {
            errorBD(e, p.identificacion());
        }
    }

    private void eliminar() {
        if (seleccionado == null) {
            Dialogos.aviso(this, "Seleccione un paciente del directorio para eliminarlo.");
            return;
        }
        if (!Dialogos.confirmar(this, "¿Está seguro de eliminar al paciente «" + txtNombre.getText().trim() + "»?")) {
            return;
        }
        try {
            dao.eliminar(seleccionado);
            limpiar(false);
            cargarTabla(txtBuscar.getText().trim());
            mensaje.accept("Paciente eliminado correctamente.");
        } catch (SQLException e) {
            errorBD(e, "");
        }
    }

    private void buscar() {
        cargarTabla(txtBuscar.getText().trim());
    }

    private void cargarTabla(String filtro) {
        try {
            datos = dao.buscar(filtro);
        } catch (SQLException e) {
            Dialogos.error(this, "No se pudo consultar la base de datos:\n" + e.getMessage());
            return;
        }
        cargando = true;
        modelo.setRowCount(0);
        for (Paciente p : datos) {
            modelo.addRow(new Object[]{p.id(), p.identificacion(), p.nombreCompleto(), p.telefono(), p.correo(),
                p.fechaNacimiento() == null ? "" : p.fechaNacimiento().format(FORMATO_FECHA)});
        }
        cargando = false;
        lblTotal.setText("Pacientes encontrados: " + datos.size());
        if (!filtro.isEmpty() && datos.isEmpty()) {
            mensaje.accept("No se encontraron pacientes con «" + filtro + "».");
        }
    }

    private void cargarFormulario(Paciente p) {
        txtIdentificacion.setText(p.identificacion());
        txtNombre.setText(p.nombreCompleto());
        txtTelefono.setText(p.telefono());
        txtCorreo.setText(p.correo() == null ? "" : p.correo());
        txtFecha.setFecha(p.fechaNacimiento());
        seleccionado = p.id();
        mensaje.accept("Paciente seleccionado: " + p.nombreCompleto() + ". Puede modificar o eliminar el registro.");
    }

    /** Valida campos obligatorios, formatos y reglas de dominio (RN07, RN08 - RN12). Devuelve null si algo falla. */
    private Paciente leer(int id) {
        String ident = txtIdentificacion.getText().trim();
        String nombre = txtNombre.getText().trim();
        String tel = txtTelefono.getText().trim();
        String correo = txtCorreo.getText().trim();
        if (ident.isEmpty() || nombre.isEmpty() || tel.isEmpty() || correo.isEmpty()) {
            Dialogos.aviso(this, "Complete los campos obligatorios (*): identificación, nombre completo, "
                    + "teléfono y correo.");
            return null;
        }
        if (!Validador.numero(ident, 5, 16)) {
            Dialogos.aviso(this, "La identificación debe contener solo números, entre 5 y 16 dígitos.");
            txtIdentificacion.requestFocusInWindow();
            return null;
        }
        if (!Validador.nombre(nombre)) {
            Dialogos.aviso(this, "El nombre solo puede contener letras y espacios (sin números ni símbolos), "
                    + "máximo 100 caracteres.");
            txtNombre.requestFocusInWindow();
            return null;
        }
        if (!Validador.numero(tel, 7, 15)) {
            Dialogos.aviso(this, "El teléfono debe contener solo números, entre 7 y 15 dígitos.");
            txtTelefono.requestFocusInWindow();
            return null;
        }
        if (!Validador.correo(correo)) {
            Dialogos.aviso(this, "El correo no es válido. Use un correo de un proveedor reconocido (por ejemplo gmail.com, hotmail.com, outlook.com o un correo institucional .edu.co), máximo 100 caracteres.");
            txtCorreo.requestFocusInWindow();
            return null;
        }
        LocalDate fecha = null;
        if (!txtFecha.getText().isBlank()) {
            fecha = txtFecha.getFecha();
            if (fecha == null) {
                Dialogos.aviso(this, "La fecha de nacimiento no es válida. Use el formato dd/mm/aaaa con una fecha real.");
                txtFecha.requestFocusInWindow();
                return null;
            }
            if (!Validador.noEsFutura(fecha)) {
                Dialogos.aviso(this, "La fecha de nacimiento no puede ser posterior a hoy.");
                txtFecha.requestFocusInWindow();
                return null;
            }
            if (!Validador.edadRealista(fecha)) {
                Dialogos.aviso(this, "La fecha de nacimiento no es realista: la edad no puede superar "
                        + Validador.EDAD_MAXIMA + " años.");
                txtFecha.requestFocusInWindow();
                return null;
            }
        }
        return new Paciente(id, ident, nombre, tel, correo, fecha);
    }

    private void errorBD(SQLException e, String identificacion) {
        if (e.getErrorCode() == 1062) {
            Dialogos.aviso(this, "Ya existe un paciente registrado con la identificación " + identificacion + ".");
        } else if (e.getErrorCode() == 1451) {
            Dialogos.aviso(this, "No se puede eliminar el paciente porque tiene citas asociadas.");
        } else {
            Dialogos.error(this, "Ocurrió un error en la base de datos:\n" + e.getMessage());
        }
    }
}
