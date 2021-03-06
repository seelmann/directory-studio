/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.apache.directory.studio.openldap.config;


import java.io.IOException;
import java.util.List;

import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.DefaultAttribute;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.api.ldap.model.schema.ObjectClassTypeEnum;
import org.apache.directory.api.ldap.model.schema.SchemaObject;
import org.apache.directory.api.ldap.model.schema.UsageEnum;
import org.apache.directory.api.ldap.model.schema.registries.DefaultSchema;
import org.apache.directory.api.ldap.model.schema.registries.Schema;
import org.apache.directory.api.ldap.model.schema.registries.SchemaLoader;
import org.apache.directory.api.ldap.schema.loader.JarLdifSchemaLoader;
import org.apache.directory.api.util.Strings;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;


/**
 * This class implements a {@link SchemaLoader} based on a connection to an OpenLDAP
 * server, as well as a set of low level base schemas from ApacheDS.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ConnectionSchemaLoader extends JarLdifSchemaLoader
{
    private static final String M_COLLECTIVE = "m-collective";
    private static final String M_DESCRIPTION = "m-description";
    private static final String M_EQUALITY = "m-equality";
    private static final String M_LENGTH = "m-length";
    private static final String M_MAY = "m-may";
    private static final String M_MUST = "m-must";
    private static final String M_NAME = "m-name";
    private static final String M_NO_USER_MODIFICATION = "m-noUserModification";
    private static final String M_OBSOLETE = "m-obsolete";
    private static final String M_OID = "m-oid";
    private static final String M_ORDERING = "m-ordering";
    private static final String M_SINGLE_VALUE = "m-singleValue";
    private static final String M_SUBSTR = "m-substr";
    private static final String M_SUP_ATTRIBUTE_TYPE = "m-supAttributeType";
    private static final String M_SUP_OBJECT_CLASS = "m-supObjectClass";
    private static final String M_SYNTAX = "m-syntax";
    private static final String M_TYPE_OBJECT_CLASS = "m-typeObjectClass";
    private static final String M_USAGE = "m-usage";
    private static final String TRUE = "TRUE";

    /** The name of the connection schema */
    public static final String CONNECTION_SCHEMA_NAME = "connectionSchema";

    /** The configuration prefix */
    private static final String CONFIG_PREFIX = "olc";

    /** The schema */
    private org.apache.directory.studio.ldapbrowser.core.model.schema.Schema browserConnectionSchema;


    /**
     * Creates a new instance of ConnectionSchemaLoader.
     *
     * @param connection the connection
     * @throws Exception
     */
    public ConnectionSchemaLoader( Connection connection ) throws Exception
    {
        super();

        // Getting the browser connection associated with the connection
        browserConnectionSchema = BrowserCorePlugin.getDefault().getConnectionManager()
            .getBrowserConnection( connection ).getSchema();

        initializeSchema();
    }


    /**
     * Initializes the schema.
     */
    private void initializeSchema()
    {
        Schema schema = new DefaultSchema( null, CONNECTION_SCHEMA_NAME );
        
        schema.addDependencies( "system", "core", "apache" );
        schemaMap.put( schema.getSchemaName(), schema );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Entry> loadAttributeTypes( Schema... schemas ) throws LdapException, IOException
    {
        List<Entry> attributeTypes = super.loadAttributeTypes( schemas );

        for ( Schema schema : schemas )
        {
            if ( CONNECTION_SCHEMA_NAME.equals( schema.getSchemaName() ) )
            {
                for ( AttributeType attributeType : browserConnectionSchema.getAttributeTypeDescriptions() )
                {
                    if ( attributeType.getName().startsWith( CONFIG_PREFIX ) )
                    {
                        attributeTypes.add( convert( attributeType ) );
                    }
                }
            }
        }

        return attributeTypes;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Entry> loadObjectClasses( Schema... schemas ) throws LdapException, IOException
    {
        List<Entry> objectClasses = super.loadObjectClasses( schemas );

        for ( Schema schema : schemas )
        {
            if ( CONNECTION_SCHEMA_NAME.equals( schema.getSchemaName() ) )
            {
                for ( ObjectClass objectClass : browserConnectionSchema.getObjectClassDescriptions() )
                {
                    if ( objectClass.getName().startsWith( CONFIG_PREFIX ) )
                    {
                        objectClasses.add( convert( objectClass ) );
                    }
                }
            }
        }

        return objectClasses;
    }


    /**
     * Converts the given attribute type to a schema entry.
     *
     * @param attributeType the attribute type
     * @return the given attribute type as a schema entry.
     * @throws LdapException 
     */
    private Entry convert( AttributeType attributeType ) throws LdapException
    {
        // Creating a blank entry
        Entry entry = new DefaultEntry();

        // Setting calculated DN
        entry.setDn( getDn( attributeType, SchemaConstants.ATTRIBUTE_TYPES_PATH ) );

        // Values common to all schema objects
        addSchemaObjectValues( attributeType, SchemaConstants.META_ATTRIBUTE_TYPE_OC, entry );

        // Superior value
        addSuperiorValue( attributeType, entry );

        // Equality matching rule value
        addEqualityValue( attributeType, entry );

        // Ordering matching rule value
        addOrderingValue( attributeType, entry );

        // Substrings matching rule value
        addSubstrValue( attributeType, entry );

        // Syntax value
        addSyntaxValue( attributeType, entry );

        // Single value value
        addSingleValueValue( attributeType, entry );

        // Collective value
        addCollectiveValue( attributeType, entry );

        // No user modification value
        addNoUserModificationValue( attributeType, entry );

        // Usage value
        addUsageValue( attributeType, entry );

        return entry;
    }


    /**
     * Converts the given object class to a schema entry.
     *
     * @param objectClass the object class
     * @return the given object class as a schema entry.
     * @throws LdapException 
     */
    private Entry convert( ObjectClass objectClass ) throws LdapException
    {
        // Creating a blank entry
        Entry entry = new DefaultEntry();

        // Setting calculated DN
        entry.setDn( getDn( objectClass, SchemaConstants.OBJECT_CLASSES_PATH ) );

        // Values common to all schema objects
        addSchemaObjectValues( objectClass, SchemaConstants.META_OBJECT_CLASS_OC, entry );

        // Superiors value
        addSuperiorsValue( objectClass, entry );

        // Class type value
        addClassTypeValue( objectClass, entry );

        // Musts value
        addMustsValue( objectClass, entry );

        // Mays value
        addMaysValue( objectClass, entry );

        return entry;
    }


    /**
     * Gets the DN associated with the given schema object in the given container.
     *
     * @param so the schema object
     * @param container the container
     * @return the DN associated with the given schema object in the given container.
     * @throws LdapInvalidDnException
     */
    private Dn getDn( SchemaObject so, String container ) throws LdapInvalidDnException, LdapInvalidAttributeValueException
    {
        return Dn.EMPTY_DN
            .add( new Rdn( SchemaConstants.OU_SCHEMA ) )
            .add( new Rdn( SchemaConstants.CN_AT, Rdn.escapeValue( CONNECTION_SCHEMA_NAME ) ) )
            .add( new Rdn( container ) )
            .add( new Rdn( "m-oid", so.getOid() ) );
    }


    /**
     * Adds the values common to all {@link SchemaObject}(s) to the entry.
     *
     * @param schemaObject the schema object
     * @param objectClassValue the value for the objectClass attribute
     * @param entry the entry
     * @throws LdapException
     */
    private static void addSchemaObjectValues( SchemaObject schemaObject, String objectClassValue, Entry entry )
        throws LdapException
    {
        // ObjectClass
        addObjectClassValue( schemaObject, objectClassValue, entry );

        // OID
        addOidValue( schemaObject, entry );

        // Names
        addNamesValue( schemaObject, entry );

        // Description
        addDescriptionValue( schemaObject, entry );

        // Obsolete
        addObsoleteValue( schemaObject, entry );
    }


    /**
     * Adds the objectClass value to the entry.
     *
     * @param schemaObject the schema object
     * @param objectClassValue the value for the objectClass attribute
     * @param entry the entry
     * @throws LdapException
     */
    private static void addObjectClassValue( SchemaObject schemaObject, String objectClassValue, Entry entry )
        throws LdapException
    {
        Attribute objectClassAttribute = new DefaultAttribute( SchemaConstants.OBJECT_CLASS_AT );
        entry.add( objectClassAttribute );
        objectClassAttribute.add( SchemaConstants.TOP_OC );
        objectClassAttribute.add( SchemaConstants.META_TOP_OC );
        objectClassAttribute.add( objectClassValue );
    }


    /**
     * Adds the OID value to the entry.
     *
     * @param schemaObject the schema object
     * @param entry the entry
     * @throws LdapException
     */
    private static void addOidValue( SchemaObject schemaObject, Entry entry ) throws LdapException
    {
        String oid = schemaObject.getOid();
        
        if ( !Strings.isEmpty( oid ) )
        {
            Attribute attribute = new DefaultAttribute( M_OID, oid );
            entry.add( attribute );
        }
    }


    /**
     * Adds the names value to the entry.
     *
     * @param schemaObject the schema object
     * @param entry the entry
     * @throws LdapException
     */
    private static void addNamesValue( SchemaObject schemaObject, Entry entry ) throws LdapException
    {
        List<String> names = schemaObject.getNames();
        
        if ( ( names != null ) && !names.isEmpty() )
        {
            Attribute attribute = new DefaultAttribute( M_NAME );
            entry.add( attribute );

            for ( String name : names )
            {
                attribute.add( name );
            }
        }
    }


    /**
     * Adds the description value to the entry.
     *
     * @param schemaObject the schema object
     * @param entry the entry
     * @throws LdapException
     */
    private static void addDescriptionValue( SchemaObject schemaObject, Entry entry ) throws LdapException
    {
        String description = schemaObject.getDescription();
        
        if ( !Strings.isEmpty( description ) )
        {
            Attribute attribute = new DefaultAttribute( M_DESCRIPTION, description );
            entry.add( attribute );
        }
    }


    /**
     * Adds the obsolete value to the entry.
     *
     * @param schemaObject the schema object
     * @param entry the entry
     * @throws LdapException
     */
    private static void addObsoleteValue( SchemaObject schemaObject, Entry entry ) throws LdapException
    {
        if ( schemaObject.isObsolete() )
        {
            Attribute attribute = new DefaultAttribute( M_OBSOLETE, TRUE );
            entry.add( attribute );
        }
    }


    /**
     * Adds the superior value.
     *
     * @param attributeType the attribute type
     * @param entry the entry
     * @throws LdapException
     */
    private static void addSuperiorValue( AttributeType attributeType, Entry entry ) throws LdapException
    {
        String superior = attributeType.getSuperiorName();
        
        if ( !Strings.isEmpty( superior ) )
        {
            Attribute attribute = new DefaultAttribute( M_SUP_ATTRIBUTE_TYPE, superior );
            entry.add( attribute );
        }
    }


    /**
     * Adds the equality matching rule value.
     *
     * @param attributeType the attribute type
     * @param entry the entry
     * @throws LdapException
     */
    private static void addEqualityValue( AttributeType attributeType, Entry entry ) throws LdapException
    {
        String equality = attributeType.getEqualityName();
        
        if ( !Strings.isEmpty( equality ) )
        {
            Attribute attribute = new DefaultAttribute( M_EQUALITY, equality );
            entry.add( attribute );
        }
    }


    /**
     * Adds the ordering matching rule value.
     *
     * @param attributeType the attribute type
     * @param entry the entry
     * @throws LdapException
     */
    private static void addOrderingValue( AttributeType attributeType, Entry entry ) throws LdapException
    {
        String ordering = attributeType.getOrderingName();
        
        if ( !Strings.isEmpty( ordering ) )
        {
            Attribute attribute = new DefaultAttribute( M_ORDERING, ordering );
            entry.add( attribute );
        }
    }


    /**
     * Adds the substring matching rule value.
     *
     * @param attributeType the attribute type
     * @param entry the entry
     * @throws LdapException
     */
    private static void addSubstrValue( AttributeType attributeType, Entry entry ) throws LdapException
    {
        String substr = attributeType.getSubstringName();
        
        if ( !Strings.isEmpty( substr ) )
        {
            Attribute attribute = new DefaultAttribute( M_SUBSTR, substr );
            entry.add( attribute );
        }
    }


    /**
     * Adds the syntax value.
     *
     * @param attributeType the attribute type
     * @param entry the entry
     * @throws LdapException
     */
    private static void addSyntaxValue( AttributeType attributeType, Entry entry ) throws LdapException
    {
        String syntax = attributeType.getSyntaxName();
        
        if ( !Strings.isEmpty( syntax ) )
        {
            Attribute attribute = new DefaultAttribute( M_SYNTAX, syntax );
            entry.add( attribute );

            long syntaxLength = attributeType.getSyntaxLength();
            
            if ( syntaxLength != -1 )
            {
                attribute = new DefaultAttribute( M_LENGTH, Long.toString( syntaxLength ) );
                entry.add( attribute );
            }
        }
    }


    /**
     * Adds the single value value.
     *
     * @param attributeType the attribute type
     * @param entry the entry
     * @throws LdapException
     */
    private static void addSingleValueValue( AttributeType attributeType, Entry entry ) throws LdapException
    {
        if ( attributeType.isSingleValued() )
        {
            Attribute attribute = new DefaultAttribute( M_SINGLE_VALUE, TRUE );
            entry.add( attribute );
        }
    }


    /**
     * Adds the collective value.
     *
     * @param attributeType the attribute type
     * @param entry the entry
     * @throws LdapException
     */
    private static void addCollectiveValue( AttributeType attributeType, Entry entry ) throws LdapException
    {
        if ( attributeType.isCollective() )
        {
            Attribute attribute = new DefaultAttribute( M_COLLECTIVE, TRUE );
            entry.add( attribute );
        }
    }


    /**
     * Adds the no user modification value.
     *
     * @param attributeType the attribute type
     * @param entry the entry
     * @throws LdapException
     */
    private static void addNoUserModificationValue( AttributeType attributeType, Entry entry ) throws LdapException
    {
        if ( !attributeType.isUserModifiable() )
        {
            Attribute attribute = new DefaultAttribute( M_NO_USER_MODIFICATION, TRUE );
            entry.add( attribute );
        }
    }


    /**
     * Adds the usage value.
     *
     * @param attributeType the attribute type
     * @param entry the entry
     * @throws LdapException
     */
    private static void addUsageValue( AttributeType attributeType, Entry entry ) throws LdapException
    {
        UsageEnum usage = attributeType.getUsage();
        
        if ( usage != UsageEnum.USER_APPLICATIONS )
        {
            Attribute attribute = new DefaultAttribute( M_USAGE, usage.render() );
            entry.add( attribute );
        }
    }


    /**
     * Adds the superiors value.
     *
     * @param objectClass the object class
     * @param entry the entry
     * @throws LdapException
     */
    private static void addSuperiorsValue( ObjectClass objectClass, Entry entry ) throws LdapException
    {
        List<String> superiors = objectClass.getSuperiorOids();
        
        if ( ( superiors != null ) && !superiors.isEmpty() )
        {
            Attribute attribute = new DefaultAttribute( M_SUP_OBJECT_CLASS );
            entry.add( attribute );

            for ( String superior : superiors )
            {
                attribute.add( superior );
            }
        }
    }


    /**
     * Adds class type value.
     *
     * @param objectClass the object class
     * @param entry the entry
     * @throws LdapException
     */
    private static void addClassTypeValue( ObjectClass objectClass, Entry entry ) throws LdapException
    {
        ObjectClassTypeEnum classType = objectClass.getType();
        
        if ( classType != ObjectClassTypeEnum.STRUCTURAL )
        {
            Attribute attribute = new DefaultAttribute( M_TYPE_OBJECT_CLASS, classType.toString() );
            entry.add( attribute );
        }
    }


    /**
     * Adds musts value.
     *
     * @param objectClass the object class
     * @param entry the entry
     * @throws LdapException
     */
    private static void addMustsValue( ObjectClass objectClass, Entry entry ) throws LdapException
    {
        List<String> musts = objectClass.getMustAttributeTypeOids();
        
        if ( ( musts != null ) && !musts.isEmpty() )
        {
            Attribute attribute = new DefaultAttribute( M_MUST );
            entry.add( attribute );

            for ( String must : musts )
            {
                attribute.add( must );
            }
        }
    }


    /**
     * Adds mays value.
     *
     * @param objectClass the object class
     * @param entry the entry
     * @throws LdapException
     */
    private static void addMaysValue( ObjectClass objectClass, Entry entry ) throws LdapException
    {
        List<String> mays = objectClass.getMayAttributeTypeOids();
        
        if ( ( mays != null ) && !mays.isEmpty() )
        {
            Attribute attribute = new DefaultAttribute( M_MAY );
            entry.add( attribute );

            for ( String may : mays )
            {
                attribute.add( may );
            }
        }
    }
}
