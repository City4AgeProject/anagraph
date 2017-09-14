package eu.city4age.dashboard.api.rest;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Query;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.city4age.dashboard.api.config.ObjectMapperFactory;
import eu.city4age.dashboard.api.persist.DetectionVariableRepository;
import eu.city4age.dashboard.api.persist.GeriatricFactorRepository;
import eu.city4age.dashboard.api.persist.NUIRepository;
import eu.city4age.dashboard.api.persist.PilotDetectionVariableRepository;
import eu.city4age.dashboard.api.persist.PilotRepository;
import eu.city4age.dashboard.api.persist.TimeIntervalRepository;
import eu.city4age.dashboard.api.persist.UserInRoleRepository;
import eu.city4age.dashboard.api.persist.VariationMeasureValueRepository;
import eu.city4age.dashboard.api.persist.ViewGefValuesPersistedSourceGesTypesRepository;
import eu.city4age.dashboard.api.persist.ViewMeaNuiDerivationPerPilotRepository;
import eu.city4age.dashboard.api.persist.ViewNuiValuesPersistedSourceMeaTypesRepository;
import eu.city4age.dashboard.api.persist.ViewPilotDetectionVariableRepository;
import eu.city4age.dashboard.api.pojo.domain.DetectionVariable;
import eu.city4age.dashboard.api.pojo.domain.GeriatricFactorValue;
import eu.city4age.dashboard.api.pojo.domain.NumericIndicatorValue;
import eu.city4age.dashboard.api.pojo.domain.Pilot;
import eu.city4age.dashboard.api.pojo.domain.PilotDetectionVariable;
import eu.city4age.dashboard.api.pojo.domain.TimeInterval;
import eu.city4age.dashboard.api.pojo.domain.UserInRole;
import eu.city4age.dashboard.api.pojo.domain.VariationMeasureValue;
import eu.city4age.dashboard.api.pojo.domain.ViewGefValuesPersistedSourceGesTypes;
import eu.city4age.dashboard.api.pojo.domain.ViewMeaNuiDerivationPerPilot;
import eu.city4age.dashboard.api.pojo.domain.ViewNuiValuesPersistedSourceMeaTypes;
import eu.city4age.dashboard.api.pojo.domain.ViewPilotDetectionVariable;
import eu.city4age.dashboard.api.pojo.enu.TypicalPeriod;
import eu.city4age.dashboard.api.pojo.json.view.View;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author milos.holclajtner
 *
 */
@Component
@Transactional("transactionManager")
@Path(MeasuresService.PATH)
public class MeasuresService {

	public static final String PATH = "measures";

	static protected Logger logger = LogManager.getLogger(MeasuresService.class);

	private static final ObjectMapper objectMapper = ObjectMapperFactory.create();

	@Autowired
	private DetectionVariableRepository detectionVariableRepository;

	@Autowired
	private PilotDetectionVariableRepository pilotDetectionVariableRepository;

	@Autowired
	private VariationMeasureValueRepository variationMeasureValueRepository;

	@Autowired
	private NUIRepository nuiRepository;

	@Autowired
	private PilotRepository pilotRepository;

	@Autowired
	private TimeIntervalRepository timeIntervalRepository;

	@Autowired
	private GeriatricFactorRepository geriatricFactorRepository;

	@Autowired
	private UserInRoleRepository userInRoleRepository;

	@Autowired
	private ViewNuiValuesPersistedSourceMeaTypesRepository viewNuiValuesPersistedSourceMeaTypesRepository;

	@Autowired
	private ViewPilotDetectionVariableRepository viewPilotDetectionVariableRepository;

	@Autowired
	private ViewMeaNuiDerivationPerPilotRepository viewMeaNuiDerivationPerPilotRepository;
	
	@Autowired
	private ViewGefValuesPersistedSourceGesTypesRepository viewGefValuesPersistedSourceGesTypesRepository;

	//new
	@GET
	@ApiOperation("Get daily measures for care recipient within given geriatric subfactor.")
	@Produces(MediaType.APPLICATION_JSON)
	@JsonView(View.VariationMeasureValueView.class)
	@Path("findByUserAndGes/userInRoleId/{userInRoleId}/gesId/{gesId}")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "userInRoleId", value = "id of care recipient", required = false, dataType = "long", paramType = "path", defaultValue = "1"),
		@ApiImplicitParam(name = "gesId", value = "id of geriatric subfactor", required = false, dataType = "long", paramType = "path", defaultValue = "2") })		
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success", response = VariationMeasureValue.class),
			@ApiResponse(code = 404, message = "Not Found"), @ApiResponse(code = 500, message = "Failure") })
	public Response findByUserAndGes(
			@ApiParam(hidden = true) @PathParam("userInRoleId") Long userInRoleId,
			@ApiParam(hidden = true) @PathParam("gesId") Long gesId)
			throws JsonProcessingException {
		
		List<VariationMeasureValue> measures = new ArrayList<VariationMeasureValue>();
		try {
			 measures = variationMeasureValueRepository.findByUserAndGes(userInRoleId, gesId);
			
			 if(measures.size() > 0) {
				
			 }
		} catch (Exception e) {
			logger.info("findByUserAndGes REST service - query exception: ", e);
		}
		
		
		return Response.ok(objectMapper.writerWithView(View.VariationMeasureValueView.class).writeValueAsString(measures)).build();	

		
	}

	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("computeFromMeasures")
	public Response computeFromMeasures() throws JsonProcessingException, IOException {

		// logger.info("computeFromMeasures");

		// lastYearMonth hardcoded for pilotCode
		// bice lastSubmited i lastComputed(servis upisuje) date

		List<Pilot> pilots = pilotRepository.findAll();
		
		logger.info("pilots size: " + pilots.size());

		for (Pilot pilot : pilots) {
			logger.info("pilot code: " + pilot.getPilotCode());
			computeNuisForPilot(pilot.getPilotCode(), pilot.getLastComputed());
		}

		for (Pilot pilot : pilots) {
			logger.info("pilot code: " + pilot.getPilotCode());
			computeGESsForPilot(pilot.getPilotCode(), pilot.getLastComputed());
			logger.info("pilot code: " + pilot.getPilotCode());
			computeForPilot("GEF", new BigDecimal(.125), pilot.getPilotCode(), pilot.getLastComputed());
			computeForPilot("GFG", new BigDecimal(.5), pilot.getPilotCode(), pilot.getLastComputed());
			computeForPilot("OVL", new BigDecimal(1), pilot.getPilotCode(), pilot.getLastComputed());
		}

		return Response.ok().build();
	}

	private void computeForPilot(String factor, BigDecimal weight, String pilotCode, YearMonth lastComputedYearMonth) {
		logger.info("computeForPilot");
		logger.info("factor" + factor);
		YearMonth currentYearMonth = YearMonth.now();
		YearMonth currentComputedYearMonth = lastComputedYearMonth.plusMonths(1L);
		Timestamp startOfMonth;
		Timestamp endOfMonth;
		if (currentYearMonth.compareTo(currentComputedYearMonth) > 1) {
			while (!currentComputedYearMonth.equals(currentYearMonth)) {
				startOfMonth = Timestamp.valueOf(currentComputedYearMonth.atDay(1).atStartOfDay());
				endOfMonth = Timestamp.valueOf(currentComputedYearMonth.atEndOfMonth().atTime(LocalTime.MAX));
				computeFor1Month(factor, weight, pilotCode, startOfMonth, endOfMonth);
				currentComputedYearMonth = currentComputedYearMonth.plusMonths(1L);
			}
		}
	}

	private void computeFor1Month(String factor, BigDecimal weight, String pilotCode, Timestamp startOfMonth, Timestamp endOfMonth) {
		logger.info("computeFor1Month: " + factor);
		logger.info("pilotCode: " + pilotCode);
		logger.info("startOfMonth: " + startOfMonth.toString());
		logger.info("endOfMonth: " + endOfMonth.toString());
		
		List<ViewGefValuesPersistedSourceGesTypes> list = viewGefValuesPersistedSourceGesTypesRepository.findAllForMonthByPilotCode(pilotCode, startOfMonth, factor);
		
		logger.info("list size: " +  list.size());
		
		list.sort(new Comparator<ViewGefValuesPersistedSourceGesTypes>() {
			@Override
			public int compare(ViewGefValuesPersistedSourceGesTypes o1, ViewGefValuesPersistedSourceGesTypes o2) {
				int value1 = o1.getId().getUserInRoleId().compareTo(o2.getId().getUserInRoleId());
				if (value1 == 0) {
					return o1.getId().getDerivedDetectionVariableId().compareTo(o2.getId().getDerivedDetectionVariableId());
				} else
					return value1;
			}
		});
		
		Long previousUserId = 0L;
		BigDecimal sum = new BigDecimal(0);
		for(ViewGefValuesPersistedSourceGesTypes ges: list) {
			if(previousUserId.equals(ges.getId().getUserInRoleId())) {
				sum.add(ges.getGefValue().multiply(ges.getDerivationWeight()));
			} else {
				if(!previousUserId.equals(0L))
					createGFV(ges.getId().getDerivedDetectionVariableId(), sum, weight, startOfMonth, endOfMonth, previousUserId, pilotCode);
				sum = ges.getGefValue().multiply(ges.getDerivationWeight());
			}
			previousUserId = ges.getId().getUserInRoleId();
		}
		if (list != null && list.size() > 1)
			createGFV(list.get(list.size()-1).getId().getDerivedDetectionVariableId(), sum, weight, startOfMonth, endOfMonth, previousUserId, pilotCode);
		
		
	}

	private void computeGESsForPilot(String pilotCode, YearMonth lastComputedYearMonth) {
		YearMonth currentYearMonth = YearMonth.now();
		YearMonth currentComputedYearMonth = lastComputedYearMonth.plusMonths(1L);
		Timestamp startOfMonth;
		Timestamp endOfMonth;
		if (currentYearMonth.compareTo(currentComputedYearMonth) > 1) {
			while (!currentComputedYearMonth.equals(currentYearMonth)) {
				startOfMonth = Timestamp.valueOf(currentComputedYearMonth.atDay(1).atStartOfDay());
				endOfMonth = Timestamp.valueOf(currentComputedYearMonth.atEndOfMonth().atTime(LocalTime.MAX));
				computeGESsFor1Month(pilotCode, startOfMonth, endOfMonth);
				currentComputedYearMonth = currentComputedYearMonth.plusMonths(1L);
			}
		}
	}

	private void computeNuisForPilot(String pilotCode, YearMonth lastComputedYearMonth) {
		YearMonth currentYearMonth = YearMonth.now();
		YearMonth currentComputedYearMonth = lastComputedYearMonth.plusMonths(1L);
		Timestamp startOfMonth;
		Timestamp endOfMonth;
		if (currentYearMonth.compareTo(currentComputedYearMonth) > 1) {
			while (!currentComputedYearMonth.equals(currentYearMonth)) {
				startOfMonth = Timestamp.valueOf(currentComputedYearMonth.atDay(1).atStartOfDay());
				endOfMonth = Timestamp.valueOf(currentComputedYearMonth.atEndOfMonth().atTime(LocalTime.MAX));
				computeNuisFor1Month(pilotCode, startOfMonth, endOfMonth);
				currentComputedYearMonth = currentComputedYearMonth.plusMonths(1L);
			}
		}
	}

	private void computeGESsFor1Month(String pilotCode, Timestamp startOfMonth, Timestamp endOfMonth) {
		logger.info("computeGESsFor1Month");
		logger.info("startOfMonth: " + startOfMonth.toString());
		logger.info("endOfMonth: " + endOfMonth.toString());

		List<VariationMeasureValue> vmsMonthly = variationMeasureValueRepository.findAllForMonthByPilotCode(pilotCode,
				startOfMonth, endOfMonth);

		if (vmsMonthly != null && vmsMonthly.size() > 0) {
			logger.info("vmsMonthly size: " + vmsMonthly.size());

			Set<Long> users = new HashSet<Long>();
			for (VariationMeasureValue vmv : vmsMonthly) {
				users.add(vmv.getUserInRole().getId());
			}

			if (users != null && users.size() > 0) {

				for (Long userId : users) {

					List<ViewPilotDetectionVariable> list = viewPilotDetectionVariableRepository
							.findAllMeaGes(pilotCode, userId);
					
					list.sort(new Comparator<ViewPilotDetectionVariable>() {
						@Override
						public int compare(ViewPilotDetectionVariable o1, ViewPilotDetectionVariable o2) {
							int value1 = o1.getId().getDetectionVariableId().compareTo(o2.getId().getDetectionVariableId());
							if (value1 == 0) {
								return o1.getId().getDerivedDetectionVariableId().compareTo(o2.getId().getDerivedDetectionVariableId());
							} else
								return value1;
						}
					});

					if (list != null && list.size() > 0) {

						Set<Long> ddvIds = new HashSet<>();
						BigDecimal previousGesValue = new BigDecimal(0);
						for (int i = 0; i < list.size(); i++) {
							
							logger.info("vpdv id dv: " + list.get(i).getId().getDetectionVariableId());
							logger.info("vpdv id ddv: " + list.get(i).getId().getDerivedDetectionVariableId());
							logger.info("vpdv id pc: " + list.get(i).getId().getPilotCode());
							logger.info("vpdv id uir: " + list.get(i).getId().getUserInRoleId());

							if (list.size() > 1 && i > 1
									&& !list.get(i - 1).getId().getDerivedDetectionVariableId()
											.equals(list.get(i).getId().getDerivedDetectionVariableId())
									&& !ddvIds.contains(list.get(i - 1).getId().getDerivedDetectionVariableId())) {
								createGFV(list.get(i - 1).getId().getDerivedDetectionVariableId(), previousGesValue,
										null, startOfMonth, endOfMonth, list.get(i - 1).getId().getUserInRoleId(),
										pilotCode);
								ddvIds.add(list.get(i - 1).getId().getDerivedDetectionVariableId());
							}

							String dtp = list.get(i).getDefaultTypicalPeriod();

							BigDecimal gesValue = new BigDecimal(0);

							logger.info("dtp: " + dtp);
							logger.info("user id: " + userId);
							if (dtp.equals("DAY") || dtp.equals("1WK")) {
								logger.info("DAY || 1WK");
								BigDecimal computeDerivedMeaForNUI = computeDerivedMeaForNUI(pilotCode, startOfMonth,
										endOfMonth, list.get(i), userId);
								logger.info("computeDerivedMeaForNUI: " + computeDerivedMeaForNUI);
								gesValue = gesValue.add(computeDerivedMeaForNUI);
								logger.info("gesValue: " + gesValue);
							} else if (dtp.equals("MON")) {
								logger.info("MON");
								BigDecimal computeDerivedMeaForMM = computeDerivedMeaForMM(pilotCode, startOfMonth,
										endOfMonth, list.get(i), userId);
								logger.info("computeDerivedMeaForMM: " + computeDerivedMeaForMM);
								gesValue = gesValue.add(computeDerivedMeaForMM);
								logger.info("gesValue: " + gesValue);
							}
							previousGesValue = gesValue;
							
						}
						if (list != null && list.size() > 1)
							createGFV(list.get(list.size()-1).getId().getDerivedDetectionVariableId(), previousGesValue, null, startOfMonth, endOfMonth, list.get(list.size()-1).getId().getUserInRoleId(), pilotCode);
					}
				}
			}
		}

		Pilot pilot = pilotRepository.findByPilotCode(pilotCode);
		pilot.setLatestVariablesComputed(new Date());
		pilotRepository.save(pilot);
	}



	private BigDecimal computeDerivedMeaForNUI(String pilotCode, Timestamp startOfMonth, Timestamp endOfMonth,
			ViewPilotDetectionVariable vpdv, Long userId) {

		logger.info("computeDerivedMeaForNUI");
		logger.info("dv: " + vpdv.getId().getDetectionVariableId() + " " + vpdv.getDetectionVariableName());

		Long previousMeaId = 0L;
		BigDecimal sum = new BigDecimal(0);

		Long meaId = vpdv.getId().getDetectionVariableId();
		if (!previousMeaId.equals(meaId)) {
			previousMeaId = meaId;
			sum = new BigDecimal(0);
		}

		logger.info("meaId: " + meaId);

		List<ViewMeaNuiDerivationPerPilot> list = viewMeaNuiDerivationPerPilotRepository.findAllNuiForMea(meaId,
				pilotCode);

		logger.info("nui size: " + list.size());

		for (ViewMeaNuiDerivationPerPilot nuiVpdv : list) {

			List<ViewNuiValuesPersistedSourceMeaTypes> userEntriesForNui1Month = viewNuiValuesPersistedSourceMeaTypesRepository
					.findNuiFor1Month(pilotCode, startOfMonth, nuiVpdv.getDerivedNuiId(), userId);

			if (userEntriesForNui1Month != null && userEntriesForNui1Month.size() > 0) {

				BigDecimal dn;
				BigDecimal tn = new BigDecimal(0);
				Long previousUirId = 0L;
				Map<String, BigDecimal> derivedMM = new HashMap<String, BigDecimal>();

				ViewNuiValuesPersistedSourceMeaTypes userEntryForNui1Month = userEntriesForNui1Month.get(0);

				Long currentUirId = userEntryForNui1Month.getUserInRoleId();
				Long currentMeaId = userEntryForNui1Month.getMeaId();
				if (previousUirId != null && !previousUirId.equals(currentUirId) && previousMeaId != null
						&& !previousMeaId.equals(currentMeaId)) {
					sum = new BigDecimal(0);
				}
				previousUirId = currentUirId;
				previousMeaId = currentMeaId;
				Long nuiMonthZeroId = nuiRepository.findMonthZero(userEntryForNui1Month.getDerivedNuiId(),
						userEntryForNui1Month.getUserInRoleId());
				NumericIndicatorValue nuiMonthZero = null;
				BigDecimal weight = new BigDecimal(0.25); // this is hardcoded
				if (nuiMonthZeroId != null) {
					nuiMonthZero = nuiRepository.findOne(nuiMonthZeroId);
					if (nuiMonthZero != null && nuiMonthZero.getNuiValue().compareTo(new BigDecimal(0)) > 0) {
						logger.info("nuiThisMonth.getNuiValue(): " + userEntryForNui1Month.getNuiValue());
						logger.info("nuiMonthZero.getNuiValue(): " + nuiMonthZero.getNuiValue());
						dn = (userEntryForNui1Month.getNuiValue().subtract(nuiMonthZero.getNuiValue()))
								.divide(nuiMonthZero.getNuiValue(), 2, RoundingMode.HALF_UP);
						logger.info("dn: " + dn);
						if (dn.compareTo(new BigDecimal(.25)) < 0) {
							if (dn.compareTo(new BigDecimal(.1)) < 0) {
								if (dn.compareTo(new BigDecimal(-.1)) < 0) {
									if (dn.compareTo(new BigDecimal(-.25)) < 0)
										tn = new BigDecimal(1);
									else
										tn = new BigDecimal(2);
								} else
									tn = new BigDecimal(3);
							} else
								tn = new BigDecimal(4);
						} else
							tn = new BigDecimal(5);
					}
				}
				logger.info("tn: " + tn.toString());
				logger.info("weight: " + weight.toString());
				derivedMM.put("", tn.multiply(weight));
				sum = sum.add(tn.multiply(weight));

			}

		}

		return sum;

	}

	private GeriatricFactorValue createGFV(Long dvId, BigDecimal gesValue, BigDecimal weight, Timestamp startOfMonth,
			Timestamp endOfMonth, Long userId, String pilotCode) {
		logger.info("createGFV");
		logger.info("gesValue: " + gesValue);
		GeriatricFactorValue ges = new GeriatricFactorValue();
		ges.setGefValue(gesValue);
		TimeInterval ti = getOrCreateTimeInterval(startOfMonth, TypicalPeriod.MONTH);
		logger.info("ti: " + ti.getId());
		ges.setTimeInterval(ti);
		logger.info("dvId: " + dvId);
		ges.setDetectionVariable(detectionVariableRepository.findOne(dvId));
		logger.info("userId: " + userId);
		UserInRole uir = userInRoleRepository.findOne(userId);
		ges.setUserInRole(uir);
		if (weight == null) {
			weight = pilotDetectionVariableRepository.findByDetectionVariableAndPilotCodeGesGef(dvId, pilotCode);
		}
		if (weight != null) {
			ges.setDerivationWeight(weight);
		} else {
			ges.setDerivationWeight(new BigDecimal(0));
		}
		logger.info("pre save");
		geriatricFactorRepository.save(ges);
		return ges;
	}

	private BigDecimal computeDerivedMeaForMM(String pilotCode, Timestamp startOfMonth, Timestamp endOfMonth,
			ViewPilotDetectionVariable vpdv, Long userId) {
		List<VariationMeasureValue> mmsThisMonth = variationMeasureValueRepository.findAllByUserInRoleId(userId,
				startOfMonth);
		BigDecimal sum = new BigDecimal(0);
		BigDecimal ds;
		BigDecimal ts;
		BigDecimal weight = new BigDecimal(0);
		for (VariationMeasureValue mmThisMonth : mmsThisMonth) {
			VariationMeasureValue mmMonthZero = variationMeasureValueRepository
					.findOne(variationMeasureValueRepository.findMinId(mmThisMonth.getDetectionVariable(), userId));
			ds = (mmThisMonth.getMeasureValue().subtract(mmMonthZero.getMeasureValue()))
					.divide(mmMonthZero.getMeasureValue(), 2, RoundingMode.HALF_UP);
			PilotDetectionVariable pdv = pilotDetectionVariableRepository
					.findByDetectionVariableAndPilotCodeMeaGes(vpdv.getMpdvId(), pilotCode);
			weight = new BigDecimal(.1);
			if (ds.compareTo(new BigDecimal(.25)) < 0) {
				if (ds.compareTo(new BigDecimal(.1)) < 0) {
					if (ds.compareTo(new BigDecimal(-.1)) < 0) {
						if (ds.compareTo(new BigDecimal(-.25)) < 0)
							ts = new BigDecimal(1);
						else
							ts = new BigDecimal(2);
					} else
						ts = new BigDecimal(3);
				} else
					ts = new BigDecimal(4);
			} else
				ts = new BigDecimal(5);
			sum = sum.add(ts.multiply(weight));
		}
		return sum;
	}

	public List<VariationMeasureValue> filterListByDetectionVariable(final List<VariationMeasureValue> list,
			final DetectionVariable dv) {
		logger.info("dv.getDetectionVariableName(): " + dv.getDetectionVariableName());
		Stream<VariationMeasureValue> filter = list.stream()
				.filter(o -> dv.getId().equals(o.getDetectionVariable().getId()));
		List<VariationMeasureValue> findAll = filter.collect(Collectors.toList());
		return findAll;
	}

	private void computeNuisFor1Month(String pilotCode, Timestamp startOfMonth, Timestamp endOfMonth) {
		List<DetectionVariable> dvsWithMEA = detectionVariableRepository.findAllMEADvTypeByPilotCode(pilotCode);

		logger.info("COUNT DVS: " + dvsWithMEA.size());

		for (DetectionVariable dv : dvsWithMEA) {
			logger.info("DV ID: " + dv.getId());
		}

		Set<DetectionVariable> dvsWithMEASet = new HashSet<DetectionVariable>(dvsWithMEA);

		if (dvsWithMEASet != null && dvsWithMEASet.size() > 0) {

			List<VariationMeasureValue> vmsMonthly = variationMeasureValueRepository
					.findAllForMonthByPilotCodeNui(pilotCode, startOfMonth, endOfMonth);

			if (vmsMonthly != null && vmsMonthly.size() > 0) {
				Long prevDvId = 0L;
				for (DetectionVariable pdv : dvsWithMEASet) {
					if (!prevDvId.equals(pdv.getId())) {
						DetectionVariable dv = pdv;
						logger.info("DETECTION VARIABLE ID: " + dv.getId());
						List<VariationMeasureValue> vmForSpecificMeasure = filterListByDetectionVariable(vmsMonthly,
								dv);
						Set<UserInRole> users = new HashSet<UserInRole>();
						for (VariationMeasureValue vmv : vmForSpecificMeasure) {
							users.add(vmv.getUserInRole());
						}
						if (vmForSpecificMeasure != null) {
							for (UserInRole uir : users) {
								createAllNuis(uir, dv, startOfMonth, endOfMonth, pilotCode);
							}
						}
						prevDvId = pdv.getId();
					}
				}
			}

		}

	}

	private List<NumericIndicatorValue> createAllNuis(UserInRole uir, DetectionVariable dv, Timestamp startOfMonth,
			Timestamp endOfMonth, String pilotCode) {
		logger.info("createAllNuis");
		logger.info("uir: " + uir.getId());
		logger.info("dv: " + dv.getId());
		logger.info("startOfMonth: " + startOfMonth);
		logger.info("endOfMonth: " + endOfMonth);
		logger.info("pilotCode: " + pilotCode);
		List<NumericIndicatorValue> nuis = new ArrayList<NumericIndicatorValue>();

		// menja se where za upit jer smo izmenili upit i podelili na dva (upit
		// i podupit)
		String formula = "WITH subq AS (SELECT AVG (vm.measure_value) AS avg, COALESCE (STDDEV(vm.measure_value), 0) stDev, PERCENTILE_CONT (0.25) WITHIN GROUP (ORDER BY vm.measure_value DESC) best25 FROM variation_measure_value AS vm INNER JOIN time_interval AS ti ON ti. ID = vm.time_interval_id WHERE ti.interval_start >= '"
				+ startOfMonth + "' AND ti.interval_end <= '" + endOfMonth + "' AND vm.user_in_role_id = "
				+ uir.getId().toString() + " AND vm.measure_type_id = " + dv.getId().toString()
				+ ") SELECT avg, (CASE WHEN avg != 0 THEN stDev / avg ELSE 0 END) std, (CASE WHEN avg != 0 THEN best25 / avg ELSE 0 END) best, (CASE WHEN avg != 0 THEN best25 - avg / avg ELSE 0 END) delta FROM subq";
		Query sql = variationMeasureValueRepository.getEntityManager().createNativeQuery(formula);
		variationMeasureValueRepository.getEntityManager().getEntityManagerFactory()
				.addNamedQuery("VariationMeasureValue.dynamic", sql);
		Query namedQuery = variationMeasureValueRepository.getEntityManager()
				.createNamedQuery("VariationMeasureValue.dynamic");

		Object[] nuiForMeasure = (Object[]) namedQuery.getSingleResult(); // Long
																			// should
																			// be
																			// converted
																			// to
																			// BigDecimal
																			// or
																			// Double!!!


		logger.info("nuiForMeasure length: " + nuiForMeasure.length);

		List<ViewMeaNuiDerivationPerPilot> dvNuisForMeasure = viewMeaNuiDerivationPerPilotRepository
				.findAllDvNuisForMeasure(dv.getDetectionVariableName(), pilotCode);

		logger.info("dvNuisForMeasure size: " + dvNuisForMeasure.size());

		BigDecimal nuiValue = new BigDecimal(0);
		for (int i = 0; i < 4; i++) {
			logger.info("dvNuiForMeasure id: " + dvNuisForMeasure.get(i).getMpdvId());
			logger.info("dvNuiForMeasure dv id: " + dvNuisForMeasure.get(i).getMeaId());
			logger.info("dvNuiForMeasure pilot code: " + dvNuisForMeasure.get(i).getPilotCode());

			if (dvNuisForMeasure.get(i).getFormula().contains("avg")) {
				String nui1 = String.valueOf(nuiForMeasure[0]);
				logger.info("nui1: " + nui1);
				nuiValue = new BigDecimal(nui1);
			} else if (dvNuisForMeasure.get(i).getFormula().contains("std")) {
				String nui2 = String.valueOf(nuiForMeasure[1]);
				logger.info("nui2: " + nui2);
				nuiValue = new BigDecimal(nui2);
			} else if (dvNuisForMeasure.get(i).getFormula().contains("best")) {
				String nui3 = String.valueOf(nuiForMeasure[2]);
				logger.info("nui3: " + nui3);
				nuiValue = new BigDecimal(nui3);
			} else if (dvNuisForMeasure.get(i).getFormula().contains("delta")) {

				String nui4 = String.valueOf(nuiForMeasure[3]);
				logger.info("nui4: " + nui4);
				nuiValue = new BigDecimal(nui4);
			}
			NumericIndicatorValue nui = create1Nui(uir, nuiValue, dvNuisForMeasure.get(i).getDerivedNuiId(), startOfMonth);
			nuis.add(nui);
		}
		return nuis;
	}

	private NumericIndicatorValue create1Nui(UserInRole uir, BigDecimal nuiValue, Long nuiDvId,
			Timestamp startOfMonth) {
		// logger.info("create1Nui");
		NumericIndicatorValue nui = new NumericIndicatorValue();
		if (nuiValue != null) {
			nui.setNuiValue(new BigDecimal(nuiValue.toString()));
		} else {
			nui.setNuiValue(new BigDecimal(0));
		}
		nui.setUserInRole(uir);
		nui.setDetectionVariable(detectionVariableRepository.getOne(nuiDvId));
		nui.setTimeInterval(getOrCreateTimeInterval(startOfMonth, TypicalPeriod.MONTH));
		nuiRepository.save(nui);
		return nui;
	}

	public TimeInterval getOrCreateTimeInterval(Timestamp intervalStart, TypicalPeriod typicalPeriod) {
		TimeInterval ti = timeIntervalRepository.findByIntervalStartAndTypicalPeriod(intervalStart,
				typicalPeriod.getDbName());
		if (ti == null) {
			ti = new TimeInterval();
			ti.setIntervalStart(intervalStart);
			ti.setTypicalPeriod(typicalPeriod.getDbName());
			timeIntervalRepository.save(ti);
			timeIntervalRepository.flush();
		}
		return ti;
	}

}