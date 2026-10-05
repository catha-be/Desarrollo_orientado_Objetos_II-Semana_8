package dao;

import model.EstadoPedido;
import model.Pedido;
import model.TipoPedido;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// CRUD de la tabla pedidos, con filtros opcionales por estado y tipo.
public class PedidoDAO {

    private static final String SQL_INSERT = "INSERT INTO pedidos (direccion, tipo, estado) VALUES (?, ?, ?)";
    // Los filtros null se ignoran: (? IS NULL OR columna = ?)
    private static final String SQL_SELECT =
            "SELECT id, direccion, tipo, estado FROM pedidos "
                    + "WHERE (? IS NULL OR estado = ?) AND (? IS NULL OR tipo = ?) ORDER BY id";
    private static final String SQL_UPDATE = "UPDATE pedidos SET direccion = ?, tipo = ?, estado = ? WHERE id = ?";
    private static final String SQL_DELETE = "DELETE FROM pedidos WHERE id = ?";

    /** Inserta el pedido y le asigna el ID generado por la BD. */
    public void create(Pedido p) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet claves = null;
        try {
            con = ConexionDB.conectar();
            ps = con.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, p.getDireccion());
            ps.setString(2, p.getTipo().name());
            ps.setString(3, p.getEstado().name());
            ps.executeUpdate();
            claves = ps.getGeneratedKeys();
            if (claves.next()) p.setId(claves.getInt(1));
        } catch (SQLException e) {
            throw new DatosException("No se pudo registrar el pedido: " + e.getMessage(), e);
        } finally {
            ConexionDB.cerrar(claves);
            ConexionDB.cerrar(ps);
            ConexionDB.cerrar(con);
        }
    }

    /** Lista todos los pedidos. */
    public List<Pedido> readAll() {
        return readFiltrado(null, null);
    }

    /** Lista pedidos filtrando opcionalmente por estado y/o tipo (null = sin filtro). */
    public List<Pedido> readFiltrado(EstadoPedido estado, TipoPedido tipo) {
        List<Pedido> lista = new ArrayList<>();
        String e = estado == null ? null : estado.name();
        String t = tipo == null ? null : tipo.name();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = ConexionDB.conectar();
            ps = con.prepareStatement(SQL_SELECT);
            ps.setString(1, e);
            ps.setString(2, e);
            ps.setString(3, t);
            ps.setString(4, t);
            rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(new Pedido(rs.getInt("id"), rs.getString("direccion"),
                        TipoPedido.valueOf(rs.getString("tipo")),
                        EstadoPedido.valueOf(rs.getString("estado"))));
            }
            return lista;
        } catch (SQLException ex) {
            throw new DatosException("No se pudieron listar los pedidos: " + ex.getMessage(), ex);
        } finally {
            ConexionDB.cerrar(rs);
            ConexionDB.cerrar(ps);
            ConexionDB.cerrar(con);
        }
    }

    /** Actualiza dirección, tipo y estado del pedido. */
    public void update(Pedido p) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = ConexionDB.conectar();
            ps = con.prepareStatement(SQL_UPDATE);
            ps.setString(1, p.getDireccion());
            ps.setString(2, p.getTipo().name());
            ps.setString(3, p.getEstado().name());
            ps.setInt(4, p.getId());
            if (ps.executeUpdate() == 0) {
                throw new DatosException("El pedido " + p.getId() + " ya no existe.", null);
            }
        } catch (SQLException e) {
            throw new DatosException("No se pudo actualizar el pedido: " + e.getMessage(), e);
        } finally {
            ConexionDB.cerrar(ps);
            ConexionDB.cerrar(con);
        }
    }

    /** Elimina el pedido; falla con mensaje claro si tiene entregas asociadas. */
    public void delete(int id) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = ConexionDB.conectar();
            ps = con.prepareStatement(SQL_DELETE);
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new DatosException("El pedido " + id + " ya no existe.", null);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DatosException("No se puede eliminar: el pedido tiene entregas asociadas.", e);
        } catch (SQLException e) {
            throw new DatosException("No se pudo eliminar el pedido: " + e.getMessage(), e);
        } finally {
            ConexionDB.cerrar(ps);
            ConexionDB.cerrar(con);
        }
    }
}
