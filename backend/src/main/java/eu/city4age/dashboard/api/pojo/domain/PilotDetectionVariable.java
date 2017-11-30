package eu.city4age.dashboard.api.pojo.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * CdPilotDetectionVariable generated by hbm2java
 */
@Entity
@Table(name = "md_pilot_detection_variable")
@SequenceGenerator(name = "default_gen", sequenceName = "md_pilot_detection_variable_id_seq", allocationSize = 1)
public class PilotDetectionVariable extends AbstractBaseEntity<Long> {

	private static final long serialVersionUID = 7337284221184594172L;
	
	@Column(name="pilot_code")
	private String pilotCode;
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "derived_detection_variable_id", referencedColumnName = "id")
	private DetectionVariable derivedDetectionVariable;

	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "detection_variable_id", referencedColumnName = "id")
	private DetectionVariable detectionVariable;
	
	
	@Column(name = "derivation_function_formula")
	private String formula;
	
	
	@Column(name="derivation_weight")
	private BigDecimal derivationWeight;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "valid_from", length = 29)
	private Date validFrom;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "valid_to", length = 29)
	private Date validTo;

	public PilotDetectionVariable() {
	}

	public PilotDetectionVariable(DetectionVariable derivedDetectionVariable,
			String formula) {
		this.detectionVariable = derivedDetectionVariable;
		this.formula = formula;
	}
	
	

	public PilotDetectionVariable(String pilotCode, DetectionVariable derivedDetectionVariable,
			DetectionVariable detectionVariable, String formula, BigDecimal derivationWeight, Date validFrom,
			Date validTo) {
		this.pilotCode = pilotCode;
		this.derivedDetectionVariable = derivedDetectionVariable;
		this.detectionVariable = detectionVariable;
		this.formula = formula;
		this.derivationWeight = derivationWeight;
		this.validFrom = validFrom;
		this.validTo = validTo;
	}

	public PilotDetectionVariable(DetectionVariable derivedDetectionVariable, DetectionVariable detectionVariable,
			String formula, Date validFrom, Date validTo) {
		this.derivedDetectionVariable = derivedDetectionVariable;
		this.detectionVariable = detectionVariable;
		this.formula = formula;
		this.validFrom = validFrom;
		this.validTo = validTo;
	}

	public DetectionVariable getDerivedDetectionVariable() {
		return this.derivedDetectionVariable;
	}

	public void setDerivedDetectionVariable(DetectionVariable derivedDetectionVariable) {
		this.derivedDetectionVariable = derivedDetectionVariable;
	}

	public DetectionVariable getDetectionVariable() {
		return this.detectionVariable;
	}

	public void setDetectionVariable(DetectionVariable detectionVariable) {
		this.detectionVariable = detectionVariable;
	}

	public String getFormula() {
		return this.formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
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

	public String getPilotCode() {
		return pilotCode;
	}

	public void setPilotCode(String pilotCode) {
		this.pilotCode = pilotCode;
	}

	public BigDecimal getDerivationWeight() {
		return derivationWeight;
	}

	public void setDerivationWeight(BigDecimal derivationWeight) {
		this.derivationWeight = derivationWeight;
	}

}
