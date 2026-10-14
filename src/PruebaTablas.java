import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import config.Conexion;

public class PruebaTablas {

    public static void main(String[] args) {

        try (Connection conexion = Conexion.conectar()) {

            if (conexion == null) {
                System.out.println("No hay conexion con la base de datos.");
                return;
            }

            DatabaseMetaData datos = conexion.getMetaData();

            try (ResultSet tablas = datos.getTables(
                    "ips_unicatolica", null, "%", new String[]{"TABLE"})) {

                System.out.println("Tablas de ips_unicatolica:");

                while (tablas.next()) {
                    System.out.println("- " + tablas.getString("TABLE_NAME"));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}