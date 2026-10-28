package vista;

/** Elemento de un Combo que guarda el id de la base de datos y el texto visible. */
public record Opcion(int id, String texto) {
    @Override
    public String toString() {
        return texto;
    }
}
