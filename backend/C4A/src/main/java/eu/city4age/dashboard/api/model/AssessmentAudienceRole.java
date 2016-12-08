package eu.city4age.dashboard.api.model;

import java.io.Serializable;

// default package
// Generated 24-Nov-2016 15:43:47 by Hibernate Tools 5.2.0.Beta1

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 * AssessmentAudienceRole generated by hbm2java
 */
public class AssessmentAudienceRole implements Serializable {

	private AssessmentAudienceRoleId assessmentAudienceRoleId;
	
	@JsonBackReference
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
	
	public AssessmentAudienceRoleId getAssessmentAudienceRoleId() {
		return assessmentAudienceRoleId;
	}

	public void setAssessmentAudienceRoleId(AssessmentAudienceRoleId assessmentAudienceRoleId) {
		this.assessmentAudienceRoleId = assessmentAudienceRoleId;
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
