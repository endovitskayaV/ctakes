package org.apache.ctakes.core.ae;

import org.apache.ctakes.core.pipeline.PipeBitInfo;
import org.apache.ctakes.core.util.external.SystemUtil;
import org.apache.ctakes.core.util.log.DotLogger;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;
import java.io.IOException;

/**
 * @author SPF , chip-nlp
 * @since {5/4/2022}
 */
@PipeBitInfo(
      name = "CommandRunner",
      description = "Runs an external process.",
      role = PipeBitInfo.Role.SPECIAL
)
public class CommandRunner extends JCasAnnotator_ImplBase {

   static private final Logger LOGGER = Logger.getLogger( "CommandRunner" );

   // to add a configuration parameter, type "param" and hit tab.
   static public final String CMD_PARAM = "Command";
   static public final String CMD_DESC = "A full command line to be executed. Make sure to quote.";
   @ConfigurationParameter(
         name = CMD_PARAM,
         description = CMD_DESC
   )
   private String _cmd;

   static public final String CMD_DIR_PARAM = "CommandDir";
   static public final String CMD_DIR_DESC = "The Command Executable's directory.";
   @ConfigurationParameter(
         name = CMD_DIR_PARAM,
         description = CMD_DIR_DESC,
         mandatory = false
   )
   private String _cmdDir;

   static public final String DIR_PARAM = "Dir";
   static public final String DIR_DESC = "The Command's root directory.";
   @ConfigurationParameter(
         name = DIR_PARAM,
         description = DIR_DESC,
         mandatory = false
   )
   private String _dir;


   static public final String PER_DOC_PARAM = "PerDoc";
   static public final String PER_DOC_DESC = "yes to run the command once per document. Default is no.";
   @ConfigurationParameter(
         name = PER_DOC_PARAM,
         description = PER_DOC_DESC,
         defaultValue = "no",
         mandatory = false
   )
   private String _perDoc;

   static public final String PAUSE_PARAM = "Pause";
   static public final String PAUSE_DESC = "Pause for some seconds after launching.  Default is 0";
   @ConfigurationParameter(
         name = PAUSE_PARAM,
         description = PAUSE_DESC,
         mandatory = false
   )
   private int _pause = 0;

   static public final String WAIT_PARAM = "Wait";
   static public final String WAIT_DESC = "Wait for the launched command to finish.  Default is no.";
   @ConfigurationParameter(
         name = WAIT_PARAM,
         description = WAIT_DESC,
         defaultValue = "no",
         mandatory = false
   )
   private String _wait;

   static public final String LOG_NAME_PARAM = "Log";
   static public final String LOG_NAME_DESC = "A name for the streaming logger.  Default is the Command.";
   @ConfigurationParameter(
         name = LOG_NAME_PARAM,
         description = LOG_NAME_DESC,
         mandatory = false
   )
   private String _logName;


   static public final String LOG_FILE_PARAM = "LogFile";
   static public final String LOG_FILE_DESC = "File to which the command's output should be sent.  This overrides Log.";
   @ConfigurationParameter(
         name = LOG_FILE_PARAM,
         description = LOG_FILE_DESC,
         mandatory = false
   )
   private String _logFile;


   /**
    * {@inheritDoc}
    */
   @Override
   public void initialize( final UimaContext context ) throws ResourceInitializationException {
      super.initialize( context );
      _cmdDir = SystemUtil.subVariableParameters( _cmdDir, context );
      _dir = SystemUtil.subVariableParameters( _dir, context );
      _cmd = SystemUtil.subVariableParameters( _cmd, context );
      if ( _cmdDir != null && !_cmdDir.isEmpty() && !new File( _cmdDir ).exists() ) {
         LOGGER.warn( "Cannot find Command Directory " + _cmdDir );
      }
      if ( _dir != null && !_dir.isEmpty() && !new File( _dir ).exists() ) {
         LOGGER.warn( "Cannot find Directory " + _dir );
      }
      if ( _perDoc.equalsIgnoreCase( "yes" ) || _perDoc.equalsIgnoreCase( "true" ) ) {
         return;
      }
      try {
         runCommand();
      } catch ( IOException ioE ) {
         throw new ResourceInitializationException( ioE );
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void process( final JCas jcas ) throws AnalysisEngineProcessException {
      if ( !_perDoc.equalsIgnoreCase( "yes" ) && !_perDoc.equalsIgnoreCase( "true" ) ) {
         return;
      }
      try {
         runCommand();
      } catch ( IOException ioE ) {
         throw new AnalysisEngineProcessException( ioE );
      }
   }


   private void runCommand() throws IOException {
      final String command = ( _cmdDir == null || _cmdDir.isEmpty() ) ? _cmd : _cmdDir + File.separator + _cmd;
      final SystemUtil.CommandRunner runner = new SystemUtil.CommandRunner( command );
      if ( _logFile != null && !_logFile.isEmpty() ) {
         runner.setLogFiles( _logFile, _logFile );
      } else {
         final Logger logger = getRunLogger();
         runner.setLogger( logger );
      }
      if ( _wait.equalsIgnoreCase( "yes" ) || _wait.equalsIgnoreCase( "true" ) ) {
         runner.wait( true );
      }
      if ( _dir != null && !_dir.isEmpty() ) {
         runner.setDirectory( _dir );
      }
      LOGGER.info( "Running " + command + " ..." );
      if ( _logFile != null && !_logFile.isEmpty() ) {
         LOGGER.info( "Log File is " + _logFile );
      }
      SystemUtil.run( runner );
      if ( _pause < 1 ) {
         return;
      }
      final long pause = _pause * 1000L;
      LOGGER.info( "Pausing " + _pause + " seconds ..." );
      try ( DotLogger dotter = new DotLogger() ) {
         Thread.sleep( pause );
      } catch ( IOException | InterruptedException multE ) {
         // do nothing
      }
   }

   private Logger getRunLogger() {
      if ( _logName != null && !_logName.isEmpty() ) {
         return Logger.getLogger( _logName );
      }
      final int spaceIndex = _cmd.indexOf( ' ' );
      if ( spaceIndex < 0 ) {
         return Logger.getLogger( _cmd );
      }
      return Logger.getLogger( _cmd.substring( 0, spaceIndex ) );
   }


}
