import dao.PacienteDAO;

public class PruebaRegistroPaciente {

    public static void main(String[] args) {

        PacienteDAO pacienteDAO = new PacienteDAO();

        pacienteDAO.registrarPaciente(
                "1005551234",
                "Paciente de Prueba",
                "3005551234",
                "prueba.ips@gmail.com",
                "2002-08-15"
        );

        System.out.println("\nConsultando pacientes registrados...");
        pacienteDAO.listarPacientes();
    }
}