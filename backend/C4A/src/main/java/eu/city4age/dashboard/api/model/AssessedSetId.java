package eu.city4age.dashboard.api.model;

// default package
// Generated 24-Nov-2016 15:43:47 by Hibernate Tools 5.2.0.Beta1

/**
 * AssessedSetId generated by hbm2java
 */
public class AssessedSetId implements java.io.Serializable {

	private int gefValueId;
	private int assessmentId;
	
	public AssessedSetId() {
	}

	public AssessedSetId(int gefValueId, int assessmentId) {
		this.gefValueId = gefValueId;
		this.assessmentId = assessmentId;
	}

	public int getGefValueId() {
		return gefValueId;
	}

	public void setGefValueId(int gefValueId) {
		this.gefValueId = gefValueId;
	}

	public int getAssessmentId() {
		return this.assessmentId;
	}

	public void setAssessmentId(int assessmentId) {
		this.assessmentId = assessmentId;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof AssessedSetId))
			return false;
		AssessedSetId castOther = (AssessedSetId) other;

		return (this.getGefValueId() == castOther.getGefValueId())
				&& ((this.getGefValueId() == castOther.getGefValueId())
				&& (this.getAssessmentId() == castOther.getAssessmentId()));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + this.getGefValueId();
		result = 37 * result + this.getAssessmentId();
		return result;
	}

}
