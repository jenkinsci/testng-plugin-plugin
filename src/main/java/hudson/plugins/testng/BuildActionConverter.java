package hudson.plugins.testng;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import hudson.model.AbstractBuild;

/**
 * Here for backward compatibility. Unmarshals the build action in a
 * backward compatible manner and marshals it out as well.
 * <p/>
 * <p>(Note: Not sure if this is the best approach, but I couldn't find
 * another that could deal nicely with {@link hudson.tasks.test.AbstractTestResultAction#owner}
 * which is defined as final)</p>
 *
 * @author nullin
 */
public class BuildActionConverter implements Converter {

    /**
     * Create the converter
     */
    public BuildActionConverter() {
    }

    public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
        return TestNGTestResultBuildAction.class == type;
    }

    /**
     * Marshals out the {@link TestNGTestResultBuildAction} object.
     * <p/>
     * <p>We are only marshaling out the {@link hudson.tasks.test.AbstractTestResultAction#owner}
     * of the action. We (currently) don't care about
     * {@link hudson.tasks.test.AbstractTestResultAction#descriptions}</p>
     *
     * @param source  source
     * @param writer  writer
     * @param context context
     */
    public void marshal(Object source, HierarchicalStreamWriter writer,
                        MarshallingContext context) {
        TestNGTestResultBuildAction action = (TestNGTestResultBuildAction) source;
        writer.startNode("owner");
        context.convertAnother(action.owner);
        writer.endNode();
    }

    /**
     * Unmarshals the TestNG build action in a backward compatible way.
     * <p/>
     * It's needed so that we can deal with builds that have were run
     * using (the now deleted) TestNGBuildAction class
     *
     * @param reader  reader
     * @param context context
     * @return unmarshalled object
     */
    public Object unmarshal(final HierarchicalStreamReader reader,
                            final UnmarshallingContext context) {
        TestNGTestResultBuildAction action = new TestNGTestResultBuildAction(null, null);
        reader.moveDown();
        Class type = context.getRequiredType();
        AbstractBuild build = (AbstractBuild) context.convertAnother(action, type);
        reader.moveUp();
        action = new TestNGTestResultBuildAction(build, action.getResult(build));
        return action.readResolve();
    }
}
