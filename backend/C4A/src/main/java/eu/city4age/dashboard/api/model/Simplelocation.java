package eu.city4age.dashboard.api.model;

// default package
// Generated 24-Nov-2016 15:43:47 by Hibernate Tools 5.2.0.Beta1

import java.util.HashSet;
import java.util.Set;

/**
 * Simplelocation generated by hbm2java
 */
public class Simplelocation extends AbstractBaseEntity {

	private String simpleLocationName;
	private Set eams = new HashSet(0);

	public Simplelocation() {
	}

	public Simplelocation(String simpleLocationName, Set eams) {
		this.simpleLocationName = simpleLocationName;
		this.eams = eams;
	}

	public String getSimpleLocationName() {
		return this.simpleLocationName;
	}

	public void setSimpleLocationName(String simpleLocationName) {
		this.simpleLocationName = simpleLocationName;
	}

	public Set getEams() {
		return this.eams;
	}

	public void setEams(Set eams) {
		this.eams = eams;
	}

}
