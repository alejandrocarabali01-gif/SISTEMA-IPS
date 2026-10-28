package modelo;

import java.time.LocalDate;

public record Paciente(int id, String identificacion, String nombreCompleto,
                       String telefono, String correo, LocalDate fechaNacimiento) {
}
