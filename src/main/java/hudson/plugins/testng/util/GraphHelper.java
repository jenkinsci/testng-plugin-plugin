package hudson.plugins.testng.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Functions;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import hudson.plugins.testng.PluginImpl;
import hudson.plugins.testng.TestNGTestResultBuildAction;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;
import hudson.util.ColorPalette;
import hudson.util.ShiftedCategoryAxis;
import hudson.util.StackedAreaRenderer2;
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
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Helper class for trend graph generation
 *
 */
@SuppressFBWarnings(value="EQ_DOESNT_OVERRIDE_EQUALS", justification="BarRenderer subclasses do not seem to need to override it")
public class GraphHelper {

   /**
    * Do not instantiate GraphHelper.
    */
   private GraphHelper() {}

   public static void redirectWhenGraphUnsupported(StaplerResponse rsp, StaplerRequest req) throws IOException {
      // not available. send out error message
      rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
   }

   public static JFreeChart createChart(final StaplerRequest req, CategoryDataset dataset) {
      final JFreeChart chart = ChartFactory.createStackedAreaChart(
          null,                     // chart title
          null,                     // unused
          "Tests Count",            // range axis label
          dataset,                  // data
          PlotOrientation.VERTICAL, // orientation
          true,                     // include legend
          true,                     // tooltips
          false                     // urls
      );

      // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
      final LegendTitle legend = chart.getLegend();
      legend.setPosition(RectangleEdge.RIGHT);

      chart.setBackgroundPaint(Color.white);

      final CategoryPlot plot = chart.getCategoryPlot();
      plot.setBackgroundPaint(Color.WHITE);
      plot.setOutlinePaint(null);
      plot.setForegroundAlpha(0.8f);
      plot.setDomainGridlinesVisible(true);
      plot.setDomainGridlinePaint(Color.white);
      plot.setRangeGridlinesVisible(true);
      plot.setRangeGridlinePaint(Color.black);

      CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
      plot.setDomainAxis(domainAxis);
      domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
      domainAxis.setLowerMargin(0.0);
      domainAxis.setUpperMargin(0.0);
      domainAxis.setCategoryMargin(0.0);

      final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
      rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

      StackedAreaRenderer ar = new StackedAreaRenderer2() {
          @Override
          public String generateURL(CategoryDataset dataset, int row, int column) {
              NumberOnlyBuildLabel label = (NumberOnlyBuildLabel) dataset.getColumnKey(column);
              String path = req.getParameter("rel");
              return  (path == null ? "" : path) + label.getRun().getNumber() + "/" + PluginImpl.URL + "/";
          }

          @Override
          public String generateToolTip(CategoryDataset dataset, int row, int column) {
              NumberOnlyBuildLabel label = (NumberOnlyBuildLabel) dataset.getColumnKey(column);
              TestNGTestResultBuildAction report = label.getRun().getAction(TestNGTestResultBuildAction.class);
              if (report == null) {
                 //there are no testng results associated with this build
                 return "";
              }
              switch (row) {
                  case 0:
                      return String.valueOf(report.getFailCount()) + " Failure(s)";
                  case 1:
                     return String.valueOf(report.getTotalCount() - report.getFailCount() - report.getSkipCount()) + " Pass";
                  case 2:
                     return String.valueOf(report.getSkipCount()) + " Skip(s)";
                  default:
                     return "";
              }
          }
      };

      plot.setRenderer(ar);
      ar.setSeriesPaint(0, ColorPalette.RED); // Failures
      ar.setSeriesPaint(1, ColorPalette.BLUE); // Pass
      ar.setSeriesPaint(2, ColorPalette.YELLOW); // Skips

      // crop extra space around the graph
      plot.setInsets(new RectangleInsets(0, 0, 0, 5.0));

      return chart;
   }

   /**
    * Creates the graph displayed on Method results page to compare execution duration
    * and status of a test method across builds.
    *
    * At max, 9 older builds are displayed.
    *
    * @param req request
    * @param dataset data set to be displayed on the graph
    * @param statusMap a map with build as key and the test methods execution status (result)
    *                   as the value
    * @param methodUrl URL to get to the method from a build test result page
    * @return the chart
    */
   public static JFreeChart createMethodChart(StaplerRequest req, final CategoryDataset dataset,
            final Map<NumberOnlyBuildLabel, String> statusMap, final String methodUrl) {

      final JFreeChart chart = ChartFactory.createBarChart(
          null,                     // chart title
          null,                     // unused
          "Duration (secs)",// range axis label
          dataset,                  // data
          PlotOrientation.VERTICAL, // orientation
          true,                     // include legend
          true,                     // tooltips
          true                      // urls
      );

      // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
      chart.setBackgroundPaint(Color.white);
      chart.removeLegend();

      final CategoryPlot plot = chart.getCategoryPlot();
      plot.setBackgroundPaint(Color.WHITE);
      plot.setOutlinePaint(null);
      plot.setForegroundAlpha(0.8f);
      plot.setDomainGridlinesVisible(true);
      plot.setDomainGridlinePaint(Color.white);
      plot.setRangeGridlinesVisible(true);
      plot.setRangeGridlinePaint(Color.black);

      CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
      plot.setDomainAxis(domainAxis);
      domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
      domainAxis.setLowerMargin(0.0);
      domainAxis.setUpperMargin(0.0);
      domainAxis.setCategoryMargin(0.0);

      final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
      rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

      BarRenderer br = new BarRenderer() {

         Map<String, Paint> statusPaintMap = new HashMap<String, Paint>();

         {
            statusPaintMap.put("PASS", ColorPalette.BLUE);
            statusPaintMap.put("SKIP", ColorPalette.YELLOW);
            statusPaintMap.put("FAIL", ColorPalette.RED);
         }

         /**
          * Returns the paint for an item.  Overrides the default behavior inherited from
          * AbstractSeriesRenderer.
          *
          * @param row  the series.
          * @param column  the category.
          *
          * @return The item color.
          */
         public Paint getItemPaint(final int row, final int column) {
            NumberOnlyBuildLabel label = (NumberOnlyBuildLabel) dataset.getColumnKey(column);
            Paint paint = statusPaintMap.get(statusMap.get(label));
            //when the status of test method is unknown, use gray color
            return paint == null ? Color.gray : paint;
         }
      };

      br.setBaseToolTipGenerator(new CategoryToolTipGenerator()
      {
         public String generateToolTip(CategoryDataset dataset, int row, int column)
         {
            NumberOnlyBuildLabel label = (NumberOnlyBuildLabel) dataset.getColumnKey(column);
            if ("UNKNOWN".equals(statusMap.get(label))) {
               return "unknown";
            }
            //values are in seconds
            float value = dataset.getValue(row, column).floatValue();
            DecimalFormat df = new DecimalFormat("0.000");
            return  df.format(value) + " secs";
         }
      });

      br.setBaseItemURLGenerator(new CategoryURLGenerator()
      {
         public String generateURL(CategoryDataset dataset, int series, int category)
         {
            NumberOnlyBuildLabel label = (NumberOnlyBuildLabel) dataset.getColumnKey(category);
            if ("UNKNOWN".equals(statusMap.get(label))) {
               //no link when method result doesn't exist
               return null;
            }
            return getUpUrl(label.getRun()) + label.getRun().getNumber() + methodUrl;
         }
      });

      br.setItemMargin(0.0);
      br.setMinimumBarLength(5);
      //set the base to be 1/100th of the maximum value displayed in the graph
      br.setBase(br.findRangeBounds(dataset).getUpperBound() / 100);
      plot.setRenderer(br);

      // crop extra space around the graph
      plot.setInsets(new RectangleInsets(0, 0, 0, 5.0));
      return chart;
  }

   /** cf. {@link AbstractBuild#getUpUrl} */
   private static String getUpUrl(Run<?,?> run) {
       return Functions.getNearestAncestorUrl(Stapler.getCurrentRequest(), run.getParent()) + '/';
   }

}
