package dao;

import conexion.ConexionDB;
import modelo.Cita;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Acceso a datos de la tabla cita (RF09 - RF13). */
public class CitaDAO {

    /** RN05: el estado no se envia; MySQL lo deja como PROGRAMADA. */
    public void insertar(Cita c) throws SQLException {
        String sql = "INSERT INTO cita (id_paciente, id_medico, fecha, hora, motivo) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, c.idPaciente());
            ps.setInt(2, c.idMedico());
            ps.setDate(3, Date.valueOf(c.fecha()));
            ps.setTime(4, Time.valueOf(c.hora()));
            ps.setString(5, c.motivo());
            ps.executeUpdate();
            renumerar(con);
        }
    }

    /** Solo se puede modificar una cita PROGRAMADA. Devuelve false si ya no lo esta. */
    public boolean actualizar(Cita c) throws SQLException {
        String sql = "UPDATE cita SET id_paciente = ?, id_medico = ?, fecha = ?, hora = ?, motivo = ? "
                + "WHERE id = ? AND estado = 'PROGRAMADA'";
        try (Connection con = ConexionDB.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, c.idPaciente());
            ps.setInt(2, c.idMedico());
            ps.setDate(3, Date.valueOf(c.fecha()));
            ps.setTime(4, Time.valueOf(c.hora()));
            ps.setString(5, c.motivo());
            ps.setInt(6, c.id());
            boolean modificada = ps.executeUpdate() > 0;
            if (modificada) {
                renumerar(con);
            }
            return modificada;
        }
    }

    /**
     * Cancelar (RF12) o marcar atendida (RF13). Solo se permite el cambio desde PROGRAMADA:
     * una cita Atendida o Cancelada no vuelve a cambiar. Devuelve false si no se pudo cambiar.
     */
    public boolean cambiarEstado(int id, String estado) throws SQLException {
        String sql = "UPDATE cita SET estado = ? WHERE id = ? AND estado = 'PROGRAMADA'";
        try (Connection con = ConexionDB.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    /** Agenda (RF10). estado = null -> todos; fecha = null -> todas. */
    public List<Cita> listar(String estado, LocalDate fecha) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT c.id, c.id_paciente, c.id_medico, p.nombre_completo AS paciente, "
                + "m.nombre_completo AS medico, c.fecha, c.hora, c.motivo, c.estado "
                + "FROM cita c JOIN pacientes p ON p.id = c.id_paciente "
                + "JOIN medico m ON m.id = c.id_medico WHERE 1 = 1");
        if (estado != null) {
            sql.append(" AND c.estado = ?");
        }
        if (fecha != null) {
            sql.append(" AND c.fecha = ?");
        }
        sql.append(" ORDER BY c.fecha, c.hora");

        List<Cita> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getConexion(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int i = 1;
            if (estado != null) {
                ps.setString(i++, estado);
            }
            if (fecha != null) {
                ps.setDate(i, Date.valueOf(fecha));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Cita(rs.getInt("id"), rs.getInt("id_paciente"), rs.getInt("id_medico"),
                            rs.getString("paciente"), rs.getString("medico"),
                            rs.getDate("fecha").toLocalDate(), rs.getTime("hora").toLocalTime(),
                            rs.getString("motivo"), rs.getString("estado")));
                }
            }
        }
        return lista;
    }

    /**
     * Horas ocupadas de un medico en una fecha (RN04). Las citas CANCELADAS no cuentan (RN06).
     * idExcluir: id de la cita que se esta modificando (o -1 si es nueva).
     */
    public Set<LocalTime> horasOcupadas(int idMedico, LocalDate fecha, int idExcluir) throws SQLException {
        String sql = "SELECT hora FROM cita WHERE id_medico = ? AND fecha = ? "
                + "AND estado <> 'CANCELADA' AND id <> ?";
        Set<LocalTime> horas = new HashSet<>();
        try (Connection con = ConexionDB.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMedico);
            ps.setDate(2, Date.valueOf(fecha));
            ps.setInt(3, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    horas.add(rs.getTime(1).toLocalTime());
                }
            }
        }
        return horas;
    }

    /**
     * Los IDs de las citas siguen el orden de la agenda (fecha y hora): la cita mas cercana es la 1.
     * Se renumera tras cada guardado o modificacion, porque ambos pueden cambiar el orden
     * (y un INSERT fallido tambien dejaria saltos en el autoincremental).
     */
    private void renumerar(Connection con) throws SQLException {
        try (CallableStatement cs = con.prepareCall("{CALL sp_renumerar_citas()}")) {
            cs.execute();
        }
    }
}
