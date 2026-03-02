package dev.jgunsett.inmobiliaria.domain.enums;

public enum PropertyType {
	
    HOUSE("Casa"),
    APARTMENT("Departamento"),
    CONDO("PH / Condominio"),
    DUPLEX("Dúplex"),
    TRIPLEX("Tríplex"),
    LOFT("Loft"),

    COMMERCIAL_STORE("Local comercial"),
    OFFICE("Oficina"),

    WAREHOUSE("Galpón"),
    STORAGE("Depósito"),
    INDUSTRIAL_BUILDING("Nave industrial"),

    LAND("Terreno"),
    LOT("Lote"),
    PARCEL("Fracción"),

    COUNTRY_HOUSE("Quinta"),
    CHALET("Chalet"),
    CABIN("Cabaña"),
    FARM("Campo"),
    RANCH("Estancia"),

    GARAGE("Cochera"),
    BUILDING("Edificio"),
    HOTEL("Hotel"),
    CLINIC_OFFICE("Consultorio"),

    OTHER("Otro");

    private final String description;

    PropertyType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
