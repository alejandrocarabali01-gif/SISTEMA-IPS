package modelo;

/** Estados de una cita. Deben coincidir EXACTAMENTE con el ENUM de MySQL. */
public final class EstadoCita {
    public static final String PROGRAMADA = "PROGRAMADA";
    public static final String ATENDIDA = "ATENDIDA";
    public static final String CANCELADA = "CANCELADA";

    private EstadoCita() {
    }

    /** "PROGRAMADA" -> "Programada" (para mostrar en pantalla). */
    public static String visible(String estado) {
        if (estado == null || estado.isEmpty()) {
            return "";
        }
        return estado.substring(0, 1).toUpperCase() + estado.substring(1).toLowerCase();
    }
}
