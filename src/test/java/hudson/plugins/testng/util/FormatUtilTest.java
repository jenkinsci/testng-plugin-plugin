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
    public void testformatTimeInMilliSeconds() {
        Assert.assertEquals("01:59:23.234", FormatUtil.formatTimeInMilliSeconds(7163.234f));
        Assert.assertEquals("00:52:43.234", FormatUtil.formatTimeInMilliSeconds(3163.234f));
        Assert.assertEquals("00:00:23.234", FormatUtil.formatTimeInMilliSeconds(23.234f));
        Assert.assertEquals("00:00:00.234", FormatUtil.formatTimeInMilliSeconds(0.234f));
        Assert.assertEquals("00:00:00.000", FormatUtil.formatTimeInMilliSeconds(0f));
        Assert.assertEquals("00:01:00.000", FormatUtil.formatTimeInMilliSeconds(60.000f));
        Assert.assertEquals("01:00:00.000", FormatUtil.formatTimeInMilliSeconds(3600.000f));
        Assert.assertEquals("00:00:10.111", FormatUtil.formatTimeInMilliSeconds(10.111f));
        Assert.assertEquals(FormatUtil.MORE_THAN_24HRS, FormatUtil.formatTimeInMilliSeconds(1000000.000f));
    }
}
