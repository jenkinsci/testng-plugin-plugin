package hudson.plugins.testng.util;

import junit.framework.Assert;
import org.junit.Test;

/**
 * Tests for {@link FormatUtil}
 *
 * @author nullin
 */
public class FormatUtilTest {

    @Test
    public void testformatTime() {
        Assert.assertEquals("01:59:23.234", FormatUtil.formatTime(7163.234f));
        Assert.assertEquals("00:52:43.234", FormatUtil.formatTime(3163.234f));
        Assert.assertEquals("00:00:23.234", FormatUtil.formatTime(23.234f));
        Assert.assertEquals("00:00:00.234", FormatUtil.formatTime(0.234f));
        Assert.assertEquals("00:00:00.000", FormatUtil.formatTime(0f));
        Assert.assertEquals("00:01:00.000", FormatUtil.formatTime(60.000f));
        Assert.assertEquals("01:00:00.000", FormatUtil.formatTime(3600.000f));
        Assert.assertEquals("00:00:10.111", FormatUtil.formatTime(10.111f));
        Assert.assertEquals(FormatUtil.MORE_THAN_24HRS, FormatUtil.formatTime(1000000.000f));
    }
}
