package view;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

// Utilidades compartidas por los paneles: validaciones de entrada y mensajes.
public final class Validador {

    private Validador() {
    }

    public static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "SpeedFast - Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void aviso(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "SpeedFast", JOptionPane.WARNING_MESSAGE);
    }

    public static void exito(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "SpeedFast", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirmar(Component padre, String mensaje) {
        return JOptionPane.showConfirmDialog(padre, mensaje, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Campo obligatorio: no vacío y con largo máximo. Devuelve el texto limpio o null si es inválido. */
    public static String textoObligatorio(Component padre, String valor, String campo, int maxLargo) {
        String limpio = valor == null ? "" : valor.trim();
        if (limpio.isEmpty()) {
            aviso(padre, "El campo \"" + campo + "\" es obligatorio.");
            return null;
        }
        if (limpio.length() > maxLargo) {
            aviso(padre, "El campo \"" + campo + "\" admite como máximo " + maxLargo + " caracteres.");
            return null;
        }
        return limpio;
    }

    /** Fecha con formato yyyy-MM-dd; null si es inválida. */
    public static LocalDate fecha(Component padre, String valor) {
        try {
            return LocalDate.parse(valor.trim());
        } catch (DateTimeParseException e) {
            aviso(padre, "La fecha debe tener el formato AAAA-MM-DD (ej. 2026-09-29).");
            return null;
        }
    }

    /** Hora con formato HH:mm o HH:mm:ss; null si es inválida. */
    public static LocalTime hora(Component padre, String valor) {
        try {
            return LocalTime.parse(valor.trim());
        } catch (DateTimeParseException e) {
            aviso(padre, "La hora debe tener el formato HH:mm (ej. 14:30).");
            return null;
        }
    }
}
