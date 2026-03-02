package dev.jgunsett.inmobiliaria.domain.enums;

public enum ContractStatus {
	ACTIVE("Contrato activo"), // Contrato vigente
	FINISHED("Finalizado"), //Finalizo normalmente
	TERMINATED("Rescindido"), // Rescision anticipada
	SUSPENDED("Suspendido"), // Pausado temporalmente
	DRAFT("Borrador"); // Aun no entro en vigencia
	
	private final String label;
	
	ContractStatus(String label){
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}
}
