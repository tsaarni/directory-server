/*
 *   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package org.apache.directory.server.standalone.installers;


import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.directory.server.standalone.daemon.InstallationLayout;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;


/**
 * Command to create installation image (footprint) before installers are triggered.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class CreateImageCommand implements MojoCommand
{
    private final Properties filterProperties = new Properties( System.getProperties() );
    private final ServiceInstallersMojo mymojo;
    private final Target target;
    private InstallationLayout layout;

    
    public CreateImageCommand( ServiceInstallersMojo mojo, Target target )
    {
        this.mymojo = mojo;
        this.target = target;
        initializeFiltering();
    }


    private void initializeFiltering() 
    {
        filterProperties.putAll( mymojo.getProject().getProperties() );
        filterProperties.put( "app" , mymojo.getApplicationName() );
        filterProperties.put( "app.caps" , mymojo.getApplicationName().toUpperCase() );
        filterProperties.put( "app.server.class", mymojo.getApplicationClass() );

        if ( mymojo.getApplicationVersion() != null )
        {
            filterProperties.put( "app.version", mymojo.getApplicationVersion() );
        }
        
        if ( mymojo.getApplicationDescription() != null )
        {
            filterProperties.put( "app.init.message", mymojo.getApplicationDescription() );
        }
    }

    
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        // make the layout directories
        File dir = new File( mymojo.getOutputDirectory(), target.getId() );
        layout = new InstallationLayout( dir );
        layout.mkdirs();
        
        // copy over the REQUIRED bootstrapper.jar file 
        try
        {
            FileUtils.copyFile( mymojo.getBootstrapper().getFile(), layout.getBootstrapper() );
        }
        catch ( IOException e )
        {
            throw new MojoFailureException( "Failed to copy bootstrapper.jar " + mymojo.getBootstrapper().getFile()
                + " into position " + layout.getBootstrapper() );
        }
        
        // copy over the REQUIRED bootstrapper configuration file
        try
        {
            FileUtils.copyFile( target.getBootstrapperConfiguraitonFile(), layout.getBootstrapperConfigurationFile() );
        }
        catch ( IOException e )
        {
            throw new MojoFailureException( "Failed to copy bootstrapper configuration file "  
                + target.getBootstrapperConfiguraitonFile() 
                + " into position " + layout.getBootstrapperConfigurationFile() );
        }
        
        // copy over the optional logging configuration file
        if ( target.getLoggerConfigurationFile().exists() )
        {
            try
            {
                FileUtils.copyFile( target.getLoggerConfigurationFile(), 
                    layout.getLoggerConfigurationFile() );
            }
            catch ( IOException e )
            {
                mymojo.getLog().error( "Failed to copy logger configuration file "  
                    + target.getLoggerConfigurationFile() 
                    + " into position " + layout.getLoggerConfigurationFile(), e );
            }
        }
        
        // copy over the optional server configuration file
        if ( target.getServerConfigurationFile().exists() )
        {
            try
            {
                FileUtils.copyFile( target.getServerConfigurationFile(), 
                    layout.getConfigurationFile() );
            }
            catch ( IOException e )
            {
                mymojo.getLog().error( "Failed to copy server configuration file "  
                    + target.getServerConfigurationFile()
                    + " into position " + layout.getConfigurationFile(), e );
            }
        }
        
        // copy over the init script template
        if ( target.getOsFamily().equals( "unix" ) )
        {
            try
            {
                MojoHelperUtils.copyAsciiFile( mymojo, filterProperties, 
                    getClass().getResourceAsStream( "template.init" ), layout.getInitScript(), true );
            }
            catch ( IOException e )
            {
                mymojo.getLog().error( "Failed to copy server configuration file "  
                    + target.getServerConfigurationFile()
                    + " into position " + layout.getInitScript(), e );
            }
        }
        
        // now copy over the jsvc executable renaming it to the applicationName 
        if ( target.getOsName().equals( "linux" ) && 
             target.getOsArch().equals( "i386" ) )
        {
            File executable = new File ( layout.getBinDirectory(), mymojo.getApplicationName() );
            try
            {
                MojoHelperUtils.copyBinaryFile( getClass().getResourceAsStream( "jsvc_linux_i386" ), executable );
            }
            catch ( IOException e )
            {
                throw new MojoFailureException( "Failed to copy jsvc executable file "  
                    + getClass().getResource( "jsvc_linux_i386" )
                    + " into position " + executable.getAbsolutePath() );
            }
        }
        
        // now copy over the jsvc executable renaming it to the mymojo.getApplicationName() 
        if ( target.getOsName().equals( "sunos" ) && 
             target.getOsArch().equals( "sparc" ) )
        {
            File executable = new File ( layout.getBinDirectory(), mymojo.getApplicationName() );
            try
            {
                MojoHelperUtils.copyBinaryFile( getClass().getResourceAsStream( "jsvc_solaris_sparc" ), executable );
            }
            catch ( IOException e )
            {
                throw new MojoFailureException( "Failed to copy jsvc executable file "  
                    + getClass().getResource( "jsvc_solaris_sparc" )
                    + " into position " + executable.getAbsolutePath() );
            }
        }
        
        // now copy over the jsvc executable renaming it to the mymojo.getApplicationName() 
        if ( target.getOsName().equals( "macosx" ) && target.getOsArch().equals( "ppc" ) )
        {
            File executable = new File ( layout.getBinDirectory(), mymojo.getApplicationName() );
            try
            {
                MojoHelperUtils.copyBinaryFile( getClass().getResourceAsStream( "jsvc_macosx_ppc" ), executable );
            }
            catch ( IOException e )
            {
                throw new MojoFailureException( "Failed to copy jsvc executable file "  
                    + getClass().getResource( "jsvc_macosx_ppc" )
                    + " into position " + executable.getAbsolutePath() );
            }
        }
        
        // now copy over the Prunsrv and Prunmgr executables renaming them to the mymojo.getApplicationName() + w for mgr
        if ( target.getOsFamily().equals( "windows" ) &&  target.getOsArch().equals( "x86" ) )
        {
            File executable = new File ( layout.getBinDirectory(), mymojo.getApplicationName() + ".exe" );
            try
            {
                MojoHelperUtils.copyBinaryFile( getClass().getResourceAsStream( "prunsrv.exe" ), executable );
            }
            catch ( IOException e )
            {
                throw new MojoFailureException( "Failed to copy prunsrv executable file "  
                    + getClass().getResource( "prunsrv.exe" )
                    + " into position " + executable.getAbsolutePath() );
            }

            executable = new File ( layout.getBinDirectory(), mymojo.getApplicationName() + "w.exe" );
            try
            {
                MojoHelperUtils.copyBinaryFile( getClass().getResourceAsStream( "prunmgr.exe" ), executable );
            }
            catch ( IOException e )
            {
                throw new MojoFailureException( "Failed to copy prunmgr executable file "  
                    + getClass().getResource( "prunmgr.exe" )
                    + " into position " + executable.getAbsolutePath() );
            }
        }
        
        MojoHelperUtils.copyDependencies( mymojo, layout );
    }
    
    
    InstallationLayout getLayout()
    {
        return layout;
    }
}
