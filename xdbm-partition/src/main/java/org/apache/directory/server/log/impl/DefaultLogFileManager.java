
package org.apache.directory.server.log.impl;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.io.RandomAccessFile;

import org.apache.directory.server.i18n.I18n;
import org.apache.directory.server.log.impl.LogFileManager.LogFileReader;
import org.apache.directory.server.log.impl.LogFileManager.LogFileWriter;

class DefaultLogFileManager implements LogFileManager 
{
    private String logFilePath;
    private String suffix;
    
    /**
     * Inits the log file manager to use the given logfile path and the suffix. Each log file
     * has name logFileName_<logFileNumber>.suffix 
     *
     * @param logFilepath log file path
     * @param suffix suffix for log file.
     */
    public void init( String logFilepath, String suffix )
    {
        this.logFilePath = logFilePath;
        this.suffix = suffix;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public LogFileReader getReaderForLogFile( long logFileNumber ) throws IOException, FileNotFoundException
    {      
        File logFile = this.makeLogFileName( logFileNumber );
        
        // This will throw a file not found exception if file does not exist
        RandomAccessFile raf = new RandomAccessFile( logFile, "r" );
        
        return new LogFileReader( raf, logFileNumber );
    }
    
    /**
     * {@inheritDoc}
     */
    public LogFileWriter getWriterForLogFile( long logFileNumber ) throws IOException, FileNotFoundException
    {
        File logFile = this.makeLogFileName( logFileNumber );
        
        // This will throw a file not found exception if file does not exist
        RandomAccessFile raf = new RandomAccessFile( logFile, "rw" );
        
        return new LogFileWriter( raf, logFileNumber );
    }
    
    
    /**
     * {@inheritDoc}
     */
    public boolean createLogFile( long logFileNumber ) throws IOException
    {
        File logFile = this.makeLogFileName( logFileNumber );
        
        boolean fileAlreadyExists = !logFile.createNewFile();
        
        return fileAlreadyExists;
    }
    
       
    /**
     * {@inheritDoc}
     */
    public void truncateLogFile( long logFileNumber, long size ) throws IOException, FileNotFoundException
    {
        if ( size < 0 )
        {
            throw new IllegalArgumentException( "Invalid file size is specified for the log file: " + logFileNumber + " " + size );
        }
        
        File logFile = this.makeLogFileName( logFileNumber );
        
        // This will throw a file not found exception if file does not exist
        RandomAccessFile raf = new RandomAccessFile( logFile, "rw" );
        
        raf.setLength( size );
        raf.getFD().sync();
    }
    
   
    /**
     * {@inheritDoc}
     */
    public void deleteLogFile( long logFileNumber )
    {
        File logFile = this.makeLogFileName( logFileNumber );
        
        logFile.delete();
    }
    
   
    /**
     * {@inheritDoc}
     */
    public boolean rename(long originalLogFileNumber, long newLongFileNumber)
    {
        File oldLogFile = this.makeLogFileName( originalLogFileNumber );  
        boolean result = oldLogFile.renameTo( this.makeLogFileName( newLongFileNumber ) );
        return result;
    }
    
    
    private File makeLogFileName( long logFileNumber )
    {
        
        return new File( logFilePath + "/" + LogFileManager.LOG_NAME_PREFIX + logFileNumber + "." + suffix );
    }
    
    static class LogFileReader implements LogFileManager.LogFileReader
    {
        /** Underlying log file */
        RandomAccessFile raf;
        
        /** Log file identifier */
        long logFileNumber;
        
  
        public LogFileReader( RandomAccessFile raf, long logFileNumber )
        {
            this.raf = raf;
            this.logFileNumber = logFileNumber;
        }
        
        /**
         * {@inheritDoc}
         */
        public int read( byte[] buffer, int offset, int length ) throws IOException, EOFException
        {
            raf.readFully( buffer, offset, length );
            return length;
        }
        
        /**
         * {@inheritDoc}
         */
        public void seek( long position ) throws IOException
        {
            raf.seek( position );
        }
        
        /**
         * {@inheritDoc}
         */
        public void close() throws IOException
        {
            raf.close();
        }
        
        
        /**
         * {@inheritDoc}
         */
        public long logFileNumber()
        {
            return logFileNumber;
        }
        
        /**
         * {@inheritDoc}
         */
        public long getLength() throws IOException
        {
            return raf.length();
        }
        
        /**
         * {@inheritDoc}
         */
        public long getOffset() throws IOException
        {
            return raf.getFilePointer();
        }
    }
    
    
    static class LogFileWriter implements LogFileManager.LogFileWriter
    {
        /** Underlying log file */
        RandomAccessFile raf;
        
        /** Log file identifier */
        long logFileNumber;
        
  
        public LogFileWriter( RandomAccessFile raf, long logFileNumber )
        {
            this.raf = raf;
            this.logFileNumber = logFileNumber;
        }
        /**
         * {@inheritDoc}
         */
        public void append( byte[] buffer, int offset, int length ) throws IOException
        {
            raf.write( buffer, offset, length );
        }
        
        /**
         * {@inheritDoc}
         */
        public void sync() throws IOException
        {
             raf.getFD().sync();
        }
        
        /**
         * {@inheritDoc}
         */
        public void close() throws IOException
        {
            raf.close();
        }
        
         /**
          * {@inheritDoc}
          */
        public long logFileNumber()
        {
            return logFileNumber;
        }
        
        /**
         * {@inheritDoc}
         */
        public long getLength() throws IOException
        {
            return raf.length();
        }
    }
}
