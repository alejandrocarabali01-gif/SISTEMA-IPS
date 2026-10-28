package dao;

import conexion.ConexionDB;
import modelo.Paciente;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** Acceso a datos de la tabla pacientes (RF01 - RF04). */
public class PacienteDAO {

    public void insertar(Paciente p) throws SQLException {
        String sql = "INSERT INTO pacientes (identificacion, nombre_completo, telefono, correo, fecha_nacimiento) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.identificacion());
            ps.setString(2, p.nombreCompleto());
            ps.setString(3, p.telefono());
            ps.setString(4, p.correo());
            asignarFecha(ps, 5, p);
            ps.executeUpdate();
            renumerar(con);
        }
    }

    public void actualizar(Paciente p) throws SQLException {
        String sql = "UPDATE pacientes SET identificacion = ?, nombre_completo = ?, telefono = ?, "
                + "correo = ?, fecha_nacimiento = ? WHERE id = ?";
        try (Connection con = ConexionDB.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.identificacion());
            ps.setString(2, p.nombreCompleto());
            ps.setString(3, p.telefono());
            ps.setString(4, p.correo());
            asignarFecha(ps, 5, p);
            ps.setInt(6, p.id());
            ps.executeUpdate();
        }
    }

    /** Elimina y renumera los IDs (procedimiento sp_eliminar_paciente). */
    public void eliminar(int id) throws SQLException {
        try (Connection con = ConexionDB.getConexion();
             CallableStatement cs = con.prepareCall("{CALL sp_eliminar_paciente(?)}")) {
            cs.setInt(1, id);
            cs.execute();
        }
    }

    /** Busca por nombre o identificacion. Texto vacio = todos. */
    public List<Paciente> buscar(String texto) throws SQLException {
        String sql = "SELECT id, identificacion, nombre_completo, telefono, correo, fecha_nacimiento "
                + "FROM pacientes WHERE identificacion LIKE ? OR nombre_completo LIKE ? ORDER BY id";
        List<Paciente> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            String patron = "%" + texto + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date f = rs.getDate("fecha_nacimiento");
                    lista.add(new Paciente(rs.getInt("id"), rs.getString("identificacion"),
                            rs.getString("nombre_completo"), rs.getString("telefono"),
                            rs.getString("correo"), f == null ? null : f.toLocalDate()));
                }
            }
        }
        return lista;
    }

    public List<Paciente> listar() throws SQLException {
        return buscar("");
    }


    /**
     * Un INSERT fallido (por ejemplo, una identificacion duplicada) consume un numero del
     * autoincremental y dejaria un salto en los IDs. Tras cada guardado exitoso se renumera
     * para que los IDs queden consecutivos.
     */
    private void renumerar(Connection con) throws SQLException {
        try (CallableStatement cs = con.prepareCall("{CALL sp_renumerar_pacientes()}")) {
            cs.execute();
        }
    }

    private void asignarFecha(PreparedStatement ps, int indice, Paciente p) throws SQLException {
        if (p.fechaNacimiento() == null) {
            ps.setNull(indice, Types.DATE);
        } else {
            ps.setDate(indice, Date.valueOf(p.fechaNacimiento()));
        }
    }
}
