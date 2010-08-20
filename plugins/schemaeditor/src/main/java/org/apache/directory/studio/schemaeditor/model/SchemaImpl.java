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
package org.apache.directory.studio.schemaeditor.model;


import java.util.ArrayList;
import java.util.List;


/**
 * This class represents a schema.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SchemaImpl implements Schema
{
    /** The name */
    private String name;

    /** The project */
    private Project project;

    /** The AttributeType List */
    private List<AttributeTypeImpl> attributeTypes = new ArrayList<AttributeTypeImpl>();

    /** The ObjectClass List */
    private List<ObjectClassImpl> objectClasses = new ArrayList<ObjectClassImpl>();

    /** The MatchingRule List */
    private List<MatchingRuleImpl> matchingRules = new ArrayList<MatchingRuleImpl>();

    /** The Syntax List */
    private List<SyntaxImpl> syntaxes = new ArrayList<SyntaxImpl>();


    /**
     * Creates a new instance of SchemaImpl.
     *
     * @param name
     *      the name of the schema
     */
    public SchemaImpl( String name )
    {
        this.name = name;
    }


    /* (non-Javadoc)
     * @see org.apache.directory.studio.schemaeditor.model.Schema#addAttributeType(org.apache.directory.shared.ldap.schema.AttributeType)
     */
    public boolean addAttributeType( AttributeTypeImpl at )
    {
        return attributeTypes.add( at );
    }


    /* (non-Javadoc)
     * @see org.apache.directory.studio.schemaeditor.model.Schema#addMatchingRule(org.apache.directory.shared.ldap.schema.MatchingRule)
     */
    public boolean addMatchingRule( MatchingRuleImpl mr )
    {
        return matchingRules.add( mr );
    }


    /* (non-Javadoc)
     * @see org.apache.directory.studio.schemaeditor.model.Schema#addObjectClass(org.apache.directory.shared.ldap.schema.ObjectClass)
     */
    public boolean addObjectClass( ObjectClassImpl oc )
    {
        return objectClasses.add( oc );
    }


    /* (non-Javadoc)
     * @see org.apache.directory.studio.schemaeditor.model.Schema#addSyntax(org.apache.directory.shared.ldap.schema.Syntax)
     */
    public boolean addSyntax( SyntaxImpl syntax )
    {
        return syntaxes.add( syntax );
    }


    /* (non-Javadoc)
     * @see org.apache.directory.studio.schemaeditor.model.Schema#getAttributeType(java.lang.String)
     */
    public AttributeTypeImpl getAttributeType( String id )
    {
        for ( AttributeTypeImpl at : attributeTypes )
        {
            String[] aliases = at.getNamesRef();
            if ( aliases != null )
            {
                for ( String alias : aliases )
                {
                    if ( alias.equalsIgnoreCase( id ) )
                    {
                        return at;
                    }
                }
            }
            if ( at.getOid().equalsIgnoreCase( id ) )
            {
                return at;
            }
        }

        return null;
    }


    /* (non-Javadoc)
     * @see org.apache.directory.studio.schemaeditor.model.Schema#getAttributeTypes()
     */
    public List<AttributeTypeImpl> getAttributeTypes()
    {
        return attributeTypes;
    }


    /* (non-Javadoc)
     * @see org.apache.directory.studio.schemaeditor.model.Schema#getMatchingRule(java.lang.String)
     */
    public MatchingRuleImpl getMatchingRule( String id )
    {
        for ( MatchingRuleImpl mr : matchingRules )
        {
            String[] aliases = mr.getNamesRef();
            if ( aliases != null )
            {
                for ( String alias : aliases )
                {
                    if ( alias.equalsIgnoreCase( id ) )
                    {
                        return mr;
                    }
                }
            }
            if ( mr.getOid().equalsIgnoreCase( id ) )
            {
                return mr;
            }
        }

        return null;
    }


    /* (non-Javadoc)
     * @see org.apache.directory.studio.schemaeditor.model.Schema#getMatchingRules()
     */
    public List<MatchingRuleImpl> getMatchingRules()
    {
        return matchingRules;
    }


    /* (non-Javadoc)
     * @see org.apache.directory.studio.schemaeditor.model.Schema#getName()
     */
    public String getName()
    {
        return name;
    }


    /* (non-Javadoc)
     * @see org.apache.directory.studio.schemaeditor.model.Schema#getProject()
     */
    public Project getProject()
    {
        return project;
    }


    /* (non-Javadoc)
     * @see org.apache.directory.studio.schemaeditor.model.Schema#getObjectClass(java.lang.String)
     */
    public ObjectClassImpl getObjectClass( String id )
    {
        for ( ObjectClassImpl oc : objectClasses )
        {
            String[] aliases = oc.getNamesRef();
            if ( aliases != null )
            {
                for ( String alias : aliases )
                {
                    if ( alias.equalsIgnoreCase( id ) )
                    {
                        return oc;
                    }
                }
            }
            if ( oc.getOid().equalsIgnoreCase( id ) )
            {
                return oc;
            }
        }

        return null;
    }


    /* (non-Javadoc)
     * @see org.apache.directory.studio.schemaeditor.model.Schema#getObjectClasses()
     */
    public List<ObjectClassImpl> getObjectClasses()
    {
        return objectClasses;
    }


    /* (non-Javadoc)
     * @see org.apache.directory.studio.schemaeditor.model.Schema#getSyntax(java.lang.String)
     */
    public SyntaxImpl getSyntax( String id )
    {
        for ( SyntaxImpl syntax : syntaxes )
        {
            String[] aliases = syntax.getNamesRef();
            if ( aliases != null )
            {
                for ( String alias : aliases )
                {
                    if ( alias.equalsIgnoreCase( id ) )
                    {
                        return syntax;
                    }
                }
            }
            if ( syntax.getOid().equalsIgnoreCase( id ) )
            {
                return syntax;
            }
        }

        return null;
    }


    /* (non-Javadoc)
     * @see org.apache.directory.studio.schemaeditor.model.Schema#getSyntaxes()
     */
    public List<SyntaxImpl> getSyntaxes()
    {
        return syntaxes;
    }


    /* (non-Javadoc)
     * @see org.apache.directory.studio.schemaeditor.model.Schema#removeAttributeType(org.apache.directory.shared.ldap.schema.AttributeType)
     */
    public boolean removeAttributeType( AttributeTypeImpl at )
    {
        return attributeTypes.remove( at );
    }


    /* (non-Javadoc)
     * @see org.apache.directory.studio.schemaeditor.model.Schema#removeMatchingRule(org.apache.directory.shared.ldap.schema.MatchingRule)
     */
    public boolean removeMatchingRule( MatchingRuleImpl mr )
    {
        return matchingRules.remove( mr );
    }


    /* (non-Javadoc)
     * @see org.apache.directory.studio.schemaeditor.model.Schema#removeObjectClass(org.apache.directory.shared.ldap.schema.ObjectClass)
     */
    public boolean removeObjectClass( ObjectClassImpl oc )
    {
        return objectClasses.remove( oc );
    }


    /* (non-Javadoc)
     * @see org.apache.directory.studio.schemaeditor.model.Schema#removeSyntax(org.apache.directory.shared.ldap.schema.Syntax)
     */
    public boolean removeSyntax( SyntaxImpl syntax )
    {
        return syntaxes.remove( syntax );
    }


    /* (non-Javadoc)
     * @see org.apache.directory.studio.schemaeditor.model.Schema#setName(java.lang.String)
     */
    public void setName( String name )
    {
        this.name = name;
    }


    /* (non-Javadoc)
     * @see org.apache.directory.studio.schemaeditor.model.Schema#setProject(org.apache.directory.studio.schemaeditor.model.Project)
     */
    public void setProject( Project project )
    {
        this.project = project;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return getName();
    }
}