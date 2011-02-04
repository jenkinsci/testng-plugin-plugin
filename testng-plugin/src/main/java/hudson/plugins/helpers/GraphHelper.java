package hudson.plugins.helpers;

import hudson.plugins.testng.BuildIndividualReport;
import hudson.util.ChartUtil;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;
import hudson.util.ColorPalette;
import hudson.util.ShiftedCategoryAxis;
import hudson.util.StackedAreaRenderer2;

import java.awt.Color;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Helper class for trend graph generation
 *
 */
public class GraphHelper {

   /**
    * Do not instantiate GraphHelper.
    */
   private GraphHelper() {}

   /**
    * Getter for property 'graphUnsupported'.
    *
    * @return Value for property 'graphUnsupported'.
    */
   public static boolean isGraphUnsupported() {
      return ChartUtil.awtProblemCause != null;
   }

   public static void redirectWhenGraphUnsupported(StaplerResponse rsp, StaplerRequest req) throws IOException {
      // not available. send out error message
      rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
   }

   public static JFreeChart createChart(StaplerRequest req, CategoryDataset dataset) {

      final String relPath = getRelPath(req);

      final JFreeChart chart = ChartFactory.createStackedAreaChart(
          null,                     // chart title
          null,                     // unused
          "Test count",             // range axis label
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
              return relPath + label.build.getNumber() + "/testngreports/";
          }

          @Override
          public String generateToolTip(CategoryDataset dataset, int row, int column) {
              NumberOnlyBuildLabel label = (NumberOnlyBuildLabel) dataset.getColumnKey(column);
              BuildIndividualReport report = label.build.getAction(BuildIndividualReport.class);
              switch (row) {
                  case 0:
                      return String.valueOf(report.getResults().getFailedTestCount()) + " Failure(s)";
                  case 1:
                     return String.valueOf(report.getResults().getPassedTestCount()) + " Pass";
                  case 2:
                     return String.valueOf(report.getResults().getSkippedTestCount()) + " Skip(s)";
                  default:
                     return "";
              }
          }
      };

      plot.setRenderer(ar);
      ar.setSeriesPaint(0,ColorPalette.RED); // Failures.
      ar.setSeriesPaint(1,ColorPalette.BLUE); // Pass.
      ar.setSeriesPaint(2,ColorPalette.YELLOW); // Skips.

      // crop extra space around the graph
      plot.setInsets(new RectangleInsets(0,0,0,5.0));

      return chart;
  }

   private static String getRelPath(StaplerRequest req) {
      String relPath = req.getParameter("rel");
      if(relPath==null)   return "";
      return relPath;
  }
}
