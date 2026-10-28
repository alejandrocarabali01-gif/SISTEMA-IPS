package conexion;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Clase responsable de gestionar la conexión con MySQL mediante lectura
 * dinámica de un archivo de propiedades externo (config.properties).
 */
public class ConexionDB {

    private static String host = "localhost";
    private static String puerto = "3306";
    private static String baseDatos = "ips_unicatolica";
    private static String usuario = "TuUsuarioDeBaseDeDatos";
    private static String clave = "TuClaveDeBaseDeDatos";

    static {
        // Cargar parámetros de conexión desde el archivo config.properties en la raíz de ejecución
        try (InputStream input = new FileInputStream("config.properties")) {
            Properties prop = new Properties();
            prop.load(input);

            host = prop.getProperty("db.host", host);
            puerto = prop.getProperty("db.puerto", puerto);
            baseDatos = prop.getProperty("db.nombre", baseDatos);
            usuario = prop.getProperty("db.usuario", usuario);
            clave = prop.getProperty("db.clave", clave);
        } catch (Exception e) {
            System.out.println("No se encontró config.properties, utilizando valores por defecto (" + host + ":" + puerto + ").");
        }
    }

    /**
     * Abre y devuelve una nueva conexión con la base de datos MySQL.
     */
    public static Connection getConexion() throws SQLException {
        String url = "jdbc:mysql://" + host + ":" + puerto + "/" + baseDatos
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota";
        return DriverManager.getConnection(url, usuario, clave);
    }
}