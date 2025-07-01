package com.veely.model;

public enum JobQualification {
	 	OPERAIO("Operaio"),
	    IPIEGATO("Impiegato"),
	    DIRIGENTE("Dirigente"),
	    ALTRO("Altro");

	    private final String displayName;

	    JobQualification(String displayName) {
	        this.displayName = displayName;
	    }

	    public String getDisplayName() {
	        return displayName;
	    }
}
