package eu.city4age.dashboard.api.model;

// default package
// Generated 24-Nov-2016 15:43:47 by Hibernate Tools 5.2.0.Beta1

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 * CdRole generated by hbm2java
 */
public class CdRole extends AbstractBaseEntity {

	private String roleName;
	private String roleAbbreviation;
	private String roleDescription;
	private Date validFrom;
	private Date validTo;
	private String stakeholderAbbreviation;
	
	@JsonBackReference
	private Set assessmentAudienceRoles = new HashSet(0);

	public CdRole() {
	}

	public CdRole(String roleName, String roleAbbreviation, String roleDescription, Date validFrom,
			Date validTo, Set assessmentAudienceRoles, String stakeholderAbbreviation) {
		this.roleName = roleName;
		this.roleAbbreviation = roleAbbreviation;
		this.roleDescription = roleDescription;
		this.validFrom = validFrom;
		this.validTo = validTo;
		this.assessmentAudienceRoles = assessmentAudienceRoles;
		this.stakeholderAbbreviation = stakeholderAbbreviation;
	}

	public String getRoleName() {
		return this.roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getRoleAbbreviation() {
		return this.roleAbbreviation;
	}

	public void setRoleAbbreviation(String roleAbbreviation) {
		this.roleAbbreviation = roleAbbreviation;
	}

	public String getRoleDescription() {
		return this.roleDescription;
	}

	public void setRoleDescription(String roleDescription) {
		this.roleDescription = roleDescription;
	}

	public Date getValidFrom() {
		return this.validFrom;
	}

	public void setValidFrom(Date validFrom) {
		this.validFrom = validFrom;
	}

	public Date getValidTo() {
		return this.validTo;
	}

	public void setValidTo(Date validTo) {
		this.validTo = validTo;
	}

	public String getStakeholderAbbreviation() {
		return stakeholderAbbreviation;
	}

	public void setStakeholderAbbreviation(String stakeholderAbbreviation) {
		this.stakeholderAbbreviation = stakeholderAbbreviation;
	}

	public Set getAssessmentAudienceRoles() {
		return this.assessmentAudienceRoles;
	}

	public void setAssessmentAudienceRoles(Set assessmentAudienceRoles) {
		this.assessmentAudienceRoles = assessmentAudienceRoles;
	}

}
