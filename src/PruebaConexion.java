public class PruebaConexion {

    public static void main(String[] args) {

        if (Conexion.conectar() != null) {
            System.out.println("La base de datos IPS está conectada.");
        } else {
            System.out.println("No se pudo conectar con la base de datos.");
        }

    }
}