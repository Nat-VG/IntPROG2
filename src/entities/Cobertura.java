package entities;

// Enum es como una lista fija de opciones que no puede cambiar
// En la base de datos esto se convierte en ENUM('RC', 'TERCEROS', 'TODO_RIESGO')
// Así nos aseguramos que solo se usen estos 3 tipos de cobertura
public enum Cobertura {
    RC,         // Responsabilidad Civil - lo mínimo obligatorio
    TERCEROS,   // Terceros Completo - cubre más cosas
    TODO_RIESGO // Comprehensive - la cobertura más completa
}