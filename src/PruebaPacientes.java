import dao.PacienteDAO;

public class PruebaPacientes {

    public static void main(String[] args) {

        PacienteDAO pacienteDAO = new PacienteDAO();

        pacienteDAO.listarPacientes();
    }
}