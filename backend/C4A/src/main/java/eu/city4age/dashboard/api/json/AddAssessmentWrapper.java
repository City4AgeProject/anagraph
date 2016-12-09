package eu.city4age.dashboard.api.json;

import java.util.List;

import eu.city4age.dashboard.api.domain.DataValidityStatus;

public class AddAssessmentWrapper {
	
	Long authorId;

	String comment;
	
	Character riskStatus;
	
	DataValidityStatus dataValidityStatus;
	
	List<Long> geriatricFactorValueIds;
	
	List<Long> audienceIds;

	
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Long getAuthorId() {
		return authorId;
	}

	public void setAuthorId(Long authorId) {
		this.authorId = authorId;
	}

	public List<Long> getGeriatricFactorValueIds() {
		return geriatricFactorValueIds;
	}

	public void setGeriatricFactorValueIds(List<Long> geriatricFactorValueIds) {
		this.geriatricFactorValueIds = geriatricFactorValueIds;
	}

	public Character getRiskStatus() {
		return riskStatus;
	}

	public void setRiskStatus(Character riskStatus) {
		this.riskStatus = riskStatus;
	}

	public DataValidityStatus getDataValidityStatus() {
		return dataValidityStatus;
	}

	public void setDataValidityStatus(DataValidityStatus dataValidityStatus) {
		this.dataValidityStatus = dataValidityStatus;
	}

	public List<Long> getAudienceIds() {
		return audienceIds;
	}

	public void setAudienceIds(List<Long> audienceIds) {
		this.audienceIds = audienceIds;
	}

}
