package eu.city4age.dashboard.api.model;

// default package
// Generated 24-Nov-2016 15:43:47 by Hibernate Tools 5.2.0.Beta1

import java.util.Date;

/**
 * AssessmentAudienceRole generated by hbm2java
 */
public class AssessmentAudienceRole extends AbstractBaseEntity {

	private Assessment assessment;
	private CdRole cdRole;
	private Date assigned;

	public AssessmentAudienceRole() {
	}

	public AssessmentAudienceRole(Assessment assessment, CdRole cdRole, Date assigned) {
		this.assessment = assessment;
		this.cdRole = cdRole;
		this.assigned = assigned;
	}

	public Assessment getAssessment() {
		return this.assessment;
	}

	public void setAssessment(Assessment assessment) {
		this.assessment = assessment;
	}

	public CdRole getCdRole() {
		return this.cdRole;
	}

	public void setCdRole(CdRole cdRole) {
		this.cdRole = cdRole;
	}

	public Date getAssigned() {
		return this.assigned;
	}

	public void setAssigned(Date assigned) {
		this.assigned = assigned;
	}

}
