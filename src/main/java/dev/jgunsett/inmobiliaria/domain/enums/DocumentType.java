package dev.jgunsett.inmobiliaria.domain.enums;

public enum DocumentType {
	
    DNI("Documento Nacional de Identidad"),
    LE("Libreta de Enrolamiento"),
    LC("Libreta Cívica"),
    CI("Cédula de Identidad"),
    PASAPORTE("Pasaporte"),
    OTRO("Otro");

    private final String descripcion;

    DocumentType(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
