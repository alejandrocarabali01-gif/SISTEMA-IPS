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
            ordenarIds(con);
        }
    }

    public void actualizar(Cita c) throws SQLException {
        String sql = "UPDATE cita SET id_paciente = ?, id_medico = ?, fecha = ?, hora = ?, motivo = ? WHERE id = ?";
        try (Connection con = ConexionDB.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, c.idPaciente());
            ps.setInt(2, c.idMedico());
            ps.setDate(3, Date.valueOf(c.fecha()));
            ps.setTime(4, Time.valueOf(c.hora()));
            ps.setString(5, c.motivo());
            ps.setInt(6, c.id());
            ps.executeUpdate();
            ordenarIds(con);
        }
    }

    /** Renumera los IDs siguiendo el orden de la agenda (fecha y hora). */
    private void ordenarIds(Connection con) throws SQLException {
        try (CallableStatement cs = con.prepareCall("{CALL sp_renumerar_citas()}")) {
            cs.execute();
        }
    }

    /** Cancelar (RF12) o marcar atendida (RF13). Usar las constantes de EstadoCita. */
    public void cambiarEstado(int id, String estado) throws SQLException {
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement("UPDATE cita SET estado = ? WHERE id = ?")) {
            ps.setString(1, estado);
            ps.setInt(2, id);
            ps.executeUpdate();
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
}