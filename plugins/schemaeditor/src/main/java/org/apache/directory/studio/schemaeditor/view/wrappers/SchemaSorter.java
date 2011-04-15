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

package org.apache.directory.studio.schemaeditor.view.wrappers;


import java.util.Comparator;

import org.apache.directory.studio.schemaeditor.model.Schema;


/**
 * This class is used to compare and sort ascending two Schemas
 */
public class SchemaSorter implements Comparator<TreeNode>
{
    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare( TreeNode o1, TreeNode o2 )
    {
        if ( ( o1 instanceof SchemaWrapper ) && ( o2 instanceof SchemaWrapper ) )
        {
            Schema s1 = ( ( SchemaWrapper ) o1 ).getSchema();
            Schema s2 = ( ( SchemaWrapper ) o2 ).getSchema();

            return s1.getSchemaName().compareToIgnoreCase( s2.getSchemaName() );
        }

        // Default
        return o1.toString().compareToIgnoreCase( o2.toString() );
    }
}