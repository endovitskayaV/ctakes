package lact;

import com.lexicalscope.jewel.cli.CliFactory;
import org.apache.ctakes.core.config.ConfigParameterConstants;
import org.apache.ctakes.core.pipeline.CliOptionals;
import org.apache.ctakes.core.pipeline.PipelineBuilder;
import org.apache.ctakes.core.pipeline.PiperFileReader;
import org.apache.ctakes.core.pipeline.StandardCliOptions;

final public class ClinicalRunner {

    /**
     * @param args {@link StandardCliOptions}
     */
    public static void main(final String... args) throws Exception {
        final CliOptionals options = CliFactory.parseArguments(CliOptionals.class, args);
        final PiperFileReader reader = new PiperFileReader();
        final PipelineBuilder builder = reader.getBuilder();
        builder.set(ConfigParameterConstants.PARAM_INPUTDIR, options.getInputDirectory());
        builder.set(ConfigParameterConstants.PARAM_OUTPUTDIR, options.getXmiOutDirectory());
        reader.loadPipelineFile(options.getPiperPath());
        builder.readFiles(options.getInputDirectory());
        builder.writeXMIs(options.getXmiOutDirectory());
        builder.run();
    }
}
