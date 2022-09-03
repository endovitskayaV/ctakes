package org.apache.ctakes.core.util.external;

import org.apache.ctakes.core.resource.FileLocator;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author SPF , chip-nlp
 * @since {5/4/2022}
 */
final public class SystemUtil {


   static private final Logger LOGGER = Logger.getLogger( "SystemUtil" );


   private SystemUtil() {
   }

   static private final String ENV_VAR_PREFIX = "ctakes.env.";
   static public final File NO_FILE = new File( "" );
   static public final String FILE_NOT_FOUND = "FILE_NOT_FOUND";


   static public String subVariableParameters( final String command, final UimaContext context ) {
      if ( context == null || command == null || !command.contains( "$" ) ) {
         return command;
      }
      final StringBuilder sb = new StringBuilder();
      int previousVarEnd = 0;
      int varBegin = command.indexOf( '$', previousVarEnd );
      while ( varBegin > -1 ) {
         // copy everything from the command from the end of the previous variable to the begin of this variable.
         sb.append( command, previousVarEnd, varBegin );
         // get the end of the current variable name.
         int varEnd = command.indexOf( ' ', varBegin );
         varEnd = varEnd < 0 ? command.length() : varEnd;
         // get the name of the variable (without $).
         final String varName = command.substring( varBegin + 1, varEnd );
         // get the value from the uima context.
         final Object value = context.getConfigParameterValue( varName );
         if ( value == null ) {
            LOGGER.error( "No value for $" + varName + " in known parameter values." );
            sb.append( "$" )
              .append( varName );
         } else {
            LOGGER.info( "Substituting Parameter Value " + value + " for $" + varName );
            sb.append( value );
         }
         if ( varEnd == command.length() ) {
            break;
         }
         previousVarEnd = varEnd + 1;
         varBegin = command.indexOf( '$', varEnd );
         if ( varBegin < 0 ) {
            sb.append( command, varEnd, command.length() );
         }
      }
      return sb.toString();
   }


   /**
    * Add key value pairs to a set of environment variables for use in external processes.
    *
    * @param variables ket value pairs
    */
   static public void addEnvironmentVariables( final Object... variables ) {
      if ( variables.length == 0 ) {
         LOGGER.warn( "No variables specified." );
         return;
      }
      if ( variables.length % 2 != 0 ) {
         LOGGER.error( "Odd number of variables provided.  Should be key value pairs." );
         return;
      }
      for ( int i = 0; i < variables.length; i += 2 ) {
         if ( variables[ i ] instanceof String ) {
            System.setProperty( ENV_VAR_PREFIX + variables[ i ], variables[ i + 1 ].toString() );
         } else {
            LOGGER.warn( "Variable" + i + " not a String, using " + variables[ i ].toString() );
            System.setProperty( ENV_VAR_PREFIX + variables[ i ].toString(), variables[ i + 1 ].toString() );
         }
      }
   }


   public boolean copyFile( final String source, final String target ) {
      final InputStream sourceStream = FileLocator.getStreamQuiet( source );
      if ( sourceStream == null ) {
         LOGGER.error( "Cannot access source " + source );
         return false;
      }
      final File targetFile = FileLocator.getFileQuiet( target );
      if ( targetFile == null ) {
         LOGGER.error( "Cannot access target file " + target );
         return false;
      }
      targetFile.getParentFile()
                .mkdirs();
      Path targetPath;
      try {
         targetPath = Paths.get( target );
      } catch ( InvalidPathException ipE ) {
         LOGGER.error( "Cannot access target path " + target );
         return false;
      }
      return SystemUtil.copyToDisk( sourceStream, targetPath );
   }

   static public boolean copyToDisk( final InputStream source, final Path target ) {
      try {
         Files.copy( source, target, StandardCopyOption.REPLACE_EXISTING );
      } catch ( IOException ioE ) {
         LOGGER.error( ioE.getMessage() );
         return false;
      }
      return true;
   }

   static public String findExecutable( final String name ) {
      final String executable = findExecutableInCtakes( name );
      if ( !FILE_NOT_FOUND.equals( executable ) ) {
         return executable;
      }
      return findExecutableOnPath( name );
   }

   static public String findExecutableInCtakes( final String name ) {
      final File executable = FileLocator.getFileQuiet( name );
      if ( executable != null ) {
         if ( executable.canExecute() ) {
            return executable.getAbsolutePath();
         }
      }
      return FILE_NOT_FOUND;
   }

   static public String findExecutableOnPath( final String name ) {
      for ( String dirname : System.getenv( "PATH" )
                                   .split( File.pathSeparator ) ) {
         final File testFile = new File( dirname, name );
         if ( testFile.isFile() && testFile.canExecute() ) {
            return testFile.getAbsolutePath();
         }
      }
      return FILE_NOT_FOUND;
   }


   static public class FileDownloader implements Callable<File> {

      private final String _url;
      private final String _tempPrefix;
      private final String _tempSuffix;

      public FileDownloader( final String url ) {
         this( url, "Prefix", "suffix" );
      }

      public FileDownloader( final String url, final String tempPrefix, final String tempSuffix ) {
         _url = url;
         _tempPrefix = tempPrefix;
         _tempSuffix = tempSuffix;
      }

      public File call() throws IOException {
         final File tempZip = File.createTempFile( _tempPrefix, _tempSuffix );
         tempZip.deleteOnExit();
         URL url = new URL( _url );
         try ( ReadableByteChannel readableByteChannel = Channels.newChannel( url.openStream() );
               FileOutputStream fileOutputStream = new FileOutputStream( tempZip );
               FileChannel fileChannel = fileOutputStream.getChannel() ) {
            fileChannel.transferFrom( readableByteChannel, 0, Long.MAX_VALUE );
         }
         return tempZip;
      }

   }


   static private boolean unzipit( final String zippedFile, final File unzipDir ) throws IOException {
      final InputStream zippedStream = FileLocator.getStreamQuiet( zippedFile );
      if ( zippedStream == null ) {
         LOGGER.error( "Could not access " + zippedFile );
         return false;
      }
      return unzipit( zippedStream, unzipDir );
   }

   static private boolean unzipit( final File zippedFile, final File unzipDir ) throws IOException {
      return unzipit( zippedFile.getPath(), unzipDir );
   }

   static private boolean unzipit( final InputStream zippedStream, final File unzipDir ) throws IOException {
      final byte[] buffer = new byte[ 1024 ];
      final ZipInputStream zis = new ZipInputStream( zippedStream );
      ZipEntry zipEntry = zis.getNextEntry();
      while ( zipEntry != null ) {
         if ( zipEntry.isDirectory() ) {
            final File newUnzipDir = new File( unzipDir, zipEntry.getName() );
            newUnzipDir.mkdirs();
         } else {
            final File newUnzipFile = newUnzipFile( unzipDir, zipEntry );
            final FileOutputStream fos = new FileOutputStream( newUnzipFile );
            int len;
            while ( ( len = zis.read( buffer ) ) > 0 ) {
               fos.write( buffer, 0, len );
            }
            fos.close();
         }
         zipEntry = zis.getNextEntry();
      }
      zis.closeEntry();
      zis.close();
      return true;
   }

   static public class FileUnzipper implements Callable<File> {

      private final File _zip;
      private final File _unzipDir;

      public FileUnzipper( final File zip, final File unzipDir ) {
         _zip = zip;
         _unzipDir = unzipDir;
      }

      public File call() throws IOException {
         unzipit( _zip, _unzipDir );
         return _unzipDir;
      }

   }


   static private File newUnzipFile( final File unzipDirPath, final ZipEntry zipEntry ) throws IOException {
      final File unzippedFile = new File( unzipDirPath, zipEntry.getName() );

      final String destDirPath = unzipDirPath.getCanonicalPath();
      final String destFilePath = unzippedFile.getCanonicalPath();

      if ( !destFilePath.startsWith( destDirPath + File.separator ) ) {
         throw new IOException( "Entry is outside of the target dir: " + zipEntry.getName() );
      }
      return unzippedFile;
   }


   static public boolean run( final String command ) throws IOException {
      final CommandRunner runner = new CommandRunner( command );
      return run( runner );
   }

   static public boolean run( final CommandRunner runner ) throws IOException {
      boolean ok = false;
      final ExecutorService executor = Executors.newSingleThreadExecutor();
      try {
         final Future<Boolean> future = executor.submit( runner );
         ok = future.get();
      } catch ( InterruptedException | ExecutionException multE ) {
         throw new IOException( multE );
      }
      return ok;
   }

   static public class CommandRunner implements Callable<Boolean> {

      private final String _command;
      private String _dir;
      private String _outLog;
      private String _errLog;
      private Logger _logger;
      private boolean _wait;

      public CommandRunner( final String command ) {
         _command = command;
      }

      public void setDirectory( final String directory ) {
         _dir = directory;
      }

      public void setLogger( final Logger logger ) {
         _logger = logger;
      }

      public void setLogFiles( final String outLog,
                               final String errLog ) {
         _outLog = outLog;
         _errLog = errLog;
      }

      public void wait( final boolean wait ) {
         _wait = wait;
      }

      private String getDefaultLogFile() {
         final Random randomizer = new Random();
         final int spaceIndex = _command.indexOf( ' ' );
         if ( spaceIndex < 0 ) {
            return _command + ".ctakes.log." + randomizer.nextLong();
         }
         return _command.substring( 0, spaceIndex ) + ".ctakes.log." + randomizer.nextLong();
      }

      static private void ensureEnvironment( final ProcessBuilder processBuilder ) {
         final Map<String, String> env = processBuilder.environment();
         // If the user set a variable in a piper file using "env" then add that to the environment.
         System.getProperties()
               .stringPropertyNames()
               .stream()
               .filter( n -> n.startsWith( ENV_VAR_PREFIX ) )
               .forEach( n -> env.put( n.substring( ENV_VAR_PREFIX.length() ), System.getProperty( n ) ) );
         if ( !env.containsKey( "JAVA_HOME" ) ) {
            env.put( "JAVA_HOME", System.getProperty( "java.home" ) );
         }
         if ( !env.containsKey( "CTAKES_HOME" ) ) {
            String cTakesHome = System.getenv( "CTAKES_HOME" );
            if ( cTakesHome == null || cTakesHome.isEmpty() ) {
               cTakesHome = System.getProperty( "user.dir" );
            }
            env.put( "CTAKES_HOME", cTakesHome );
         }
         if ( !env.containsKey( "CLASSPATH" ) ) {
            final String classpath = System.getProperty( "java.class.path" );
            if ( classpath != null && !classpath.isEmpty() ) {
               env.put( "CLASSPATH", classpath );
            }
         }
      }

      public Boolean call() throws IOException, InterruptedException {
         String command = _command;
         if ( _logger == null ) {
            if ( _outLog != null && !_outLog.isEmpty() ) {
               command += " > " + _outLog + " 2>&1";
            } else {
               command += " > " + getDefaultLogFile() + " 2>&1";
            }
         }
         String cmd = "cmd.exe";
         String cmdOpt = "/c";
         final String os = System.getProperty( "os.name" );
         if ( os.toLowerCase()
                .contains( "windows" ) ) {
            command = command.replace( '/', '\\' );
         } else {
            cmd = "bash";
            cmdOpt = "-c";
//            if ( !_wait ) {
//               command += " &";
//            }
         }
         final ProcessBuilder processBuilder = new ProcessBuilder( cmd, cmdOpt, command );
         if ( _dir != null && !_dir.isEmpty() ) {
            final File dir = new File( _dir );
            if ( !dir.exists() ) {
               dir.mkdirs();
            }
            processBuilder.directory( dir );
         }
         ensureEnvironment( processBuilder );
         final Process process = processBuilder.start();
         if ( _logger != null ) {
            final ExecutorService executors = Executors.newFixedThreadPool( 2 );
            executors.submit( new OutputLogger( process, _logger ) );
            executors.submit( new ErrorLogger( process, _logger ) );
         }
         if ( _wait ) {
            return process.waitFor() == 0;
         }
         return true;
      }

   }


   static private class OutputLogger implements Runnable {

      final private InputStream _output;
      final private Logger _logger;

      private OutputLogger( final Process process, final Logger logger ) {
         _output = process.getInputStream();
         _logger = logger;
      }

      public void run() {
         try ( BufferedReader reader = new BufferedReader( new InputStreamReader( _output ) ) ) {
            reader.lines()
                  .forEach( _logger::info );
         } catch ( IOException ioE ) {
            _logger.error( ioE.getMessage() );
         }
      }

   }

   static private class ErrorLogger implements Runnable {

      final private InputStream _error;
      final private Logger _logger;

      private ErrorLogger( final Process process, final Logger logger ) {
         _error = process.getErrorStream();
         _logger = logger;
      }

      public void run() {
         try ( BufferedReader reader = new BufferedReader( new InputStreamReader( _error ) ) ) {
            reader.lines()
                  .forEach( _logger::error );
         } catch ( IOException ioE ) {
            _logger.error( ioE.getMessage() );
         }
      }

   }


}
