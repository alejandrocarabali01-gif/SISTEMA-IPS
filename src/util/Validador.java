package util;

import java.time.LocalDate;
import java.time.Period;
import java.util.Set;
import java.util.regex.Pattern;

/** Validaciones de formato y de dominio para los formularios (RN07 a RN18). */
public final class Validador {
    /** Edad maxima aceptada para una fecha de nacimiento. */
    public static final int EDAD_MAXIMA = 120;

    /**
     * Proveedores de correo aceptados (RN11). Para aceptar otro proveedor, agreguelo aqui.
     * Ademas se aceptan correos institucionales que terminen en .edu.co.
     */
    public static final Set<String> DOMINIOS_CORREO = Set.of(
            "gmail.com", "googlemail.com", "hotmail.com", "hotmail.es", "outlook.com", "outlook.es",
            "live.com", "msn.com", "yahoo.com", "yahoo.es", "icloud.com", "me.com",
            "protonmail.com", "proton.me", "unicatolica.edu.co");

    private static final Pattern NOMBRE = Pattern.compile("^\\p{L}+( \\p{L}+)*$");
    private static final Pattern CORREO_LOCAL = Pattern.compile("^[A-Za-z0-9]+([._%+-][A-Za-z0-9]+)*$");
    private static final Pattern DOMINIO_EDU = Pattern.compile("^[a-z0-9-]+(\\.[a-z0-9-]+)*\\.edu\\.co$");
    private static final Pattern MOTIVO_CARACTERES = Pattern.compile("^[\\p{L}\\p{N} .,;:()¿?¡!/%&#+\"'-]*$");
    private static final Pattern REPETIDOS = Pattern.compile("(.)\\1{3,}");

    private Validador() {
    }

    /** Solo digitos, con longitud entre min y max. */
    public static boolean numero(String texto, int min, int max) {
        return texto.matches("\\d{" + min + "," + max + "}");
    }

    /** Solo letras (con tildes y ñ) separadas por un espacio; entre 2 y 100 caracteres. */
    public static boolean nombre(String texto) {
        return texto.length() >= 2 && texto.length() <= 100 && NOMBRE.matcher(texto).matches();
    }

    /** Correo con formato valido y de un proveedor reconocido (RN11). Maximo 100 caracteres. */
    public static boolean correo(String texto) {
        if (texto.length() > 100) {
            return false;
        }
        int arroba = texto.indexOf('@');
        if (arroba <= 0 || arroba != texto.lastIndexOf('@')) {
            return false;
        }
        String local = texto.substring(0, arroba);
        String dominio = texto.substring(arroba + 1).toLowerCase();
        if (!CORREO_LOCAL.matcher(local).matches()) {
            return false;
        }
        return DOMINIOS_CORREO.contains(dominio) || DOMINIO_EDU.matcher(dominio).matches();
    }

    public static boolean noEsFutura(LocalDate fecha) {
        return !fecha.isAfter(LocalDate.now());
    }

    /** La edad calculada no supera EDAD_MAXIMA anos. */
    public static boolean edadRealista(LocalDate nacimiento) {
        return Period.between(nacimiento, LocalDate.now()).getYears() <= EDAD_MAXIMA;
    }

    /** Fecha de cita dentro de la ventana permitida: desde hoy hasta un año hacia adelante (RN15). */
    public static boolean fechaCitaEnRango(LocalDate fecha) {
        LocalDate hoy = LocalDate.now();
        return !fecha.isBefore(hoy) && !fecha.isAfter(hoy.plusYears(1));
    }

    /** El motivo solo usa letras, numeros y puntuacion basica (RN18). Vacio es valido. */
    public static boolean motivoCaracteresValidos(String texto) {
        return MOTIVO_CARACTERES.matcher(texto).matches();
    }

    /** Un motivo no vacio debe ser legible: al menos 3 letras y sin repetir un caracter 4 o mas veces seguidas. */
    public static boolean motivoLegible(String texto) {
        if (texto.isEmpty()) {
            return true;
        }
        long letras = texto.codePoints().filter(Character::isLetter).count();
        return letras >= 3 && !REPETIDOS.matcher(texto).find();
    }
}
