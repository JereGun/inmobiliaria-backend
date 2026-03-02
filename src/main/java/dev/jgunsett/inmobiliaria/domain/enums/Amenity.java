package dev.jgunsett.inmobiliaria.domain.enums;

public enum Amenity {

    // Exterior
    GARDEN("Jardín"),
    TERRACE("Terraza"),
    BALCONY("Balcón"),
    PATIO("Patio"),
    BARBECUE_AREA("Parrilla / Quincho"),
    SWIMMING_POOL("Pileta"),
    SOLARIUM("Solárium"),

    // Parking
    GARAGE("Cochera cubierta"),
    PARKING_LOT("Estacionamiento descubierto"),

    // Building features
    ELEVATOR("Ascensor"),
    SECURITY("Seguridad privada"),
    DOORMAN("Portero"),
    CCTV("Cámaras de seguridad"),
    GYM("Gimnasio"),
    LAUNDRY("Lavadero"),
    STORAGE_ROOM("Baulera"),
    POOL("Piscina"),

    // Climate control
    AIR_CONDITIONING("Aire acondicionado"),
    HEATING("Calefacción"),

    // Technology
    INTERNET("Internet disponible"),
    CABLE_TV("Televisión por cable"),

    // Accessibility
    WHEELCHAIR_ACCESS("Acceso para personas con movilidad reducida");

    private final String description;

    Amenity(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
