package hudson.plugins.testng;

import hudson.FilePath;
import hudson.model.Action;
import hudson.model.AbstractBuild;
import hudson.model.Api;
import hudson.model.Descriptor.FormException;
import hudson.plugins.testng.parser.ResultsParser;
import hudson.plugins.testng.results.TestResults;
import hudson.plugins.testng.util.CommentHelper;
import hudson.plugins.testng.util.TestResultHistoryUtil;

import java.io.*;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;

import net.sf.json.JSONException;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class TestNGBuildAction implements Action, Serializable {

   /**
    * Unique identifier for this class.
    */
   private static final long serialVersionUID = 31415926L;

   /**
    * The owner of this Action.
    */
   private final AbstractBuild<?, ?> build;

   /**
    * @deprecated since v0.23. Only here for supporting older version of this plug-in
    */
   private transient TestResults results;
   private transient SoftReference<TestResults> testResults;

   public TestNGBuildAction(AbstractBuild<?, ?> build, TestResults testngResults) {
      this.build = build;
      testngResults.setOwner(this.build);
      this.testResults = new SoftReference<TestResults>(testngResults);
   }
   
   public synchronized void doSaveComments(StaplerRequest req,
           StaplerResponse rsp) throws IOException, ServletException, FormException 
   {
	   try 
	   {
           List<CommentHelper> commentLists = getCommentHelperList(req);
           FilePath testngDir = Publisher.getTestNGReport(getBuild());
           
           	File fXmlFile = getResutlXMLFile(testngDir);
       		Document doc = getDocument(fXmlFile);
        
       		addComments(commentLists, doc);
       		saveXMLResults(fXmlFile, doc);
       		testResults.clear();
       		rsp.sendRedirect(".");
       }
	   catch (Exception e) 
	   {
           StringWriter sw = new StringWriter();
           PrintWriter pw = new PrintWriter(sw);
           pw.println("Failed to parse form data. Please report this problem as a bug");
           pw.println("JSON=" + req.getSubmittedForm());
           pw.println();
           e.printStackTrace(pw);

           pw.close();
           rsp.sendError(StaplerResponse.SC_BAD_REQUEST, e.getMessage());
       }
   }

	private void saveXMLResults(File fXmlFile, Document doc) throws TransformerFactoryConfigurationError,
			TransformerConfigurationException, TransformerException
	{
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(fXmlFile);
		transformer.transform(source, result);
	}
	
	private void addComments(List<CommentHelper> commentLists, Document doc) throws XPathExpressionException
	{
		XPath xpath = XPathFactory.newInstance().newXPath();
		for (CommentHelper commentHelper : commentLists)
		{
			String xpathExp = "/testng-results/suite[@name='" + commentHelper.getParentSuiteName() 
					+ "']/test[@name='" + commentHelper.getParentTestName() 
					+ "']/class[@name='" + commentHelper.getParentName() + "']/test-method[@name='" + commentHelper.getTestName() + "']";
			Element result = (Element) xpath.evaluate(xpathExp, doc, XPathConstants.NODE);
			if (result != null)
			{
				Element exception = (Element)result.getElementsByTagName("exception").item(0);
				if (exception != null)
				{
					Element comment = (Element)exception.getElementsByTagName("comment").item(0);
					if (comment == null)
					{
						comment = doc.createElement("comment");
						exception.appendChild(comment);
					}
					else
					{
						comment.setTextContent("");
					}
					comment.appendChild(doc.createCDATASection(commentHelper.getComment()));
				}
			}
			
		}
	}
	
	private Document getDocument(File fXmlFile) throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
		return doc;
	}
	
	private File getResutlXMLFile(FilePath testngDir)
	{
		FilePath path = testngDir.child("testng-results.xml");
		File fXmlFile = new File(path.getRemote());
		return fXmlFile;
	}
	
	private List<CommentHelper> getCommentHelperList(StaplerRequest req)
	{
		Enumeration<String> enums = req.getParameterNames();
		List<CommentHelper> commentLists = new ArrayList<CommentHelper>();
		while (enums.hasMoreElements())
		{
			String parameterName = enums.nextElement();
			if (!"submit".equalsIgnoreCase(parameterName) && !"json".equalsIgnoreCase(parameterName))
			{
				String value = req.getParameter(parameterName);
				CommentHelper commentHelper =
						CommentHelper.getCommentHelperFromParameter(parameterName, value);
				if (commentHelper != null)
				{
					commentLists.add(commentHelper);
				}
			}
		}
		return commentLists;
	}

   /**
    * Getter for property 'build'.
    *
    * @return Value for property 'build'.
    */
   public synchronized AbstractBuild<?, ?> getBuild() {
      return build;
   }

   /**
    * Override to control when the floating box should be displayed.
    *
    * @return <code>true</code> if the floating box should be visible.
    */
   public boolean isFloatingBoxActive() {
      return true;
   }

   /**
    * Override to control when the action displays a trend graph.
    *
    * @return <code>true</code> if the action should show a trend graph.
    */
   public boolean isGraphActive() {
      return false;
   }

   public TestResults getResults() {
      if (results == null) {
        if (testResults == null) {
           testResults = new SoftReference<TestResults>(loadResults(getBuild()));
           return testResults.get();
        }

        TestResults tr = testResults.get();
        if (tr == null) {
           testResults = new SoftReference<TestResults>(loadResults(getBuild()));
          return testResults.get();
        } else {
          return tr;
        }
      } else {
        return results;
      }
   }
   
   public boolean isCommentColumn()
   {
	   TestNGProjectAction projAction = getBuild().getProject().getAction(TestNGProjectAction.class);
	   return projAction.getCommentColumn();
   }

   static TestResults loadResults(AbstractBuild<?, ?> owner)
   {
      FilePath testngDir = Publisher.getTestNGReport(owner);
     
      FilePath[] paths = null;
      try {
         paths = testngDir.list("testng-results*.xml");
      } catch (Exception e) {
         //do nothing
      }

      TestResults tr = null;
      if (paths == null) {
        tr = new TestResults("");
        tr.setOwner(owner);
        return tr;
      }

      ResultsParser parser = new ResultsParser();
      TestResults result = parser.parse(paths);
      result.setOwner(owner);
      return result;
   }

   public TestResults getPreviousResults() {
      AbstractBuild<?, ?> previousBuild = getBuild().getPreviousBuild();
      while (previousBuild != null && previousBuild.getAction(getClass()) == null) {
         previousBuild = previousBuild.getPreviousBuild();
      }
      if (previousBuild == null) {
         return new TestResults("");
      } else {
         TestNGBuildAction action = previousBuild.getAction(getClass());
         return action.getResults();
      }
   }

   /**
    * The summary of this build report for display on the build index page.
    *
    * @return
    */
   public String getSummary() {
      return TestResultHistoryUtil.toSummary(this);
   }

   /**
    * {@inheritDoc}
    */
   public String getIconFileName() {
      return PluginImpl.ICON_FILE_NAME;
   }

   /**
    * {@inheritDoc}
    */
   public String getDisplayName() {
      return PluginImpl.DISPLAY_NAME;
   }

   /**
    * {@inheritDoc}
    */
   public String getUrlName() {
      return PluginImpl.URL;
   }

   public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
      return getResults().getDynamic(token, req, rsp);
   }

   public Api getApi() {
      return new Api(getResults());
   }
}
