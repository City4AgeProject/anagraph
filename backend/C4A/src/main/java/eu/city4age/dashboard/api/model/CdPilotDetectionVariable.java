package eu.city4age.dashboard.api.model;

import java.io.Serializable;

// default package
// Generated 24-Nov-2016 15:43:47 by Hibernate Tools 5.2.0.Beta1

/**
 * CdPilotDetectionVariable generated by hbm2java
 */
public class CdPilotDetectionVariable implements Serializable {

	private CdPilotDetectionVariableId cdPilotDetectionVariableId;
	private CdDetectionVariable cdDetectionVariable;
	private Pilot pilot;
	private String detectionVariableDescriptionFormula;

	public CdPilotDetectionVariable() {
	}

	public CdPilotDetectionVariable(CdDetectionVariable cdDetectionVariable, Pilot pilot,
			String detectionVariableDescriptionFormula) {
		this.cdDetectionVariable = cdDetectionVariable;
		this.pilot = pilot;
		this.detectionVariableDescriptionFormula = detectionVariableDescriptionFormula;
	}

	public CdPilotDetectionVariableId getCdPilotDetectionVariableId() {
		return cdPilotDetectionVariableId;
	}

	public void setCdPilotDetectionVariableId(CdPilotDetectionVariableId cdPilotDetectionVariableId) {
		this.cdPilotDetectionVariableId = cdPilotDetectionVariableId;
	}

	public CdDetectionVariable getCdDetectionVariable() {
		return this.cdDetectionVariable;
	}

	public void setCdDetectionVariable(CdDetectionVariable cdDetectionVariable) {
		this.cdDetectionVariable = cdDetectionVariable;
	}

	public Pilot getPilot() {
		return this.pilot;
	}

	public void setPilot(Pilot pilot) {
		this.pilot = pilot;
	}

	public String getDetectionVariableDescriptionFormula() {
		return this.detectionVariableDescriptionFormula;
	}

	public void setDetectionVariableDescriptionFormula(String detectionVariableDescriptionFormula) {
		this.detectionVariableDescriptionFormula = detectionVariableDescriptionFormula;
	}

}
