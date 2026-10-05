package view;

import dao.DatosException;
import dao.PedidoDAO;
import model.EstadoPedido;
import model.Pedido;
import model.TipoPedido;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

// Pestaña de gestión de pedidos: formulario, filtros opcionales, JTable y CRUD.
public class PanelPedidos extends JPanel {

    private static final String TODOS = "Todos";

    private final PedidoDAO dao = new PedidoDAO();
    private final Runnable alCambiar;

    private final JTextField txtDireccion = new JTextField(25);
    private final JComboBox<TipoPedido> cmbTipo = new JComboBox<>(TipoPedido.values());
    private final JComboBox<EstadoPedido> cmbEstado = new JComboBox<>(EstadoPedido.values());
    private final JComboBox<Object> filtroEstado = new JComboBox<>();
    private final JComboBox<Object> filtroTipo = new JComboBox<>();

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Dirección", "Tipo", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);

    public PanelPedidos(Runnable alCambiar) {
        this.alCambiar = alCambiar;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formulario = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formulario.add(new JLabel("Dirección:"));
        formulario.add(txtDireccion);
        formulario.add(new JLabel("Tipo:"));
        formulario.add(cmbTipo);
        formulario.add(new JLabel("Estado:"));
        formulario.add(cmbEstado);
        JButton btnRegistrar = new JButton("Registrar");
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar");
        formulario.add(btnRegistrar);
        formulario.add(btnEditar);
        formulario.add(btnEliminar);
        formulario.add(btnLimpiar);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtros.add(new JLabel("Filtrar por estado:"));
        filtroEstado.addItem(TODOS);
        for (EstadoPedido e : EstadoPedido.values()) filtroEstado.addItem(e);
        filtros.add(filtroEstado);
        filtros.add(new JLabel("por tipo:"));
        filtroTipo.addItem(TODOS);
        for (TipoPedido t : TipoPedido.values()) filtroTipo.addItem(t);
        filtros.add(filtroTipo);

        JPanel norte = new JPanel(new GridLayout(2, 1));
        norte.add(formulario);
        norte.add(filtros);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) cargarFormulario(tabla.getSelectedRow());
        });

        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        btnRegistrar.addActionListener(e -> registrar());
        btnEditar.addActionListener(e -> editar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());
        filtroEstado.addActionListener(e -> refrescar());
        filtroTipo.addActionListener(e -> refrescar());

        refrescar();
    }

    /** Recarga la tabla aplicando los filtros seleccionados. */
    public void refrescar() {
        try {
            EstadoPedido estado = filtroEstado.getSelectedItem() instanceof EstadoPedido es ? es : null;
            TipoPedido tipo = filtroTipo.getSelectedItem() instanceof TipoPedido t ? t : null;
            modelo.setRowCount(0);
            for (Pedido p : dao.readFiltrado(estado, tipo)) {
                modelo.addRow(new Object[]{p.getId(), p.getDireccion(), p.getTipo(), p.getEstado()});
            }
        } catch (DatosException ex) {
            Validador.error(this, ex.getMessage());
        }
    }

    private void cargarFormulario(int fila) {
        txtDireccion.setText(String.valueOf(modelo.getValueAt(fila, 1)));
        cmbTipo.setSelectedItem(modelo.getValueAt(fila, 2));
        cmbEstado.setSelectedItem(modelo.getValueAt(fila, 3));
    }

    private void registrar() {
        String direccion = Validador.textoObligatorio(this, txtDireccion.getText(), "Dirección", 100);
        if (direccion == null) return;
        try {
            dao.create(new Pedido(direccion, (TipoPedido) cmbTipo.getSelectedItem(),
                    (EstadoPedido) cmbEstado.getSelectedItem()));
            despuesDeCambio("Pedido registrado correctamente.");
        } catch (DatosException ex) {
            Validador.error(this, ex.getMessage());
        }
    }

    private void editar() {
        int id = idSeleccionado("editar");
        if (id < 0) return;
        String direccion = Validador.textoObligatorio(this, txtDireccion.getText(), "Dirección", 100);
        if (direccion == null) return;
        try {
            dao.update(new Pedido(id, direccion, (TipoPedido) cmbTipo.getSelectedItem(),
                    (EstadoPedido) cmbEstado.getSelectedItem()));
            despuesDeCambio("Pedido actualizado correctamente.");
        } catch (DatosException ex) {
            Validador.error(this, ex.getMessage());
        }
    }

    private void eliminar() {
        int id = idSeleccionado("eliminar");
        if (id < 0) return;
        if (!Validador.confirmar(this, "¿Eliminar el pedido " + id + "?")) return;
        try {
            dao.delete(id);
            despuesDeCambio("Pedido eliminado correctamente.");
        } catch (DatosException ex) {
            Validador.error(this, ex.getMessage());
        }
    }

    private int idSeleccionado(String accion) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Validador.aviso(this, "Selecciona un pedido de la tabla para " + accion + ".");
            return -1;
        }
        return (int) modelo.getValueAt(fila, 0);
    }

    private void despuesDeCambio(String mensaje) {
        limpiar();
        alCambiar.run();
        Validador.exito(this, mensaje);
    }

    private void limpiar() {
        txtDireccion.setText("");
        cmbTipo.setSelectedIndex(0);
        cmbEstado.setSelectedIndex(0);
        tabla.clearSelection();
    }
}
