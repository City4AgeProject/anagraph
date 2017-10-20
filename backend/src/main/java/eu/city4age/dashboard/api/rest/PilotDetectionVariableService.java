package eu.city4age.dashboard.api.rest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.server.ContainerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.main.JsonSchema;

import eu.city4age.dashboard.api.config.ObjectMapperFactory;
import eu.city4age.dashboard.api.persist.DetectionVariableRepository;
import eu.city4age.dashboard.api.persist.PilotDetectionVariableRepository;
import eu.city4age.dashboard.api.persist.PilotRepository;
import eu.city4age.dashboard.api.pojo.domain.Assessment;
import eu.city4age.dashboard.api.pojo.domain.DetectionVariable;
import eu.city4age.dashboard.api.pojo.domain.DetectionVariableType;
import eu.city4age.dashboard.api.pojo.domain.Pilot;
import eu.city4age.dashboard.api.pojo.domain.PilotDetectionVariable;
import eu.city4age.dashboard.api.pojo.ex.ConfigurationValidityDateException;
import eu.city4age.dashboard.api.pojo.ex.JsonValidationException;
import eu.city4age.dashboard.api.pojo.ex.MissingKeyException;
import eu.city4age.dashboard.api.pojo.json.ConfigureDailyMeasuresDeserializer;
import eu.city4age.dashboard.api.pojo.json.desobj.Configuration;
import eu.city4age.dashboard.api.pojo.json.desobj.Gef;
import eu.city4age.dashboard.api.pojo.json.desobj.Ges;
import eu.city4age.dashboard.api.pojo.json.desobj.Groups;
import eu.city4age.dashboard.api.pojo.json.desobj.Mea;
import eu.city4age.dashboard.api.pojo.other.ConfigurationCounter;
import eu.city4age.dashboard.api.utils.ValidationUtils;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author Andrija Petrovic
 *
 */
@Component
@Transactional("transactionManager")
@Path(PilotDetectionVariableService.PATH)
public class PilotDetectionVariableService {

	public static final String PATH = "configuration";

	@Context
	private HttpServletRequest request;

	@Autowired
	private DetectionVariableRepository detectionVariableRepository;

	@Autowired
	private PilotDetectionVariableRepository pilotDetectionVariableRepository;

	@Autowired
	private PilotRepository pilotRepository;

	static protected Logger logger = LogManager.getLogger(PilotDetectionVariableService.class);

	static protected RestTemplate rest = new TestRestTemplate();

	private static final ObjectMapper objectMapper = ObjectMapperFactory.create();

	@Context
	UriInfo uriInfo;

	@POST
	@ApiOperation("Insert configuration from Pilot Configuration Json into md_pilot_detection_variable table.")
	@Produces(MediaType.TEXT_PLAIN)
	@Path("updateFromConfigFile")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = Assessment.class),
			@ApiResponse(code = 404, message = "Not Found"), @ApiResponse(code = 500, message = "Failure") })
	public final Response updateConfigurationService(String json)
			throws FileNotFoundException, IOException, ProcessingException, ContainerException, JsonParseException,
			JsonValidationException, MissingKeyException, ConfigurationValidityDateException {

		StringBuilder response = new StringBuilder();

		JsonSchema jsonSchemaFromResource = ValidationUtils
				.getSchemaNodeFromResource("/PilotConfigurationJsonValidator.json");
		JsonNode jsonNodeAsString = ValidationUtils.getJsonNode(json);

		String baseUri = uriInfo.getBaseUri().toString();

		if (ValidationUtils.isJsonValid(jsonSchemaFromResource, jsonNodeAsString)) {
			@SuppressWarnings("deprecation")
			ConfigureDailyMeasuresDeserializer data = objectMapper.reader(ConfigureDailyMeasuresDeserializer.class)
					.with(DeserializationFeature.READ_ENUMS_USING_TO_STRING).readValue(json);

			for (int z = 0; z < data.getConfigurations().size(); z++) {
				Configuration configuration = data.getConfigurations().get(z);

				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

				ConfigurationCounter confCounter = new ConfigurationCounter();

				String validFromInString = configuration.getValidFrom();
				String validToInString = configuration.getValidTo();
				Date validFrom;
				Date validTo;

				try {
					validFrom = formatter.parse(validFromInString);
					validTo = formatter.parse(validToInString);
				} catch (ParseException ex) {
					throw new ConfigurationValidityDateException();
				}

				int startingNumberOfRows = pilotDetectionVariableRepository.findAll().size();

				String pilotCode = configuration.getPilotCode();

				try {
					Pilot pilot = pilotRepository.findByPilotCode(pilotCode);
					pilot.setLatestConfigurationUpdate(new Date());
					pilotRepository.save(pilot);
				} catch (NullPointerException ex) {
					throw new MissingKeyException(
							"MissingKeyException:\n\tName for property : pilotCode(pilot_code): "+pilotCode+"\n\tin JSON file doesn't match to any key\n\tin corresponding table: pilot.\n\t");
				}

				for (int i = 0; i < configuration.getGroups().size(); i++) {

					Groups group = configuration.getGroups().get(i);

					logger.info(" DETECTION_VAR(GFG): " + group.getName() + "  DERIVED_VAR(OVL): "
							+ configuration.getName());

					DetectionVariable gfgDetectionVariable = findDetectionVariableOfType(group.getName(),
							DetectionVariableType.GFG);

					DetectionVariable ovlDetectionVariable = findDetectionVariableOfType(configuration.getName(),
							DetectionVariableType.OVL);

					createOrUpdatePilotDetectionVariable(gfgDetectionVariable, ovlDetectionVariable, validFrom, validTo,
							pilotCode, group.getFormula(), group.getWeight(), confCounter);

					for (int j = 0; j < group.getFactors().size(); j++) {

						Gef factor = group.getFactors().get(j);
						logger.info(
								" DETECTION_VAR(GEF): " + factor.getName() + "  DERIVED_VAR(GFG): " + group.getName());

						DetectionVariable gefDetectionVariable = findDetectionVariableOfType(factor.getName(),
								DetectionVariableType.GEF);

						createOrUpdatePilotDetectionVariable(gefDetectionVariable, gfgDetectionVariable, validFrom,
								validTo, pilotCode, factor.getFormula(), factor.getWeight(), confCounter);

						for (int k = 0; k < factor.getSubFactors().size(); k++) {

							Ges subFactor = factor.getSubFactors().get(k);
							logger.info(" DETECTION_VAR(GES): " + subFactor.getName() + "  DERIVED_VAR(GEF): "
									+ factor.getName());

							DetectionVariable gesDetectionVariable = findDetectionVariableOfType(subFactor.getName(),
									DetectionVariableType.GES);
							createOrUpdatePilotDetectionVariable(gesDetectionVariable, gefDetectionVariable, validFrom,
									validTo, pilotCode, subFactor.getFormula(), subFactor.getWeight(), confCounter);

							for (int n = 0; n < subFactor.getMeasures().size(); n++) {

								Mea measure = subFactor.getMeasures().get(n);
								logger.info(" DETECTION_VAR(MEA): " + measure.getName() + "  DERIVED_VAR(GES): "
										+ subFactor.getName());

								DetectionVariable meaDetectionVariable = findDetectionVariableOfType(measure.getName(),
										DetectionVariableType.MEA);
								List<String> NuiList = Arrays.asList("avg", "std", "delta", "best");
								Map<String, String> NuiListFormula = new HashMap<String, String>() {

									private static final long serialVersionUID = 1L;

									{
										put("avg", "avg");
										put("std", "std");
										put("delta", "delta");
										put("best", "best");
									}
								};

								createOrUpdatePilotDetectionVariable(meaDetectionVariable, gesDetectionVariable,
										validFrom, validTo, pilotCode, null, measure.getWeight(), confCounter);

								if (meaDetectionVariable.getDefaultTypicalPeriod().equals("1WK")
										|| meaDetectionVariable.getDefaultTypicalPeriod().equals("DAY")) {

									BigDecimal nuiWeight = BigDecimal.valueOf((double) 1 / NuiListFormula.size());
									String nuiFormula;
									for (String nui : NuiList) {

										DetectionVariable nuiDetectionVariable = findDetectionVariableOfType(
												nui + '_' + measure.getName(), DetectionVariableType.NUI);

										nuiFormula = NuiListFormula.get(nui);
										logger.info(" DETECTION_VAR(NUI): " + nui + '_' + measure.getName()
												+ "  DERIVED_VAR(GES): " + subFactor.getName());
										logger.info(" DETECTION_VAR(MEA): " + measure.getName() + "  DERIVED_VAR(GES): "
												+ nui + '_' + measure.getName());

										createOrUpdatePilotDetectionVariable(nuiDetectionVariable, gesDetectionVariable,
												validFrom, validTo, pilotCode, null, nuiWeight, confCounter);

										createOrUpdatePilotDetectionVariable(meaDetectionVariable, nuiDetectionVariable,
												validFrom, validTo, pilotCode, nuiFormula, nuiWeight, confCounter);
									}
								}

							}
						}
					}
				}

				List<PilotDetectionVariable> AllFromDB = pilotDetectionVariableRepository.findAll();
				
				response.append("\n\nNumber of ALL rows for all Pilot Codes and Valid Dates : ");
				response.append(startingNumberOfRows);
				response.append("\n\tNumber of rows only For Pilot Code: ");
				response.append(pilotCode);
				response.append("\nand Valid From Date: ");
				response.append(validFrom);
				response.append(" : \n\tNumber of inserted rows from configuration file is: ");
				response.append(confCounter.getInserted());
				response.append("\n\tNumber of updated rows from configuration file is: ");
				response.append(confCounter.getUpdated());

				boolean exists;
				int removed = 0;

				List<String> NUIList = Arrays.asList("avg", "std", "delta", "best");
				for (PilotDetectionVariable one : AllFromDB) {
					exists = true;
					outerLoop: if (one.getPilotCode().equals(configuration.getPilotCode())
							&& one.getValidFrom().compareTo(validFrom) == 0) {

						exists = false;
						for (int i = 0; i < configuration.getGroups().size(); i++) {

							Groups group = configuration.getGroups().get(i);

							if (existPdv(one, group.getName(), configuration.getName()) == false) {

								exists = false;
								for (int j = 0; j < group.getFactors().size(); j++) {

									Gef factor = group.getFactors().get(j);

									if (existPdv(one, factor.getName(), group.getName()) == false) {

										exists = false;
										for (int k = 0; k < factor.getSubFactors().size(); k++) {

											Ges subFactor = factor.getSubFactors().get(k);

											if (existPdv(one, subFactor.getName(), factor.getName()) == false) {

												exists = false;
												for (int n = 0; n < subFactor.getMeasures().size(); n++) {

													Mea measure = subFactor.getMeasures().get(n);

													DetectionVariable meaDetectionVariable = findDetectionVariableOfType(
															measure.getName(), DetectionVariableType.MEA);

													if (existPdv(one, measure.getName(),
															subFactor.getName()) == false) {
														exists = false;

													} else {
														exists = true;
														break outerLoop;
													}

													if (meaDetectionVariable.getDefaultTypicalPeriod().equals("1WK")
															|| meaDetectionVariable.getDefaultTypicalPeriod()
																	.equals("DAY")) {

														for (String nui : NUIList) {

															if (existPdv(one, nui + '_' + measure.getName(),
																	subFactor.getName()) == false) {
																exists = false;

																if (existPdv(one, measure.getName(),
																		nui + '_' + measure.getName()) == false) {
																	exists = false;

																} else {
																	exists = true;
																	break outerLoop;
																}

															} else {
																exists = true;
																break outerLoop;
															}
														}

													}
												}

											} else {
												exists = true;
												break outerLoop;
											}
										}
									} else {
										exists = true;
										break outerLoop;
									}
								}
							} else {
								exists = true;
								break outerLoop;
							}
						}
					}

					if (exists == false) {
						pilotDetectionVariableRepository.delete(one);
						removed++;
					}
				}
				response.append("\n\tNumber of deleted rows is: ");
				response.append(removed);
				response.append("\n\tNumber of rows in DB after update is: ");
				response.append(pilotDetectionVariableRepository.findAll().size());
			}
		} else {
			response.append("Json is not valid!\nProperty(ies) is/are missing or it has incorrect data type!");

			Iterator<?> itr = ValidationUtils.getReport(jsonSchemaFromResource, jsonNodeAsString).iterator();
			ProcessingMessage message;
			while (itr.hasNext()) {

				message = (ProcessingMessage) itr.next();

				String pointer = message.asJson().get("instance").get("pointer").toString();

				if (pointer.length() > 2) {
					pointer = pointer.substring(2, pointer.length() - 1);
					String[] parts = pointer.split("/");
					StringBuilder c = new StringBuilder(" ");
					response.append("\nError in configuration json file at location:");
					for (String p : parts) {
						c.append(" ");
						response.append("\n").append(c).append(p).append(":");
					}
					response.setLength(response.length()-1);
				} else {
					response.append("\nError in configuration json file located:\n at the ROOT of the JSON file");
				}

				if (message.asJson().get("keyword").toString().equals("\"type\"")) {
					
					response.append("\nExpected data type is: ");
					response.append(message.asJson().get("expected").toString().replaceAll("[\\[|\\]]", ""));
					response.append(", but: ");
					response.append(message.asJson().get("found").toString());
					response.append(" is found.");
					
				} else if (message.asJson().get("keyword").toString().equals("\"required\"")) {
					response.append("\nMissing property is: ");
					response.append(message.asJson().get("missing").toString().replaceAll("[\\[|\\]]", ""));

				} else {
					response.append("Other type of JsonValidationException!");
				}
				throw new JsonValidationException(response.toString());
			}
		}
		
		//CALLING OF computeMeasures SERVICE
		/*
		try {
			StringBuilder uri = new StringBuilder(baseUri).append("measures/computeFromMeasures");
			response.append("\n\tCalled Url: ").append(uri);
			ResponseEntity<String> responseFromComputeMeasures = rest.getForEntity(uri.toString(), String.class);
			if (!responseFromComputeMeasures.getStatusCode().equals(HttpStatus.OK)) {
				response.append("\n\t\tcomputeFromMeasures:\n\tEXCEPTION:\n\tFailed : HTTP error code : ");
				response.append(responseFromComputeMeasures.getStatusCode());
				response.append("\n\t\t\terror body: ");
				response.append(responseFromComputeMeasures.getBody());
			} else {
				response.append("\n\t\tcomputeFromMeasures:\n\tstatus code: ");
				response.append(responseFromComputeMeasures.getStatusCode());
				response.append("\n\t\t\tbody: ");
				response.append(responseFromComputeMeasures.getBody());
			}
		} catch (Exception ex) {
			response.append("\n\tERROR while calling computeFromMeasures Service:\n\texception message: ");
			response.append(ex.getMessage());
		}
		*/
		
		return Response.ok().type(MediaType.TEXT_PLAIN).entity(response.toString()).build();
	}

	private DetectionVariable findDetectionVariableOfType(String dvName, DetectionVariableType dvt)
			throws MissingKeyException {
		DetectionVariable dv = detectionVariableRepository.findByDetectionVariableNameAndDetectionVariableType(dvName,
				dvt);
		if (dv == null) {
			throw new MissingKeyException(
					"MissingKeyException:\n\tName of property : name(detection_variable_name): "+dvName+"\n\tin JSON file doesn't match to any key\n\tin corresponding table: cd_detection_variable.\n\t");
		}
		return dv;
	}

	private void createOrUpdatePilotDetectionVariable(DetectionVariable dv, DetectionVariable ddv, Date validFrom,
			Date validTo, String pilotCode, String nuiFormula, BigDecimal nuiWeight, ConfigurationCounter cfc) {

		PilotDetectionVariable pdv1 = pilotDetectionVariableRepository
				.findOneByPilotCodeAndDetectionVariableIdAndDerivedDetectionVariableId(pilotCode, dv.getId(),
						ddv.getId());

		if (pdv1 != null) {
			if (!((pdv1.getDerivationWeight().compareTo(nuiWeight) == 0)
					&& (pdv1.getFormula() == null
							|| (pdv1.getFormula() != null && pdv1.getFormula().trim().compareTo(nuiFormula) == 0))
					&& (pdv1.getValidFrom().compareTo(validFrom) == 0)
					&& (pdv1.getValidTo().compareTo(validTo) == 0))) {

				pdv1.setDerivationWeight(nuiWeight);
				if (nuiFormula != null)
					pdv1.setFormula(nuiFormula.equals("") ? null : nuiFormula);
				pdv1.setValidFrom(validFrom);
				pdv1.setValidTo(validTo);

				pilotDetectionVariableRepository.save(pdv1);
				cfc.incrementUpdated();

			}

		} else {
			PilotDetectionVariable pdv2 = new PilotDetectionVariable();
			pdv2.setValidFrom(validFrom);
			pdv2.setValidTo(validTo);
			pdv2.setPilotCode(pilotCode);
			if (nuiFormula != null)
				pdv2.setFormula(nuiFormula.equals("") ? null : nuiFormula);
			pdv2.setDerivationWeight(nuiWeight);
			pdv2.setDetectionVariable(dv);
			pdv2.setDerivedDetectionVariable(ddv);

			pilotDetectionVariableRepository.save(pdv2);
			cfc.incrementInserted();
		}

	}

	private boolean existPdv(PilotDetectionVariable pdv, String dvName, String ddvName) {
		if (pdv.getDetectionVariable().getDetectionVariableName().equals(dvName)
				&& pdv.getDerivedDetectionVariable().getDetectionVariableName().equals(ddvName))
			return true;
		else
			return false;
	}

}
