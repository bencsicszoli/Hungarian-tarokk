package com.codecool.tarokkgame.constants;

public enum RomanTarokkNumber {

    XII(12), XIII(13), XIV(14), XV(15), XVI(16), XVII(17), XVIII(18), XIX(19), XX(20);

    final int arabicNumber;

    RomanTarokkNumber(int arabicNumber) {
        this.arabicNumber = arabicNumber;
    }

    public static RomanTarokkNumber fromArabicNumber(int arabicNumber) {
        for (RomanTarokkNumber tarokkNumber : RomanTarokkNumber.values()) {
            if (tarokkNumber.arabicNumber == arabicNumber) {
                return tarokkNumber;
            }
        }
        throw new IllegalArgumentException("Invalid arabic number: " + arabicNumber);
    }

    public static int toArabicNumber(String romanNumber) {
        for (RomanTarokkNumber tarokkNumber : RomanTarokkNumber.values()) {
            if (tarokkNumber.toString().equals(romanNumber)) {
                return tarokkNumber.arabicNumber;
            }
        }
        throw new IllegalArgumentException("Invalid roman number: " + romanNumber);
    }
}
