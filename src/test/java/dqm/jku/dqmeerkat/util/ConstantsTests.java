package dqm.jku.dqmeerkat.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * This class tests if methods in {@link dqm.jku.dqmeerkat.util.Constants} return the expected constant values.
 * @author Johannes Schrott
 */

public class ConstantsTests {
    @Test
    @DisplayName("Get Path from FileName Enum (exemplarily: Popular_Baby_Names.csv)")
    // The test could be further extended with other resources using the JUnit TestFactory or the parameterized test.
    void testFileNameGetPath() {
        assertEquals("src/main/java/dqm/jku/dqmeerkat/resources/csv/Popular_Baby_Names.csv", Constants.FileName.popularBabyNames.getPath());
    }
}