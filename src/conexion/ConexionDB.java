package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase responsable unicamente de abrir la conexion con MySQL.
 * Los DAO (PacienteDAO, MedicoDAO, CitaDAO) la usan para ejecutar consultas.
 */
public class ConexionDB {

    private static final String HOST = "localhost";
    private static final String PUERTO = "3307";
    private static final String BASE_DATOS = "ips_unicatolica";
    private static final String USUARIO = "root";
    private static final String CLAVE = "ingeniero"; 

    private static final String URL = "jdbc:mysql://" + HOST + ":" + PUERTO + "/" + BASE_DATOS
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota";

    /**
     * Abre y devuelve una conexion nueva. Quien la llame debe cerrarla,
     * idealmente con try-with-resources.
     */
    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }
}