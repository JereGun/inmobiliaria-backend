package dev.jgunsett.inmobiliaria.domain.enums;

public enum AdjustmentFrequency {

    MONTHLY(1),
    QUARTERLY(3),
    SEMI_ANNUAL(6),
    ANNUAL(12);

    private final int months;

    AdjustmentFrequency(int months) {
        this.months = months;
    }

    public int getMonths() {
        return months;
    }
}