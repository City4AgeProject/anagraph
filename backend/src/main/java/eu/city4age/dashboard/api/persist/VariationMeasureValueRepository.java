package eu.city4age.dashboard.api.persist;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.city4age.dashboard.api.persist.generic.GenericRepository;
import eu.city4age.dashboard.api.pojo.domain.DetectionVariable;
import eu.city4age.dashboard.api.pojo.domain.UserInRole;
import eu.city4age.dashboard.api.pojo.domain.VariationMeasureValue;

@Repository(value = "variationMeasureValueRepository")
@Transactional(readOnly = true)
public interface VariationMeasureValueRepository extends GenericRepository<VariationMeasureValue, Long> {

	@Query("SELECT vm FROM VariationMeasureValue vm INNER JOIN vm.detectionVariable dv LEFT JOIN vm.timeInterval ti WHERE vm.userInRole.id = :uId AND (ti.intervalStart >= :intervalStart OR (ti.intervalEnd IS NULL OR ti.intervalEnd >= :intervalStart)) AND (ti.typicalPeriod IS NULL OR ti.typicalPeriod = 'MON')")
	List<VariationMeasureValue> findByUserInRoleId(@Param("uId") final Long uId,
			@Param("intervalStart") final Timestamp intervalStart);

	@Query("SELECT vm FROM VariationMeasureValue vm INNER JOIN vm.userInRole uir LEFT JOIN vm.timeInterval ti WHERE uir.pilotCode = :pilotCode AND (ti.intervalStart >= :intervalStart OR (ti.intervalEnd IS NULL OR ti.intervalEnd >= :intervalStart)) AND (ti.intervalStart <= :intervalEnd OR (ti.intervalEnd IS NULL OR ti.intervalEnd <= :intervalEnd)) AND (ti.typicalPeriod IS NULL OR ti.typicalPeriod = 'DAY')")
	List<VariationMeasureValue> findByPilotCode(@Param("pilotCode") final String pilotCode,
			@Param("intervalStart") final Timestamp intervalStart, @Param("intervalEnd") final Timestamp intervalEnd);

	@Query("SELECT MIN(vm.id) FROM VariationMeasureValue vm WHERE vm.detectionVariable = :dv AND vm.userInRole = :uir")
	Long findMinId(@Param("dv") DetectionVariable dv, @Param("uir") UserInRole uir);

	@Query(nativeQuery = true)
	BigDecimal doWeightedAvg();

	@Query(nativeQuery = true)
	BigDecimal doWeightedStDev();

	@Query(nativeQuery = true)
	BigDecimal doWeightedBest25Perc();

	@Query(nativeQuery = true)
	BigDecimal doWeightedDelta25PercAvg();

}