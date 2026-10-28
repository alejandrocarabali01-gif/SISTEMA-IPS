package modelo;

import java.time.LocalDate;
import java.time.LocalTime;

public record Cita(int id, int idPaciente, int idMedico, String nombrePaciente,
                   String nombreMedico, LocalDate fecha, LocalTime hora,
                   String motivo, String estado) {
}
