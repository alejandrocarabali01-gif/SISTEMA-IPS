package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static final String URL =
            "jdbc:mysql://127.0.0.1:3307/ips_unicatolica";

    private static final String USUARIO = "root";

    // Después configuraremos la contraseña de forma segura.
    private static final String CLAVE = "Sandra1224$";

    public static Connection conectar() {
        try {
            Connection conexion = DriverManager.getConnection(
                    URL, USUARIO, CLAVE
            );

            System.out.println("Conexion exitosa con MySQL");
            return conexion;

        } catch (SQLException e) {
            System.out.println(
                    "Error al conectar con MySQL: " + e.getMessage()
            );
            return null;
        }
    }
}