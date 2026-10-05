package view;

import javax.swing.*;
import java.awt.*;

// Ventana principal: una pestaña por entidad (repartidores, pedidos, entregas).
public class VentanaPrincipal extends JFrame {

    private final PanelRepartidores panelRepartidores;
    private final PanelPedidos panelPedidos;
    private final PanelEntregas panelEntregas;

    public VentanaPrincipal() {
        super("SpeedFast - Gestión de pedidos");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 520);
        setLocationRelativeTo(null);

        // Cada panel avisa aquí al cambiar datos para que los combos/tablas relacionados se actualicen
        panelRepartidores = new PanelRepartidores(this::refrescarTodo);
        panelPedidos = new PanelPedidos(this::refrescarTodo);
        panelEntregas = new PanelEntregas(this::refrescarTodo);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Repartidores", panelRepartidores);
        pestanas.addTab("Pedidos", panelPedidos);
        pestanas.addTab("Entregas", panelEntregas);
        add(pestanas, BorderLayout.CENTER);
    }

    private void refrescarTodo() {
        panelRepartidores.refrescar();
        panelPedidos.refrescar();
        panelEntregas.refrescar();
    }
}
