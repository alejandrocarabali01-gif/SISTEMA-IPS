package util;

import java.util.regex.Pattern;

/** Validaciones de formato para los formularios (RN07). */
public final class Validador {
    private static final Pattern CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private Validador() {
    }

    /** Solo digitos, con longitud entre min y max. */
    public static boolean numero(String texto, int min, int max) {
        return texto.matches("\\d{" + min + "," + max + "}");
    }

    public static boolean correo(String texto) {
        return CORREO.matcher(texto).matches();
    }
}
