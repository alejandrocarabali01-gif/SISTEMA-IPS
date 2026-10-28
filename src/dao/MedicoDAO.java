package dao;

import conexion.ConexionDB;
import modelo.Medico;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Acceso a datos de la tabla medico (RF05 - RF08). */
public class MedicoDAO {

    public void insertar(Medico m) throws SQLException {
        String sql = "INSERT INTO medico (identificacion, nombre_completo, especialidad, telefono, correo) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, m.identificacion());
            ps.setString(2, m.nombreCompleto());
            ps.setString(3, m.especialidad());
            ps.setString(4, m.telefono());
            ps.setString(5, m.correo());
            ps.executeUpdate();
            renumerar(con);
        }
    }

    public void actualizar(Medico m) throws SQLException {
        String sql = "UPDATE medico SET identificacion = ?, nombre_completo = ?, especialidad = ?, "
                + "telefono = ?, correo = ? WHERE id = ?";
        try (Connection con = ConexionDB.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, m.identificacion());
            ps.setString(2, m.nombreCompleto());
            ps.setString(3, m.especialidad());
            ps.setString(4, m.telefono());
            ps.setString(5, m.correo());
            ps.setInt(6, m.id());
            ps.executeUpdate();
        }
    }

    /** Elimina y renumera los IDs (procedimiento sp_eliminar_medico). */
    public void eliminar(int id) throws SQLException {
        try (Connection con = ConexionDB.getConexion();
             CallableStatement cs = con.prepareCall("{CALL sp_eliminar_medico(?)}")) {
            cs.setInt(1, id);
            cs.execute();
        }
    }

    /** Busca por nombre o identificacion. Texto vacio = todos. */
    public List<Medico> buscar(String texto) throws SQLException {
        String sql = "SELECT id, identificacion, nombre_completo, especialidad, telefono, correo "
                + "FROM medico WHERE identificacion LIKE ? OR nombre_completo LIKE ? ORDER BY id";
        List<Medico> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            String patron = "%" + texto + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Medico(rs.getInt("id"), rs.getString("identificacion"),
                            rs.getString("nombre_completo"), rs.getString("especialidad"),
                            rs.getString("telefono"), rs.getString("correo")));
                }
            }
        }
        return lista;
    }

    public List<Medico> listar() throws SQLException {
        return buscar("");
    }

    /**
     * Un INSERT fallido (por ejemplo, una identificacion duplicada) consume un numero del
     * autoincremental y dejaria un salto en los IDs. Tras cada guardado exitoso se renumera
     * para que los IDs queden consecutivos.
     */
    private void renumerar(Connection con) throws SQLException {
        try (CallableStatement cs = con.prepareCall("{CALL sp_renumerar_medicos()}")) {
            cs.execute();
        }
    }
}
