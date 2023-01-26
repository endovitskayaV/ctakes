package lact;

import com.lexicalscope.jewel.cli.CliFactory;
import org.apache.ctakes.core.config.ConfigParameterConstants;
import org.apache.ctakes.core.pipeline.CliOptionals;
import org.apache.ctakes.core.pipeline.PipelineBuilder;
import org.apache.ctakes.core.pipeline.PiperFileReader;
import org.apache.ctakes.core.pipeline.StandardCliOptions;
import org.apache.ctakes.coreference.factory.CoreferenceAnnotatorFactory;
import org.apache.ctakes.dictionary.lookup2.ae.JCasTermAnnotator;

final public class ClinicalRunner {

    /**
     * @param args <p>
     *             <i>-i</i> input directory path with texts to process
     *             <p>
     *             <i>--xmiOut</i> output directory path, where .xmi files will be saved
     *             <p>
     *             <i>-l</i> optional dictionary path, default  {@link JCasTermAnnotator.DEFAULT_DICT_DESC_PATH}
     *             <p>
     *             other available options: {@link StandardCliOptions}
     */
    public static void main(final String... args) throws Exception {
        final CliOptionals options = CliFactory.parseArguments(CliOptionals.class, args);
        final PiperFileReader reader = new PiperFileReader();
        final PipelineBuilder builder = reader.getBuilder();
        builder.set(ConfigParameterConstants.PARAM_INPUTDIR, options.getInputDirectory());
        builder.set(ConfigParameterConstants.PARAM_OUTPUTDIR, options.getXmiOutDirectory());
        reader.setCliOptionals(options);
        reader.loadPipelineFile("pipers/Full.piper");
        builder.readFiles(options.getInputDirectory());
        builder.writeXMIs(options.getXmiOutDirectory());
        builder.run();
    }
}
