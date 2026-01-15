package com.racslabs.types;

public record Complex64(float real, float imag) {

    @Override
    public String toString() {
        return String.format("%f+i%f", real, imag);
    }
}
