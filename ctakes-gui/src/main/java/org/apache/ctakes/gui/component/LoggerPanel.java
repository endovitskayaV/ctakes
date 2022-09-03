package org.apache.ctakes.gui.component;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.spi.LoggingEvent;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author SPF , chip-nlp
 * @version %I%
 * @since 11/29/2016
 */
final public class LoggerPanel extends JScrollPane {

   static private final int MAX_DOC_LENGTH = Integer.MAX_VALUE - 5000;

   static public LoggerPanel createLoggerPanel( final Level... levels ) {
      final LoggerPanel panel = new LoggerPanel( levels );
      LogManager.getRootLogger().addAppender( panel.getLogHandler() );
      return panel;
   }

   static private final Level[] ALL_LEVELS = { Level.FATAL, Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG,
                                               Level.TRACE };


   private final Appender _appender;
   private final Document _textAreaDoc = new PlainDocument();

   /**
    * text gui that will display log4j messages
    */
   private LoggerPanel( final Level... levels ) {
      final JTextArea textArea = new JTextArea( _textAreaDoc );
      textArea.setEditable( false );
      textArea.setEnabled( false );
      textArea.setDisabledTextColor( Color.DARK_GRAY );
      super.setViewportView( textArea );
      _appender = new LogHandler( levels );
   }

   /**
    * @return all the text in this gui
    */
   public String getText() {
      try {
         return _textAreaDoc.getText( 0, _textAreaDoc.getLength() );
      } catch ( BadLocationException blE ) {
         return "";
      }
   }

   /**
    * clear the text in this gui
    */
   public void clearText() {
      SwingUtilities.invokeLater( () -> {
         try {
            _textAreaDoc.remove( 0, _textAreaDoc.getLength() );
         } catch ( BadLocationException blE ) {
            //
         }
      } );
   }

   /**
    * @param text to append to the text displayed in this gui
    */
   public void appendText( final String text ) {
      SwingUtilities.invokeLater( () -> {
         try {
            if (_textAreaDoc.getLength() >= MAX_DOC_LENGTH ) {
               // clear log if it gets too long.  Not the best solution, but good enough for now.
               _textAreaDoc.remove( 0, _textAreaDoc.getLength() );
            }
            _textAreaDoc.insertString( _textAreaDoc.getLength(), text, null );
         } catch ( BadLocationException blE ) {
            //
         }
      } );
   }


   /**
    * @return the log4j appender that handles logging
    */
   private Appender getLogHandler() {
      return _appender;
   }

   /**
    * Handles reception of logging messages
    */
   private class LogHandler extends AppenderSkeleton {
      private final Collection<Level> _levels;

      private LogHandler( final Level... levels ) {
         _levels = Arrays.asList( (levels.length == 0 ? ALL_LEVELS : levels) );
      }

      /**
       * {@inheritDoc}
       */
      @Override
      protected void append( final LoggingEvent event ) {
         if ( event == null ) {
            return;
         }
         if ( _levels.contains( event.getLevel() ) ) {
            final Object message = event.getMessage();
            if ( message != null ) {
               appendText( message.toString() + "\n" );
            }
         }
      }

      /**
       * {@inheritDoc}
       *
       * @return false
       */
      @Override
      public boolean requiresLayout() {
         return false;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void close() {
      }
   }


}
