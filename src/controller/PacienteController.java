package controller;

import dao.PacienteDAO;
import model.Paciente;

public class PacienteController {

    private final PacienteDAO pacienteDAO = new PacienteDAO();

    public boolean registrarPaciente(Paciente paciente) {
        if (paciente == null) {
            System.out.println("El paciente no puede estar vacio.");
            return false;
        }

        if (paciente.getIdentificacion() == null
                || paciente.getIdentificacion().trim().isEmpty()) {
            System.out.println("La identificacion es obligatoria.");
            return false;
        }

        if (paciente.getNombre() == null
                || paciente.getNombre().trim().isEmpty()) {
            System.out.println("El nombre es obligatorio.");
            return false;
        }

        return pacienteDAO.registrarPaciente(
                paciente.getIdentificacion(),
                paciente.getNombre(),
                paciente.getTelefono(),
                paciente.getCorreo(),
                paciente.getFechaNacimiento()
        );
    }

    public void listarPacientes() {
        pacienteDAO.listarPacientes();
    }
}