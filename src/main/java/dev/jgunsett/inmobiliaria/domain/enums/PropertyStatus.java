package dev.jgunsett.inmobiliaria.domain.enums;

public enum PropertyStatus {
	
	AVAILABLE("Disponible"),
	RESERVED("Reservada"),
	SOLD("Vendida"),
	RENTED("Alquilada"),
	INACTIVE("No publicada");
	
	private final String description;
	
	PropertyStatus(String description){
		this.description = description;
	}
	
    public String getDescription() {
        return description;
    }
}
