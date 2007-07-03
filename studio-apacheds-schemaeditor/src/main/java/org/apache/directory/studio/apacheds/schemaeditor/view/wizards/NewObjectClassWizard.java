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
package org.apache.directory.studio.apacheds.schemaeditor.view.wizards;


import org.apache.directory.studio.apacheds.schemaeditor.Activator;
import org.apache.directory.studio.apacheds.schemaeditor.model.ObjectClassImpl;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;


/**
 * This class represents the wizard to create a new ObjectClass.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class NewObjectClassWizard extends Wizard implements INewWizard
{
    // The pages of the wizards
    private NewObjectClassGeneralPageWizardPage generalPage;
    private NewObjectClassContentWizardPage contentPage;
    private NewObjectClassMandatoryAttributesPage mandatoryAttributesPage;
    private NewObjectClassOptionalAttributesPage optionalAttributesPage;


    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    public void addPages()
    {
        // Creating pages
        generalPage = new NewObjectClassGeneralPageWizardPage();
        contentPage = new NewObjectClassContentWizardPage();
        mandatoryAttributesPage = new NewObjectClassMandatoryAttributesPage();
        optionalAttributesPage = new NewObjectClassOptionalAttributesPage();

        // Adding pages
        addPage( generalPage );
        addPage( contentPage );
        addPage( mandatoryAttributesPage );
        addPage( optionalAttributesPage );
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    public boolean performFinish()
    {
        ObjectClassImpl newOC = new ObjectClassImpl( generalPage.getOidValue() );
        newOC.setSchema( generalPage.getSchemaValue() );
        newOC.setNames( generalPage.getAliasesValue() );
        newOC.setDescription( generalPage.getDescriptionValue() );
        newOC.setSuperClassesNames( contentPage.getSuperiorsNameValue() );
        newOC.setType( contentPage.getClassTypeValue() );
        newOC.setObsolete( contentPage.getObsoleteValue() );
        newOC.setMustNamesList( mandatoryAttributesPage.getMandatoryAttributeTypesNames() );
        newOC.setMustNamesList( optionalAttributesPage.getOptionalAttributeTypesNames() );

        Activator.getDefault().getSchemaHandler().addObjectClass( newOC );

        return true;
    }


    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init( IWorkbench workbench, IStructuredSelection selection )
    {
        // Nothing to do.
    }


    /**
     * Gets the mandatory attributes page.
     *
     * @return
     *      the mandatory attributes page
     */
    public NewObjectClassMandatoryAttributesPage getMandatoryAttributesPage()
    {
        return mandatoryAttributesPage;
    }


    /**
     * Gets the optional attributes page.
     *
     * @return
     *      the optional attributes page
     */
    public NewObjectClassOptionalAttributesPage getOptionalAttributesPage()
    {
        return optionalAttributesPage;
    }
}
