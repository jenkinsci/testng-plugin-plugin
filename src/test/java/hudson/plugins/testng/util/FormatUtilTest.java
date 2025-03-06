package hudson.plugins.testng.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link FormatUtil}
 *
 * @author nullin
 */
class FormatUtilTest {

    @Test
    void testformatTime() {
        assertEquals("01:59:23.234", FormatUtil.formatTime(7163.234f));
        assertEquals("00:52:43.234", FormatUtil.formatTime(3163.234f));
        assertEquals("00:00:23.234", FormatUtil.formatTime(23.234f));
        assertEquals("00:00:00.234", FormatUtil.formatTime(0.234f));
        assertEquals("00:00:00.000", FormatUtil.formatTime(0f));
        assertEquals("00:01:00.000", FormatUtil.formatTime(60.000f));
        assertEquals("01:00:00.000", FormatUtil.formatTime(3600.000f));
        assertEquals("00:00:10.111", FormatUtil.formatTime(10.111f));
        assertEquals(FormatUtil.MORE_THAN_24HRS, FormatUtil.formatTime(1000000.000f));
    }
}
