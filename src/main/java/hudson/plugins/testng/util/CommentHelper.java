package hudson.plugins.testng.util;

import org.apache.commons.lang.StringUtils;

/**
 * Helper class for test methods comments
 */
public class CommentHelper
{
	private String parentSuiteName;
	private String parentTestName;
	private String parentName;
	private String testName;
	private String comment;
	
	public CommentHelper(String parentSuiteName, String parentTestName, String parentName, String testName, String comment)
	{
		this.parentSuiteName = parentSuiteName;
		this.parentTestName = parentTestName;
		this.parentName = parentName;
		this.testName = testName;
		this.comment = comment;
	}
	
	/**
	 * Get comment for selected test method. 
	 * @param field parameter name (should be in form "testsuite--parenttest--classname--methodname--comment"
	 * @param value comment
	 * @return parsed comment
	 */
	public static CommentHelper getCommentHelperFromParameter(String field, String value)
	{
		String [] fields = StringUtils.split(field, "--");
		if (fields != null && fields.length == 5)
		{
			return new CommentHelper(fields[0], fields[1], fields[2], fields[3], value);
		}
		return null;
	}
	
	public String getParentSuiteName()
	{
		return parentSuiteName;
	}
	
	public String getParentTestName()
	{
		return parentTestName;
	}
	
	public String getParentName()
	{
		return parentName;
	}
	
	public String getTestName()
	{
		return testName;
	}
	
	public String getComment()
	{
		return comment;
	}
	
	@Override
	public String toString()
	{
		return "CommentHelper [parentSuiteName=" + parentSuiteName + ", parentTestName=" + parentTestName 
				+ ", parentName=" + parentName + ", testName=" + testName + ", comment=" + comment + "]";
	}
	
	
}
