package controller;

import dao.MedicoDAO;
import model.Medico;

public class MedicoController {

    private final MedicoDAO medicoDAO = new MedicoDAO();

    public void registrarMedico(Medico medico) {
        if (medico == null) {
            System.out.println("Los datos del medico son obligatorios.");
            return;
        }

        if (medico.getIdentificacion() == null
                || medico.getIdentificacion().trim().isEmpty()) {
            System.out.println("La identificacion del medico es obligatoria.");
            return;
        }

        if (medico.getNombre() == null
                || medico.getNombre().trim().isEmpty()) {
            System.out.println("El nombre del medico es obligatorio.");
            return;
        }

        medicoDAO.registrarMedico(medico);
    }

    public void listarMedicos() {
        medicoDAO.listarMedicos();
    }
}