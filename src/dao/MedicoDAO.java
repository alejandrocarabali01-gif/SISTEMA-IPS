package dao;

import config.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.Medico;

public class MedicoDAO {

    public boolean registrarMedico(Medico medico) {

        String sql = "INSERT INTO medicos "
                + "(identificacion, nombre, especialidad, telefono, correo) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.conectar()) {

            if (conexion == null) {
                return false;
            }

            try (PreparedStatement consulta =
                    conexion.prepareStatement(sql)) {

                consulta.setString(1, medico.getIdentificacion());
                consulta.setString(2, medico.getNombre());
                consulta.setString(3, medico.getEspecialidad());
                consulta.setString(4, medico.getTelefono());
                consulta.setString(5, medico.getCorreo());

                return consulta.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            System.out.println(
                    "Error al registrar medico: " + e.getMessage()
            );
            return false;
        }
    }

    public void listarMedicos() {

        String sql = "SELECT id_medico, identificacion, nombre, "
                   + "especialidad, telefono, correo FROM medicos";

        try (Connection conexion = Conexion.conectar()) {

            if (conexion == null) {
                return;
            }

            try (PreparedStatement consulta =
                    conexion.prepareStatement(sql);
                 ResultSet resultado = consulta.executeQuery()) {

                System.out.println("\n--- LISTA DE MEDICOS ---");

                while (resultado.next()) {
                    System.out.println(
                        "ID: " + resultado.getInt("id_medico")
                        + " | Identificacion: "
                        + resultado.getString("identificacion")
                        + " | Nombre: "
                        + resultado.getString("nombre")
                        + " | Especialidad: "
                        + resultado.getString("especialidad")
                        + " | Telefono: "
                        + resultado.getString("telefono")
                        + " | Correo: "
                        + resultado.getString("correo")
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Error al consultar medicos: " + e.getMessage()
            );
        }
    }
}