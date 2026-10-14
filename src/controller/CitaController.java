package controller;

import dao.CitaDAO;
import model.Cita;

public class CitaController {

    private final CitaDAO citaDAO = new CitaDAO();

    public void registrarCita(Cita cita) {
        if (cita == null) {
            System.out.println("Los datos de la cita son obligatorios.");
            return;
        }

        if (cita.getIdPaciente() <= 0 || cita.getIdMedico() <= 0) {
            System.out.println("Debes seleccionar un paciente y un medico validos.");
            return;
        }

        if (cita.getFecha() == null || cita.getFecha().trim().isEmpty()) {
            System.out.println("La fecha de la cita es obligatoria.");
            return;
        }

        if (cita.getHora() == null || cita.getHora().trim().isEmpty()) {
            System.out.println("La hora de la cita es obligatoria.");
            return;
        }

        citaDAO.registrarCita(cita);
    }

    public void listarCitas() {
        citaDAO.listarCitas();
    }

    public void cancelarCita(int idCita) {
        if (idCita <= 0) {
            System.out.println("El ID de la cita no es valido.");
            return;
        }

        citaDAO.cancelarCita(idCita);
    }
}