package com.batch.domain.enumeration;

public enum Gender {

    MS, MR;

    private static final String ELECTRO_DEPOT_MADAME = "Madame";
    private static final String ELECTRO_DEPOT_MONSIEUR = "Monsieur";

    public static String toChronopostCivility(final Gender gender) {
        if (gender == null) {
            return "M";
        }
        if (gender.equals(Gender.MR)) {
            return "M";
        } else if (gender.equals(Gender.MS)) {
            return "E";
        } else {
            return "L";
        }
    }
}
