/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.directory.studio.schemaeditor;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionCorePlugin;
import org.apache.directory.studio.schemaeditor.controller.ProjectsHandler;
import org.apache.directory.studio.schemaeditor.model.AttributeTypeImpl;
import org.apache.directory.studio.schemaeditor.model.ObjectClassImpl;
import org.apache.directory.studio.schemaeditor.model.Project;
import org.apache.directory.studio.schemaeditor.model.Schema;
import org.apache.directory.studio.schemaeditor.model.io.ProjectsExporter;
import org.apache.directory.studio.schemaeditor.model.io.ProjectsImportException;
import org.apache.directory.studio.schemaeditor.model.io.ProjectsImporter;
import org.apache.directory.studio.schemaeditor.model.io.SchemaConnector;
import org.apache.directory.studio.schemaeditor.model.io.XMLSchemaFileImportException;
import org.apache.directory.studio.schemaeditor.model.io.XMLSchemaFileImporter;
import org.apache.directory.studio.schemaeditor.view.ViewUtils;
import org.apache.directory.studio.schemaeditor.view.wizards.NewProjectWizardSchemasSelectionPage.ServerTypeEnum;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;


/**
 * This class contains helper methods.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class PluginUtils
{
    /**
     * Verifies that the given name is syntaxely correct according to the RFC 2252 
     * (Lightweight Directory Access Protocol (v3): Attribute Syntax Definitions).
     *
     * @param name
     *      the name to test
     * @return
     *      true if the name is correct, false if the name is not correct.
     */
    public static boolean verifyName( String name )
    {
        return name.matches( "[a-zA-Z]+[a-zA-Z0-9;-]*" ); //$NON-NLS-1$
    }


    /**
     * Returns a clone of the given attribute type.
     *
     * @param at
     *      the attribute type to clone
     * @return
     *      a clone of the given attribute type
     */
    public static AttributeTypeImpl getClone( AttributeTypeImpl at )
    {
        AttributeTypeImpl clone = new AttributeTypeImpl( at.getOid() );
        clone.setNames( at.getNames() );
        clone.setSchema( at.getSchema() );
        clone.setDescription( at.getDescription() );
        clone.setSuperiorName( at.getSuperiorName() );
        clone.setUsage( at.getUsage() );
        clone.setSyntaxOid( at.getSyntaxOid() );
        clone.setLength( at.getLength() );
        clone.setObsolete( at.isObsolete() );
        clone.setSingleValue( at.isSingleValue() );
        clone.setCollective( at.isCollective() );
        clone.setCanUserModify( at.isCanUserModify() );
        clone.setEqualityName( at.getEqualityName() );
        clone.setOrderingName( at.getOrderingName() );
        clone.setSubstrName( at.getSubstrName() );

        return clone;
    }


    /**
     * Returns a clone of the given object class.
     *
     * @param oc
     *      the object class to clone
     * @return
     *      a clone of the given object class
     */
    public static ObjectClassImpl getClone( ObjectClassImpl oc )
    {
        ObjectClassImpl clone = new ObjectClassImpl( oc.getOid() );
        clone.setNames( oc.getNames() );
        clone.setSchema( oc.getSchema() );
        clone.setDescription( oc.getDescription() );
        clone.setSuperClassesNames( oc.getSuperClassesNames() );
        clone.setType( oc.getType() );
        clone.setObsolete( oc.isObsolete() );
        clone.setMustNamesList( oc.getMustNamesList() );
        clone.setMayNamesList( oc.getMayNamesList() );

        return clone;
    }


    /**
     * Gets the Projects (where is stored information about the loaded Projects).
     *
     * @return
     *      the Projects File
     */
    private static File getProjectsFile()
    {
        return Activator.getDefault().getStateLocation().append( "projects.xml" ).toFile(); //$NON-NLS-1$
    }


    /**
     * Loads the projects saved in the Projects File.
     */
    public static void loadProjects()
    {
        ProjectsHandler projectsHandler = Activator.getDefault().getProjectsHandler();
        File projectsFile = getProjectsFile();

        if ( projectsFile.exists() )
        {
            Project[] projects = null;
            try
            {
                projects = ProjectsImporter.getProjects( new FileInputStream( projectsFile ), projectsFile
                    .getAbsolutePath() );
            }
            catch ( ProjectsImportException e )
            {
                handleErrorWhileLoadingProjects( projectsFile, e );
                return;
            }
            catch ( FileNotFoundException e )
            {
                handleErrorWhileLoadingProjects( projectsFile, e );
                return;
            }

            for ( Project project : projects )
            {
                projectsHandler.addProject( project );
            }
        }
    }


    /**
     * This method is called when an exception is raised when trying to load the Projects file.
     * 
     * @param projectsFile
     * 		the Projects file
     * @param e
     * 		the exception raised
     */
    private static void handleErrorWhileLoadingProjects( File projectsFile, Exception e )
    {
        PluginUtils.logError( "An error occured when loading the projects.", e );
        ViewUtils.displayErrorMessageBox( "Projects Loading Error", "An error occured when loading the projects." );
    }


    /**
     * Saves the projects in the Projects File.
     */
    public static void saveProjects()
    {
        try
        {
            OutputFormat outformat = OutputFormat.createPrettyPrint();
            outformat.setEncoding( "UTF-8" );
            XMLWriter writer = new XMLWriter( new FileOutputStream( getProjectsFile() ), outformat );
            writer.write( ProjectsExporter.toDocument( Activator.getDefault().getProjectsHandler().getProjects()
                .toArray( new Project[0] ) ) );
            writer.flush();
        }
        catch ( IOException e )
        {
            PluginUtils.logError( "An error occured when saving the projects.", e );
            ViewUtils.displayErrorMessageBox( "Projects Saving Error", "An error occured when saving the projects." );
        }
    }


    /**
     * Logs the given message and exception with the ERROR status level.
     * 
     * @param message
     *      the message
     * @param exception
     *      the exception
     */
    public static void logError( String message, Throwable exception )
    {
        Activator.getDefault().getLog().log(
            new Status( Status.ERROR, Activator.getDefault().getBundle().getSymbolicName(), Status.OK, message,
                exception ) );
    }


    /**
     * Logs the given message and exception with the ERROR status level.
     * 
     * @param message
     *      the message
     * @param exception
     *      the exception
     */
    public static void logInfo( Throwable exception, String message, Object... args )
    {
        String msg = MessageFormat.format( message, args );
        Activator.getDefault().getLog()
            .log(
                new Status( Status.ERROR, Activator.getDefault().getBundle().getSymbolicName(), Status.OK, msg,
                    exception ) );
    }


    /**
     * Logs the given message and exception with the WARNING status level.
     * 
     * @param message
     *      the message
     * @param exception
     *      the exception
     */
    public static void logWarning( String message, Throwable exception )
    {
        Activator.getDefault().getLog().log(
            new Status( Status.WARNING, Activator.getDefault().getBundle().getSymbolicName(), Status.OK, message,
                exception ) );
    }


    /**
     * Loads the 'core' corresponding to the given name.
     *
     * @param schemaName
     *      the name of the 'core' schema
     * @return
     *      the corresponding schema, or null if no schema has been found
     */
    public static Schema loadCoreSchema( ServerTypeEnum serverType, String schemaName )
    {
        Schema schema = null;

        try
        {
            URL url = Platform.getBundle( Activator.PLUGIN_ID ).getResource(
                "resources/schemas/" + getFolderName( serverType ) + "/" + schemaName + ".xml" );

            if ( url == null )
            {
                PluginUtils.logError( "An error occured when loading the schema " + schemaName + ".", null );
                ViewUtils.displayErrorMessageBox( "Projects Saving Error", "An error occured when loading the schema "
                    + schemaName + "." );
            }
            else
            {
                schema = XMLSchemaFileImporter.getSchema( new FileInputStream( new File( url.toURI() ) ), url
                    .toString() );
            }
        }
        catch ( XMLSchemaFileImportException e )
        {
            PluginUtils.logError( "An error occured when loading the schema " + schemaName + ".", e );
            ViewUtils.displayErrorMessageBox( "Projects Saving Error", "An error occured when loading the schema "
                + schemaName + "." );
        }
        catch ( FileNotFoundException e )
        {
            PluginUtils.logError( "An error occured when loading the schema " + schemaName + ".", e );
            ViewUtils.displayErrorMessageBox( "Projects Saving Error", "An error occured when loading the schema "
                + schemaName + "." );
        }
        catch ( URISyntaxException e )
        {
            PluginUtils.logError( "An error occured when loading the schema " + schemaName + ".", e );
            ViewUtils.displayErrorMessageBox( "Projects Saving Error", "An error occured when loading the schema "
                + schemaName + "." );
        }

        return schema;
    }


    /**
     * The name of the folder for the given Server Type.
     *
     * @param serverType
     *      the Server Type
     * @return
     *      the name of the folder for the given Server Type
     */
    private static String getFolderName( ServerTypeEnum serverType )
    {
        if ( ServerTypeEnum.APACHE_DS.equals( serverType ) )
        {
            return "apacheds";
        }
        else if ( ServerTypeEnum.OPENLDAP.equals( serverType ) )
        {
            return "openldap";
        }

        // Default
        return null;
    }


    /**
     * Gets a Connection from the given id.
     *
     * @param id
     *      the id of the Connection
     * @return
     *      the corresponding Connection, or null if no connection was found.
     */
    public static Connection getConnection( String id )
    {
        Connection[] connectionsArray = ConnectionCorePlugin.getDefault().getConnectionManager().getConnections();

        HashMap<String, Connection> connections = new HashMap<String, Connection>();
        for ( Connection connection : connectionsArray )
        {
            connections.put( connection.getId(), connection );
        }

        return connections.get( id );
    }


    /**
     * Gets the List of SchemaConnectors defined using the ExtensionPoint.
     *
     * @return
     *      the List of SchemaConnectors defined using the ExtensionPoint
     */
    public static List<SchemaConnector> getSchemaConnectors()
    {
        List<SchemaConnector> schemaConnectors = new ArrayList<SchemaConnector>();

        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(
            "org.apache.directory.studio.schemaeditor.schemaConnectors" );
        IConfigurationElement[] members = extensionPoint.getConfigurationElements();

        if ( members != null )
        {
            // Creating each SchemaConnector
            for ( IConfigurationElement member : members )
            {
                try
                {
                    SchemaConnector schemaConnector = ( SchemaConnector ) member.createExecutableExtension( "class" );
                    schemaConnector.setName( member.getAttribute( "name" ) );
                    schemaConnector.setId( member.getAttribute( "id" ) );
                    schemaConnector.setDescription( member.getAttribute( "description" ) );

                    schemaConnectors.add( schemaConnector );
                }
                catch ( CoreException e )
                {
                    PluginUtils.logError( "An error occured when loading the schema connectors.", e );
                    ViewUtils.displayErrorMessageBox( "Error", "An error occured when loading the schema connectors." );
                }
            }
        }

        return schemaConnectors;
    }
}
