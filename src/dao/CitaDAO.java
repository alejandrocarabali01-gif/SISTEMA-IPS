package dao;

import config.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.Cita;

public class CitaDAO {

    public boolean registrarCita(Cita cita) {

        String sql = "INSERT INTO citas "
                + "(id_paciente, id_medico, fecha, hora, motivo) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.conectar()) {

            if (conexion == null) {
                return false;
            }

            try (PreparedStatement consulta =
                    conexion.prepareStatement(sql)) {

                consulta.setInt(1, cita.getIdPaciente());
                consulta.setInt(2, cita.getIdMedico());
                consulta.setDate(
                    3, java.sql.Date.valueOf(cita.getFecha())
                );
                consulta.setTime(
                    4, java.sql.Time.valueOf(cita.getHora())
                );
                consulta.setString(5, cita.getMotivo());

                return consulta.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            System.out.println(
                    "Error al registrar cita: " + e.getMessage()
            );
            return false;

        } catch (IllegalArgumentException e) {
            System.out.println(
                    "Fecha u hora incorrecta. Usa AAAA-MM-DD y HH:MM:SS."
            );
            return false;
        }
    }

    public void listarCitas() {

        String sql = "SELECT c.id_cita, p.nombre AS paciente, "
                + "m.nombre AS medico, c.fecha, c.hora, "
                + "c.motivo, c.estado "
                + "FROM citas c "
                + "INNER JOIN pacientes p "
                + "ON c.id_paciente = p.id_paciente "
                + "INNER JOIN medicos m "
                + "ON c.id_medico = m.id_medico "
                + "ORDER BY c.fecha, c.hora";

        try (Connection conexion = Conexion.conectar()) {

            if (conexion == null) {
                return;
            }

            try (PreparedStatement consulta =
                    conexion.prepareStatement(sql);
                 ResultSet resultado = consulta.executeQuery()) {

                System.out.println("\n--- LISTA DE CITAS ---");

                while (resultado.next()) {
                    System.out.println(
                        "Cita: " + resultado.getInt("id_cita")
                        + " | Paciente: "
                        + resultado.getString("paciente")
                        + " | Medico: "
                        + resultado.getString("medico")
                        + " | Fecha: "
                        + resultado.getDate("fecha")
                        + " | Hora: "
                        + resultado.getTime("hora")
                        + " | Motivo: "
                        + resultado.getString("motivo")
                        + " | Estado: "
                        + resultado.getString("estado")
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Error al consultar citas: " + e.getMessage()
            );
        }
    }

    public boolean cancelarCita(int idCita) {

        String sql = "UPDATE citas SET estado = 'Cancelada' "
                + "WHERE id_cita = ? AND estado = 'Programada'";

        try (Connection conexion = Conexion.conectar()) {

            if (conexion == null) {
                return false;
            }

            try (PreparedStatement consulta =
                    conexion.prepareStatement(sql)) {

                consulta.setInt(1, idCita);
                return consulta.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            System.out.println(
                    "Error al cancelar cita: " + e.getMessage()
            );
            return false;
        }
    }
}