package conexion;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Prueba minima: se conecta a MySQL y lista los pacientes.
 * Ejecutar con clic derecho -> Run File. Se puede borrar despues.
 */
public class PruebaConexion {

    public static void main(String[] args) {
        try (Connection con = ConexionDB.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT id, identificacion, nombre_completo FROM pacientes")) {

            System.out.println("Conexion exitosa");
            while (rs.next()) {
                System.out.println(rs.getInt("id") + " | "
                        + rs.getString("identificacion") + " | "
                        + rs.getString("nombre_completo"));
            }

        } catch (SQLException e) {
            System.out.println("Error de conexion: " + e.getMessage());
        }
    }
}