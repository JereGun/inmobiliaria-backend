package dev.jgunsett.inmobiliaria.domain.enums;

public enum OperationType {

    SALE("Venta"),
    RENT("Alquiler"),
    TEMPORARY_RENT("Alquiler temporal"),
    LEASE("Arrendamiento"),
    EXCHANGE("Permuta"),
    PRE_SALE("Preventa"),
    OTHER("Otro");

    private final String description;

    OperationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}