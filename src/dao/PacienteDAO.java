package dao;

import config.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PacienteDAO {

    // METODO PARA LISTAR TODOS LOS PACIENTES
    public void listarPacientes() {

        String sql = "SELECT id_paciente, identificacion, nombre, "
                   + "telefono, correo, fecha_nacimiento "
                   + "FROM pacientes";

        try (Connection conexion = Conexion.conectar()) {

            if (conexion == null) {
                System.out.println("No hay conexion con MySQL.");
                return;
            }

            try (PreparedStatement consulta =
                    conexion.prepareStatement(sql);
                 ResultSet resultado = consulta.executeQuery()) {

                System.out.println("\n--- LISTA DE PACIENTES ---");

                while (resultado.next()) {

                    System.out.println(
                        "ID: " + resultado.getInt("id_paciente")
                        + " | Identificacion: "
                        + resultado.getString("identificacion")
                        + " | Nombre: "
                        + resultado.getString("nombre")
                        + " | Telefono: "
                        + resultado.getString("telefono")
                        + " | Correo: "
                        + resultado.getString("correo")
                        + " | Fecha nacimiento: "
                        + resultado.getDate("fecha_nacimiento")
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println(
                "Error al consultar pacientes: " + e.getMessage()
            );
        }
    }

    // METODO PARA REGISTRAR UN PACIENTE
    public boolean registrarPaciente(
            String identificacion,
            String nombre,
            String telefono,
            String correo,
            String fechaNacimiento) {

        String sql = "INSERT INTO pacientes "
                + "(identificacion, nombre, telefono, correo, fecha_nacimiento) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conexion = Conexion.conectar()) {

            if (conexion == null) {
                System.out.println("No hay conexion con MySQL.");
                return false;
            }

            try (PreparedStatement consulta =
                    conexion.prepareStatement(sql)) {

                consulta.setString(1, identificacion);
                consulta.setString(2, nombre);
                consulta.setString(3, telefono);
                consulta.setString(4, correo);
                consulta.setDate(
                    5,
                    java.sql.Date.valueOf(fechaNacimiento)
                );

                int filas = consulta.executeUpdate();

                if (filas > 0) {
                    System.out.println(
                        "Paciente registrado correctamente."
                    );
                    return true;
                }
            }

        } catch (SQLException e) {
            System.out.println(
                "Error al registrar paciente: " + e.getMessage()
            );

        } catch (IllegalArgumentException e) {
            System.out.println(
                "Fecha incorrecta. Usa el formato AAAA-MM-DD."
            );
        }

        return false;
    }
}