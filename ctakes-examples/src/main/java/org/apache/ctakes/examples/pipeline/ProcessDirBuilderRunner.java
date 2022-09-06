package org.apache.ctakes.examples.pipeline;

import com.lexicalscope.jewel.cli.CliFactory;
import org.apache.ctakes.assertion.medfacts.cleartk.GenericCleartkAnalysisEngine;
import org.apache.ctakes.assertion.medfacts.cleartk.HistoryCleartkAnalysisEngine;
import org.apache.ctakes.assertion.medfacts.cleartk.PolarityCleartkAnalysisEngine;
import org.apache.ctakes.assertion.medfacts.cleartk.SubjectCleartkAnalysisEngine;
import org.apache.ctakes.assertion.medfacts.cleartk.UncertaintyCleartkAnalysisEngine;
import org.apache.ctakes.chunker.ae.Chunker;
import org.apache.ctakes.chunker.ae.adjuster.ChunkAdjuster;
import org.apache.ctakes.clinicalpipeline.ae.ExtractionPrepAnnotator;
import org.apache.ctakes.constituency.parser.ae.ConstituencyParser;
import org.apache.ctakes.contexttokenizer.ae.ContextDependentTokenizerAnnotator;
import org.apache.ctakes.core.ae.SentenceDetector;
import org.apache.ctakes.core.ae.SimpleSegmentAnnotator;
import org.apache.ctakes.core.ae.TokenizerAnnotator;
import org.apache.ctakes.core.ae.TokenizerAnnotatorPTB;
import org.apache.ctakes.core.config.ConfigParameterConstants;
import org.apache.ctakes.core.pipeline.CliOptionals;
import org.apache.ctakes.core.pipeline.PipelineBuilder;
import org.apache.ctakes.core.pipeline.PiperFileReader;
import org.apache.ctakes.dependency.parser.ae.ClearNLPDependencyParserAE;
import org.apache.ctakes.dependency.parser.ae.ClearNLPSemanticRoleLabelerAE;
import org.apache.ctakes.dictionary.lookup2.ae.DefaultJCasTermAnnotator;
import org.apache.ctakes.drugner.ae.DrugMentionAnnotator;
import org.apache.ctakes.lvg.ae.LvgAnnotator;
import org.apache.ctakes.postagger.POSTagger;

import java.util.Arrays;
import java.util.HashSet;

/**
 * @author SPF , chip-nlp
 * @version %I%
 * @since 10/12/2016
 */
final public class ProcessDirBuilderRunner {
    public static void main(final String... args) throws Exception {
        final CliOptionals options = CliFactory.parseArguments(CliOptionals.class, args);
        final PiperFileReader reader = new PiperFileReader();
        final PipelineBuilder builder = reader.getBuilder();
        builder.set(ConfigParameterConstants.PARAM_INPUTDIR, options.getInputDirectory());
        builder.set(ConfigParameterConstants.PARAM_OUTPUTDIR, options.getXmiOutDirectory());
        builder
                .addDescription(SimpleSegmentAnnotator.createAnnotatorDescription("SimpleSegmentAnnotator/SegmentID"))
                .addDescription(SentenceDetector.createAnnotatorDescription())
                .addDescription(TokenizerAnnotatorPTB.createAnnotatorDescription())
                .addDescription(LvgAnnotator.createAnnotatorDescription())
                .addDescription(ContextDependentTokenizerAnnotator.createAnnotatorDescription())
                .addDescription(POSTagger.createAnnotatorDescription())
                .addDescription(Chunker.createAnnotatorDescription())
                .addDescription(ChunkAdjuster.createAnnotatorDescription(new String[]{"NP", "NP"}, 1))
                .addDescription(ChunkAdjuster.createAnnotatorDescription(new String[]{"NP", "PP", "NP"}, 2))
                .addDescription(DefaultJCasTermAnnotator.createAnnotatorDescription("org.apache.ctakes.typesystem.type.textspan.Sentence", "VB,VBD,VBG,VBN,VBP,VBZ,CC,CD,DT,EX,IN,LS,MD,PDT,POS,PP,PP$,PRP,PRP$,RP,TO,WDT,WP,WPS,WRB", 3, "org/apache/ctakes/dictionary/lookup/fast/sno_rx_16ab.xml"))
                .addDescription(DrugMentionAnnotator.createAnnotatorDescription("1", "org.apache.ctakes.typesystem.type.textspan.Sentence", "org.apache.ctakes.typesystem.type.textspan.Sentence"))
                .addDescription(ClearNLPDependencyParserAE.createAnnotatorDescription())
                .addDescription(ClearNLPSemanticRoleLabelerAE.createAnnotatorDescription())
                .addDescription(ConstituencyParser.createAnnotatorDescription())
                .addDescription(GenericCleartkAnalysisEngine.createAnnotatorDescription())
                .addDescription(HistoryCleartkAnalysisEngine.createAnnotatorDescription())
                .addDescription(PolarityCleartkAnalysisEngine.createAnnotatorDescription())
                .addDescription(SubjectCleartkAnalysisEngine.createAnnotatorDescription())
                .addDescription(UncertaintyCleartkAnalysisEngine.createAnnotatorDescription())
                .addDescription(ExtractionPrepAnnotator.createAnnotatorDescription(2, "ANNOTATION_VERSION"));

        builder.readFiles(options.getInputDirectory());
        builder.writeXMIs(options.getXmiOutDirectory());
        builder.run();
    }

}
