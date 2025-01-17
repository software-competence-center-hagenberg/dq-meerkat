package dqm.jku.dqmeerkat.dtdl.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * <h2>ProfileStatisticDto</h2>
 * <summary>
 * Data Transfer Object for {@link dqm.jku.dqmeerkat.quality.profilingstatistics.ProfileStatistic}s. All data is
 * encoded in strings
 * </summary>
 *
 * @author meindl, rainer.meindl@scch.at
 * @since 10.05.2022
 */
@SuperBuilder
@Getter
@Setter
public class ProfileStatisticDto extends DtdlDto {
    private String title;
    private String value;
    private String category;
}
