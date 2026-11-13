package hudson.plugins.testng.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Functions;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.plugins.testng.PluginImpl;
import hudson.plugins.testng.TestNGTestResultBuildAction;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;
import hudson.util.ShiftedCategoryAxis;
import hudson.util.StackedAreaRenderer2;
import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;

/** Helper class for trend graph generation */
@SuppressFBWarnings(
        value = "EQ_DOESNT_OVERRIDE_EQUALS",
        justification = "BarRenderer subclasses do not seem to need to override it")
public class GraphHelper {

    // Semantic success/failure/warning palette, in place of ColorPalette.RED/BLUE/YELLOW.
    private static final Color PASS_COLOR = new Color(0x2E, 0x7D, 0x32); // green
    private static final Color FAIL_COLOR = new Color(0xC6, 0x28, 0x28); // red
    private static final Color SKIP_COLOR = new Color(0xF9, 0xA8, 0x25); // amber
    private static final Color GRID_LINE_COLOR = new Color(0xE0, 0xE0, 0xE0);
    private static final Color AXIS_LINE_COLOR = new Color(0xB0, 0xB6, 0xBE);
    private static final Color LABEL_TEXT_COLOR = new Color(0x3D, 0x42, 0x47);
    private static final Font AXIS_LABEL_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
    private static final Font LEGEND_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 11);

    // "fg"/"grid" request params (read here) let the view recolor the chart per theme.
    private static Color resolveColor(StaplerRequest2 req, String paramName, Color fallback) {
        String value = req.getParameter(paramName);
        if (value == null || value.isEmpty()) {
            return fallback;
        }
        try {
            return Color.decode(value.startsWith("#") ? value : "0x" + value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /** Applies the shared modern axis/legend styling to any plot built here. */
    private static void applyModernAxisStyle(CategoryPlot plot, Color textColor, Color lineColor) {
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(AXIS_LABEL_FONT);
        domainAxis.setTickLabelPaint(textColor);
        domainAxis.setAxisLinePaint(lineColor);
        domainAxis.setTickMarkPaint(lineColor);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLabelFont(AXIS_LABEL_FONT);
        rangeAxis.setTickLabelFont(AXIS_LABEL_FONT);
        rangeAxis.setTickLabelPaint(textColor);
        rangeAxis.setLabelPaint(textColor);
        rangeAxis.setAxisLinePaint(lineColor);
        rangeAxis.setTickMarkPaint(lineColor);
    }

    /** Do not instantiate GraphHelper. */
    private GraphHelper() {}

    public static void redirectWhenGraphUnsupported(StaplerResponse2 rsp, StaplerRequest2 req) throws IOException {
        // not available. send out error message
        rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
    }

    public static JFreeChart createChart(final StaplerRequest2 req, CategoryDataset dataset) {
        final JFreeChart chart = ChartFactory.createStackedAreaChart(
                null, // chart title
                null, // unused
                "Tests Count", // range axis label
                dataset, // data
                PlotOrientation.VERTICAL, // orientation
                true, // include legend
                true, // tooltips
                false // urls
                );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);

        Color textColor = resolveColor(req, "fg", LABEL_TEXT_COLOR);
        Color gridColor = resolveColor(req, "grid", GRID_LINE_COLOR);

        final LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.RIGHT);
        legend.setItemFont(LEGEND_FONT);
        legend.setItemPaint(textColor);
        legend.setBackgroundPaint(null);
        // Chart/plot background not set here: Graph#render() (core) overwrites it via
        // the "graphBg"/"plotBg" request params afterward.

        final CategoryPlot plot = chart.getCategoryPlot();
        plot.setOutlinePaint(null);
        plot.setForegroundAlpha(0.9f);
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(gridColor);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(gridColor);

        CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
        plot.setDomainAxis(domainAxis);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);
        domainAxis.setCategoryMargin(0.0);

        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        applyModernAxisStyle(plot, textColor, resolveColor(req, "grid", AXIS_LINE_COLOR));

        StackedAreaRenderer ar = new StackedAreaRenderer2() {
            @Override
            public String generateURL(CategoryDataset dataset, int row, int column) {
                NumberOnlyBuildLabel label = (NumberOnlyBuildLabel) dataset.getColumnKey(column);
                String path = req.getParameter("rel");
                return (path == null ? "" : path) + label.getRun().getNumber() + "/" + PluginImpl.URL + "/";
            }

            @Override
            public String generateToolTip(CategoryDataset dataset, int row, int column) {
                NumberOnlyBuildLabel label = (NumberOnlyBuildLabel) dataset.getColumnKey(column);
                TestNGTestResultBuildAction report = label.getRun().getAction(TestNGTestResultBuildAction.class);
                if (report == null) {
                    // there are no testng results associated with this build
                    return "";
                }
                switch (row) {
                    case 0:
                        return String.valueOf(report.getFailCount()) + " Failure(s)";
                    case 1:
                        return String.valueOf(report.getTotalCount() - report.getFailCount() - report.getSkipCount())
                                + " Pass";
                    case 2:
                        return String.valueOf(report.getSkipCount()) + " Skip(s)";
                    default:
                        return "";
                }
            }
        };

        plot.setRenderer(ar);
        ar.setSeriesPaint(0, FAIL_COLOR); // Failures
        ar.setSeriesPaint(1, PASS_COLOR); // Pass
        ar.setSeriesPaint(2, SKIP_COLOR); // Skips

        // crop extra space around the graph
        plot.setInsets(new RectangleInsets(0, 0, 0, 5.0));

        return chart;
    }

    /**
     * Creates the graph displayed on Method results page to compare execution duration and status
     * of a test method across builds.
     *
     * <p>At max, 9 older builds are displayed.
     *
     * @param req request
     * @param dataset data set to be displayed on the graph
     * @param statusMap a map with build as key and the test methods execution status (result) as
     *     the value
     * @param methodUrl URL to get to the method from a build test result page
     * @return the chart
     */
    public static JFreeChart createMethodChart(
            StaplerRequest2 req,
            final CategoryDataset dataset,
            final Map<NumberOnlyBuildLabel, String> statusMap,
            final String methodUrl) {

        final JFreeChart chart = ChartFactory.createBarChart(
                null, // chart title
                null, // unused
                "Duration (secs)", // range axis label
                dataset, // data
                PlotOrientation.VERTICAL, // orientation
                true, // include legend
                true, // tooltips
                true // urls
                );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);
        // Chart/plot background intentionally not set -- see createChart() above.
        chart.removeLegend();

        Color textColor = resolveColor(req, "fg", LABEL_TEXT_COLOR);
        Color gridColor = resolveColor(req, "grid", GRID_LINE_COLOR);
        Color axisLineColor = resolveColor(req, "grid", AXIS_LINE_COLOR);

        final CategoryPlot plot = chart.getCategoryPlot();
        plot.setOutlinePaint(null);
        plot.setForegroundAlpha(0.9f);
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(gridColor);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(gridColor);

        CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
        plot.setDomainAxis(domainAxis);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);
        domainAxis.setCategoryMargin(0.0);

        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        applyModernAxisStyle(plot, textColor, axisLineColor);

        BarRenderer br = new BarRenderer() {

            Map<String, Paint> statusPaintMap = new HashMap<String, Paint>();

            {
                statusPaintMap.put("PASS", PASS_COLOR);
                statusPaintMap.put("SKIP", SKIP_COLOR);
                statusPaintMap.put("FAIL", FAIL_COLOR);
            }

            /**
             * Returns the paint for an item. Overrides the default behavior inherited from
             * AbstractSeriesRenderer.
             *
             * @param row the series.
             * @param column the category.
             * @return The item color.
             */
            public Paint getItemPaint(final int row, final int column) {
                NumberOnlyBuildLabel label = (NumberOnlyBuildLabel) dataset.getColumnKey(column);
                Paint paint = statusPaintMap.get(statusMap.get(label));
                // when the status of test method is unknown, use gray color
                return paint == null ? Color.gray : paint;
            }
        };

        br.setBaseToolTipGenerator(new CategoryToolTipGenerator() {
            public String generateToolTip(CategoryDataset dataset, int row, int column) {
                NumberOnlyBuildLabel label = (NumberOnlyBuildLabel) dataset.getColumnKey(column);
                if ("UNKNOWN".equals(statusMap.get(label))) {
                    return "unknown";
                }
                // values are in seconds
                float value = dataset.getValue(row, column).floatValue();
                DecimalFormat df = new DecimalFormat("0.000");
                return df.format(value) + " secs";
            }
        });

        br.setBaseItemURLGenerator(new CategoryURLGenerator() {
            public String generateURL(CategoryDataset dataset, int series, int category) {
                NumberOnlyBuildLabel label = (NumberOnlyBuildLabel) dataset.getColumnKey(category);
                if ("UNKNOWN".equals(statusMap.get(label))) {
                    // no link when method result doesn't exist
                    return null;
                }
                return getUpUrl(label.getRun()) + label.getRun().getNumber() + methodUrl;
            }
        });

        br.setItemMargin(0.0);
        br.setMinimumBarLength(5);
        // set the base to be 1/100th of the maximum value displayed in the graph
        br.setBase(br.findRangeBounds(dataset).getUpperBound() / 100);
        // Flat fill, no gradient/shadow, in place of JFreeChart's default 3D-ish look.
        br.setBarPainter(new StandardBarPainter());
        br.setShadowVisible(false);
        plot.setRenderer(br);

        // crop extra space around the graph
        plot.setInsets(new RectangleInsets(0, 0, 0, 5.0));
        return chart;
    }

    /** cf. {@link AbstractBuild#getUpUrl} */
    private static String getUpUrl(Run<?, ?> run) {
        return Functions.getNearestAncestorUrl(Stapler.getCurrentRequest2(), run.getParent()) + '/';
    }
}
