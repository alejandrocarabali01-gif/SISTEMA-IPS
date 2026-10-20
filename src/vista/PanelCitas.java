package vista;

import dao.CitaDAO;
import dao.MedicoDAO;
import dao.PacienteDAO;
import modelo.Cita;
import modelo.EstadoCita;
import modelo.Medico;
import modelo.Paciente;
import util.Validador;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/** Pantalla "Programar citas médicas" (RF09 - RF13). */
public class PanelCitas extends JPanel implements Refrescable {
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA =
            DateTimeFormatter.ofPattern("h:mm a", Locale.forLanguageTag("es-CO"));

    private final CitaDAO citaDao = new CitaDAO();
    private final PacienteDAO pacienteDao = new PacienteDAO();
    private final MedicoDAO medicoDao = new MedicoDAO();
    private final Consumer<String> mensaje;

    private final Combo cmbPaciente = new Combo("Seleccione un paciente");
    private final Combo cmbMedico = new Combo("Seleccione un médico");
    private final CampoFecha txtFecha = new CampoFecha();
    private final Combo cmbHora = new Combo("Seleccione la hora");
    private final CampoTexto txtMotivo = new CampoTexto("Ingrese el motivo de la consulta");
    private final Chip chipEstado = new Chip(true);

    private final Combo cmbFiltroEstado = new Combo("Todos", false);
    private final CampoFecha txtFiltroFecha = new CampoFecha();

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Fecha", "Hora", "Paciente", "Médico", "Motivo", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);

    private List<Cita> datos = new ArrayList<>();
    private Integer seleccionado = null;
    private String estadoSeleccionado = null;
    private LocalDate fechaSeleccionada = null;
    private LocalTime horaPropia = null;
    private boolean ajustando = false;

    /** Hora de la lista desplegable; si esta ocupada se muestra pero no se puede elegir. */
    private record Franja(LocalTime hora, boolean ocupada, boolean pasada) implements Deshabilitable {
        @Override
        public boolean deshabilitado() {
            return ocupada || pasada;
        }

        @Override
        public String toString() {
            return FORMATO_HORA.format(hora) + (ocupada ? "   (ocupada)" : pasada ? "   (no disponible)" : "");
        }
    }

    public PanelCitas(Consumer<String> mensaje) {
        super(new BorderLayout());
        this.mensaje = mensaje;
        cmbFiltroEstado.reiniciar(List.of("Programada", "Atendida", "Cancelada"));
        chipEstado.setEstado(EstadoCita.PROGRAMADA);

        JPanel caja = Tema.caja();
        Tema.agregar(caja, Tema.encabezadoPagina("Programar citas médicas",
                "Organice la agenda y actualice el estado de cada cita.", "Agenda médica"), 16);
        Tema.agregar(caja, tarjetaFormulario(), 16);
        Tema.agregar(caja, tarjetaAgenda(), 0);
        add(Tema.pagina(caja), BorderLayout.CENTER);
        txtFecha.setLimites(true, false);
        txtFecha.setMaximoUnAnio(true);

        tabla.getColumnModel().getColumn(6).setCellRenderer(new RenderEstado());
        tabla.getColumnModel().getColumn(0).setMaxWidth(60);

        // Cuando cambia el medico o la fecha se recalculan las horas ocupadas
        cmbMedico.addActionListener(e -> {
            if (!ajustando) {
                recargarHoras();
            }
        });
        Tema.alCambiar(txtFecha, () -> {
            if (!ajustando && txtFecha.getText().length() == 10) {
                recargarHoras();
            }
        });
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || ajustando) {
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
        t.add(Tema.tituloTarjeta("Programar / editar cita", "* Campos obligatorios"), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 4));
        centro.setOpaque(false);

        JPanel formulario = new JPanel(new BorderLayout());
        formulario.setOpaque(false);
        formulario.add(Tema.grilla(new JComponent[][]{
                {Tema.campo("Paciente *", cmbPaciente), Tema.campo("Fecha *", txtFecha)},
                {Tema.campo("Médico *", cmbMedico), Tema.campo("Hora *", cmbHora)},
                {Tema.campo("Motivo", txtMotivo)}
        }), BorderLayout.NORTH);

        JPanel estado = Tema.fila(10, Tema.texto("Estado", Font.BOLD, 11, Tema.TEXTO), chipEstado,
                Tema.texto("Se asigna automáticamente al crear la cita.", Font.PLAIN, 11, Tema.TEXTO_SUAVE));
        estado.setBorder(new EmptyBorder(0, 0, 14, 0));
        formulario.add(estado, BorderLayout.SOUTH);
        centro.add(formulario, BorderLayout.NORTH);

        Boton nuevo = new Boton("Nuevo", Boton.Estilo.BLANCO, Iconos.Tipo.MAS);
        Boton guardar = new Boton("Guardar", Boton.Estilo.VERDE, Iconos.Tipo.DISCO);
        Boton modificar = new Boton("Modificar", Boton.Estilo.AZUL, Iconos.Tipo.LAPIZ);
        Boton cancelar = new Boton("Cancelar cita", Boton.Estilo.ROJO, Iconos.Tipo.EQUIS);
        Boton atendida = new Boton("Cita atendida", Boton.Estilo.BLANCO, Iconos.Tipo.CHECK);
        nuevo.addActionListener(e -> nuevo());
        guardar.addActionListener(e -> guardar());
        modificar.addActionListener(e -> modificar());
        cancelar.addActionListener(e -> cancelarCita());
        atendida.addActionListener(e -> marcarAtendida());
        JPanel botones = Tema.fila(nuevo, guardar, modificar, cancelar, atendida);
        botones.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, Tema.BORDE), new EmptyBorder(14, 0, 0, 0)));
        centro.add(botones, BorderLayout.SOUTH);
        t.add(centro, BorderLayout.CENTER);
        return t;
    }

    private Tarjeta tarjetaAgenda() {
        Tarjeta t = new Tarjeta();
        JPanel norte = new JPanel();
        norte.setOpaque(false);
        norte.setLayout(new BoxLayout(norte, BoxLayout.Y_AXIS));
        JPanel titulo = Tema.tituloTarjeta("Agenda de citas", null);
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        norte.add(titulo);

        cmbFiltroEstado.setPreferredSize(new java.awt.Dimension(170, 34));
        txtFiltroFecha.setPreferredSize(new java.awt.Dimension(170, 34));
        Boton buscar = new Boton("Buscar", Boton.Estilo.VERDE, Iconos.Tipo.LUPA);
        buscar.addActionListener(e -> cargarAgenda());
        JPanel botonAlineado = new JPanel(new BorderLayout());
        botonAlineado.setOpaque(false);
        botonAlineado.setBorder(new EmptyBorder(19, 0, 0, 0));
        botonAlineado.add(buscar);

        JPanel filtros = Tema.fila(12, Tema.campo("Filtrar por estado", cmbFiltroEstado),
                Tema.campo("Filtrar fecha", txtFiltroFecha), botonAlineado);
        filtros.setBorder(new EmptyBorder(0, 0, 12, 0));
        filtros.setAlignmentX(LEFT_ALIGNMENT);
        norte.add(filtros);
        t.add(norte, BorderLayout.NORTH);

        t.add(Tema.scrollTabla(tabla, 200), BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        pie.setBorder(new EmptyBorder(10, 0, 0, 0));
        pie.add(Tema.texto("Agenda de citas médicas", Font.PLAIN, 11, Tema.TEXTO_SUAVE), BorderLayout.WEST);
        pie.add(Tema.texto("Página 1", Font.PLAIN, 11, Tema.TEXTO_SUAVE), BorderLayout.EAST);
        t.add(pie, BorderLayout.SOUTH);
        return t;
    }

    /** Dibuja el estado de la agenda como una etiqueta de color. */
    private static class RenderEstado extends JPanel implements TableCellRenderer {
        private final Chip chip = new Chip(true);

        RenderEstado() {
            super(new FlowLayout(FlowLayout.LEFT, 10, 5));
            add(chip);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean sel,
                                                       boolean foco, int fila, int col) {
            chip.setEstado(String.valueOf(valor));
            setBackground(sel ? tabla.getSelectionBackground() : tabla.getBackground());
            return this;
        }
    }

    // ------------------------------------------------------------------ datos

    /** Recarga pacientes, medicos y agenda desde MySQL (se llama al abrir la pestaña). */
    @Override
    public void refrescar() {
        Opcion pacienteActual = cmbPaciente.opcion();
        Opcion medicoActual = cmbMedico.opcion();
        try {
            List<Opcion> pacientes = new ArrayList<>();
            for (Paciente p : pacienteDao.listar()) {
                pacientes.add(new Opcion(p.id(), p.nombreCompleto() + " (" + p.identificacion() + ")"));
            }
            List<Opcion> medicos = new ArrayList<>();
            for (Medico m : medicoDao.listar()) {
                medicos.add(new Opcion(m.id(), m.nombreCompleto() + " - " + m.especialidad()));
            }
            ajustando = true;
            cmbPaciente.reiniciar(pacientes);
            cmbMedico.reiniciar(medicos);
            if (pacienteActual != null) {
                cmbPaciente.seleccionarPorTexto(pacienteActual.texto());
            }
            if (medicoActual != null) {
                cmbMedico.seleccionarPorTexto(medicoActual.texto());
            }
            ajustando = false;
        } catch (SQLException e) {
            ajustando = false;
            Dialogos.error(this, "No se pudieron cargar pacientes y médicos:\n" + e.getMessage());
        }
        recargarHoras();
        cargarAgenda();
    }

    private void cargarAgenda() {
        String estado = cmbFiltroEstado.haySeleccion()
                ? cmbFiltroEstado.getSelectedItem().toString().toUpperCase() : null;
        LocalDate fecha = null;
        if (!txtFiltroFecha.getText().isBlank()) {
            fecha = txtFiltroFecha.getFecha();
            if (fecha == null) {
                Dialogos.aviso(this, "La fecha del filtro no es válida. Use el formato dd/mm/aaaa.");
                return;
            }
        }
        try {
            datos = citaDao.listar(estado, fecha);
        } catch (SQLException e) {
            Dialogos.error(this, "No se pudo consultar la agenda:\n" + e.getMessage());
            return;
        }
        ajustando = true;
        modelo.setRowCount(0);
        for (Cita c : datos) {
            modelo.addRow(new Object[]{c.id(), c.fecha().format(FORMATO_FECHA), FORMATO_HORA.format(c.hora()),
                c.nombrePaciente(), c.nombreMedico(), c.motivo(), c.estado()});
        }
        ajustando = false;
        if (datos.isEmpty()) {
            mensaje.accept("No hay citas para los filtros seleccionados.");
        }
    }

    /** Muestra las horas del medico en esa fecha y marca como ocupadas las que ya tienen cita (RN04, RN06). */
    private void recargarHoras() {
        LocalTime previa = cmbHora.getSelectedItem() instanceof Franja f ? f.hora() : null;
        Opcion medico = cmbMedico.opcion();
        LocalDate fecha = txtFecha.getFecha();
        Set<LocalTime> ocupadas = Set.of();
        if (medico != null && fecha != null) {
            try {
                ocupadas = citaDao.horasOcupadas(medico.id(), fecha, seleccionado == null ? -1 : seleccionado);
            } catch (SQLException e) {
                Dialogos.error(this, "No se pudo consultar la disponibilidad:\n" + e.getMessage());
            }
        }
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();
        List<Franja> franjas = new ArrayList<>();
        for (LocalTime h = LocalTime.of(8, 0); !h.isAfter(LocalTime.of(17, 0)); h = h.plusMinutes(30)) {
            boolean propia = h.equals(horaPropia);
            boolean pasada = !propia && fecha != null
                    && (fecha.isBefore(hoy) || (fecha.equals(hoy) && h.isBefore(ahora))
                        || fecha.isAfter(hoy.plusYears(1)));
            franjas.add(new Franja(h, !propia && ocupadas.contains(h), pasada));
        }
        boolean anterior = ajustando;
        ajustando = true;
        cmbHora.reiniciar(franjas);
        if (previa != null) {
            for (int i = 1; i < cmbHora.getItemCount(); i++) {
                Franja f = (Franja) cmbHora.getItemAt(i);
                if (f.hora().equals(previa) && !f.deshabilitado()) {
                    cmbHora.setSelectedIndex(i);
                }
            }
        }
        ajustando = anterior;
        if (fecha != null && fecha.isBefore(hoy)) {
            mensaje.accept("No se pueden programar citas en fechas pasadas. Elija hoy o una fecha futura.");
        } else if (fecha != null && fecha.isAfter(hoy.plusYears(1))) {
            mensaje.accept("Solo se pueden programar citas hasta un año hacia adelante.");
        } else if (!ocupadas.isEmpty()) {
            mensaje.accept("Las horas marcadas como ocupadas no están disponibles para ese médico en esa fecha.");
        }
    }

    // ------------------------------------------------------------------ acciones

    private void nuevo() {
        limpiar();
        mensaje.accept("Formulario listo para programar una nueva cita.");
    }

    private void limpiar() {
        ajustando = true;
        seleccionado = null;
        estadoSeleccionado = null;
        fechaSeleccionada = null;
        horaPropia = null;
        cmbPaciente.limpiar();
        cmbMedico.limpiar();
        txtFecha.setText("");
        txtMotivo.setText("");
        chipEstado.setEstado(EstadoCita.PROGRAMADA);
        tabla.clearSelection();
        ajustando = false;
        recargarHoras();
    }

    private void guardar() {
        if (seleccionado != null) {
            Dialogos.aviso(this, "Hay una cita seleccionada.\nUse «Modificar» para actualizarla "
                    + "o «Nuevo» para programar otra cita.");
            return;
        }
        Cita c = leer(0);
        if (c == null) {
            return;
        }
        try {
            citaDao.insertar(c);
            limpiar();
            cargarAgenda();
            mensaje.accept("Cita programada correctamente. Su estado es Programada.");
        } catch (SQLException e) {
            errorBD(e);
        }
    }

    private void modificar() {
        if (seleccionado == null) {
            Dialogos.aviso(this, "Seleccione una cita de la agenda para modificarla.");
            return;
        }
        if (!EstadoCita.PROGRAMADA.equals(estadoSeleccionado)) {
            Dialogos.aviso(this, "Solo se pueden modificar las citas en estado Programada.");
            return;
        }
        Cita c = leer(seleccionado);
        if (c == null) {
            return;
        }
        try {
            if (!citaDao.actualizar(c)) {
                Dialogos.aviso(this, "La cita ya no está en estado Programada, por eso no se puede modificar.");
                limpiar();
                cargarAgenda();
                return;
            }
            limpiar();
            cargarAgenda();
            mensaje.accept("Cita modificada correctamente.");
        } catch (SQLException e) {
            errorBD(e);
        }
    }

    private void cancelarCita() {
        if (seleccionado == null) {
            Dialogos.aviso(this, "Seleccione una cita de la agenda para cancelarla.");
            return;
        }
        if (!EstadoCita.PROGRAMADA.equals(estadoSeleccionado)) {
            Dialogos.aviso(this, "Solo se pueden cancelar las citas en estado Programada.");
            return;
        }
        if (!Dialogos.confirmar(this, "¿Desea cancelar la cita seleccionada?")) {
            return;
        }
        cambiarEstado(EstadoCita.CANCELADA, "La cita fue cancelada. Esa hora queda disponible.");
    }

    private void marcarAtendida() {
        if (seleccionado == null) {
            Dialogos.aviso(this, "Seleccione una cita de la agenda para marcarla como atendida.");
            return;
        }
        if (!EstadoCita.PROGRAMADA.equals(estadoSeleccionado)) {
            Dialogos.aviso(this, "Solo las citas en estado Programada pueden marcarse como atendidas.");
            return;
        }
        if (fechaSeleccionada != null && fechaSeleccionada.isAfter(LocalDate.now())) {
            Dialogos.aviso(this, "No se puede marcar como atendida una cita con fecha futura.");
            return;
        }
        if (!Dialogos.confirmar(this, "¿Confirma que la consulta de esta cita ya fue atendida?")) {
            return;
        }
        cambiarEstado(EstadoCita.ATENDIDA, "La cita fue marcada como atendida.");
    }

    private void cambiarEstado(String nuevoEstado, String textoExito) {
        try {
            if (!citaDao.cambiarEstado(seleccionado, nuevoEstado)) {
                Dialogos.aviso(this, "La cita ya no está en estado Programada, por eso no se puede cambiar su estado.");
                limpiar();
                cargarAgenda();
                return;
            }
            limpiar();
            cargarAgenda();
            mensaje.accept(textoExito);
        } catch (SQLException e) {
            Dialogos.error(this, "No se pudo actualizar la cita:\n" + e.getMessage());
        }
    }

    private void cargarFormulario(Cita c) {
        ajustando = true;
        seleccionado = c.id();
        estadoSeleccionado = c.estado();
        fechaSeleccionada = c.fecha();
        horaPropia = c.hora();
        cmbPaciente.seleccionarPorId(c.idPaciente());
        cmbMedico.seleccionarPorId(c.idMedico());
        txtFecha.setFecha(c.fecha());
        txtMotivo.setText(c.motivo());
        chipEstado.setEstado(c.estado());
        ajustando = false;
        recargarHoras();
        ajustando = true;
        for (int i = 1; i < cmbHora.getItemCount(); i++) {
            if (((Franja) cmbHora.getItemAt(i)).hora().equals(c.hora())) {
                cmbHora.setSelectedIndex(i);
            }
        }
        ajustando = false;
        mensaje.accept("Cita seleccionada. Puede modificarla, cancelarla o marcarla como atendida.");
    }

    /** Valida los campos obligatorios (RN03, RN07). Devuelve null si algo falla. */
    private Cita leer(int id) {
        Opcion paciente = cmbPaciente.opcion();
        Opcion medico = cmbMedico.opcion();
        LocalTime hora = cmbHora.getSelectedItem() instanceof Franja f ? f.hora() : null;
        if (paciente == null || medico == null || txtFecha.getText().isBlank() || hora == null) {
            Dialogos.aviso(this, "Complete los campos obligatorios (*): paciente, médico, fecha y hora.");
            return null;
        }
        LocalDate fecha = txtFecha.getFecha();
        if (fecha == null) {
            Dialogos.aviso(this, "La fecha no es válida. Use el formato dd/mm/aaaa.");
            return null;
        }
        if (fecha.isBefore(LocalDate.now())) {
            Dialogos.aviso(this, "No se pueden programar citas en fechas pasadas.\nElija la fecha de hoy o una fecha futura.");
            txtFecha.requestFocusInWindow();
            return null;
        }
        if (fecha.isAfter(LocalDate.now().plusYears(1))) {
            Dialogos.aviso(this, "Solo se pueden programar citas hasta un año hacia adelante (máximo "
                    + LocalDate.now().plusYears(1).format(FORMATO_FECHA) + ").");
            txtFecha.requestFocusInWindow();
            return null;
        }
        if (fecha.equals(LocalDate.now()) && hora.isBefore(LocalTime.now())) {
            Dialogos.aviso(this, "La hora seleccionada ya pasó. Elija una hora posterior a la actual.");
            return null;
        }
        String motivo = txtMotivo.getText().trim();
        if (motivo.length() > 200) {
            Dialogos.aviso(this, "El motivo no puede superar los 200 caracteres.");
            txtMotivo.requestFocusInWindow();
            return null;
        }
        if (!Validador.motivoCaracteresValidos(motivo)) {
            Dialogos.aviso(this, "El motivo solo puede contener letras, números y signos básicos de puntuación "
                    + "(. , ; : ( ) ¿ ? ¡ ! / % & # + - ' \").");
            txtMotivo.requestFocusInWindow();
            return null;
        }
        if (!Validador.motivoLegible(motivo)) {
            Dialogos.aviso(this, "El motivo debe ser legible: escriba al menos 3 letras y evite repetir "
                    + "un mismo carácter 4 o más veces seguidas.");
            txtMotivo.requestFocusInWindow();
            return null;
        }
        return new Cita(id, paciente.id(), medico.id(), paciente.texto(), medico.texto(),
                fecha, hora, motivo, EstadoCita.PROGRAMADA);
    }

    private void errorBD(SQLException e) {
        if (e.getErrorCode() == 1062) {
            Dialogos.aviso(this, "El médico ya tiene una cita en esa fecha y hora.\nElija otra hora disponible.");
            recargarHoras();
        } else {
            Dialogos.error(this, "Ocurrió un error en la base de datos:\n" + e.getMessage());
        }
    }
}
