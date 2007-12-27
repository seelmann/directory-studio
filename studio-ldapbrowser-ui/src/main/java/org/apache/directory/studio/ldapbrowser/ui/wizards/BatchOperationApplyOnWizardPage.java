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

package org.apache.directory.studio.ldapbrowser.ui.wizards;


import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.naming.InvalidNameException;

import org.apache.directory.shared.ldap.name.LdapDN;
import org.apache.directory.studio.ldapbrowser.common.actions.BrowserSelectionUtils;
import org.apache.directory.studio.ldapbrowser.common.widgets.BaseWidgetUtils;
import org.apache.directory.studio.ldapbrowser.common.widgets.WidgetModifyEvent;
import org.apache.directory.studio.ldapbrowser.common.widgets.WidgetModifyListener;
import org.apache.directory.studio.ldapbrowser.common.widgets.search.SearchPageWrapper;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBookmark;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.ISearch;
import org.apache.directory.studio.ldapbrowser.core.model.ISearchResult;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection.ReferralHandlingMethod;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;


public class BatchOperationApplyOnWizardPage extends WizardPage
{

    private String[] initCurrentSelectionTexts;

    private LdapDN[][] initCurrentSelectionDns;

    private ISearch initSearch;

    private Button currentSelectionButton;

    private Combo currentSelectionCombo;

    private Button searchButton;

    private SearchPageWrapper spw;


    public BatchOperationApplyOnWizardPage( String pageName, BatchOperationWizard wizard )
    {
        super( pageName );
        super.setTitle( "Select Application Entries" );
        super.setDescription( "Please select the entries where the batch operation should be applied to." );
        super.setPageComplete( false );

        this.prepareCurrentSelection();
        this.prepareSearch();
    }


    private void validate()
    {
        setPageComplete( getApplyOnDns() != null || spw.isValid() );
        setErrorMessage( searchButton.getSelection() ? spw.getErrorMessage() : null );
    }


    public void createControl( Composite parent )
    {

        Composite composite = new Composite( parent, SWT.NONE );
        GridLayout gl = new GridLayout( 1, false );
        composite.setLayout( gl );
        composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );

        Composite applyOnGroup = composite;

        this.currentSelectionButton = BaseWidgetUtils.createRadiobutton( applyOnGroup, "Current Selection:", 1 );
        this.currentSelectionButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                enableCurrentSelectionWidgets( currentSelectionButton.getSelection() );
                validate();
            }
        } );

        Composite currentSelectionComposite = BaseWidgetUtils.createColumnContainer( applyOnGroup, 2, 1 );
        BaseWidgetUtils.createRadioIndent( currentSelectionComposite, 1 );
        this.currentSelectionCombo = BaseWidgetUtils.createReadonlyCombo( currentSelectionComposite,
            this.initCurrentSelectionTexts, 0, 1 );
        this.currentSelectionCombo.addModifyListener( new ModifyListener()
        {
            public void modifyText( ModifyEvent e )
            {
                validate();
            }
        } );

        BaseWidgetUtils.createSpacer( applyOnGroup, 1 );
        BaseWidgetUtils.createSpacer( applyOnGroup, 1 );

        this.searchButton = BaseWidgetUtils.createRadiobutton( applyOnGroup, "Results of following Search:", 1 );
        this.searchButton.addSelectionListener( new SelectionAdapter()
        {
            public void widgetSelected( SelectionEvent e )
            {
                enableSearchWidgets( searchButton.getSelection() );
                validate();
            }
        } );

        Composite searchComposite = BaseWidgetUtils.createColumnContainer( applyOnGroup, 2, 1 );
        BaseWidgetUtils.createRadioIndent( searchComposite, 1 );
        Composite innerSearchComposite = BaseWidgetUtils.createColumnContainer( searchComposite, 3, 1 );
        this.spw = new SearchPageWrapper( SearchPageWrapper.NAME_INVISIBLE
            | SearchPageWrapper.RETURNINGATTRIBUTES_INVISIBLE | SearchPageWrapper.REFERRALOPTIONS_READONLY );
        this.spw.createContents( innerSearchComposite );
        this.spw.loadFromSearch( this.initSearch );
        this.spw.addWidgetModifyListener( new WidgetModifyListener()
        {
            public void widgetModified( WidgetModifyEvent event )
            {
                validate();
            }
        } );

        this.currentSelectionButton.setSelection( this.currentSelectionCombo.getItemCount() > 0 );
        this.currentSelectionButton.setEnabled( this.currentSelectionCombo.getItemCount() > 0 );
        this.searchButton.setSelection( this.currentSelectionCombo.getItemCount() == 0 );
        this.enableCurrentSelectionWidgets( this.currentSelectionButton.getSelection() );
        this.enableSearchWidgets( this.searchButton.getSelection() );

        validate();

        setControl( composite );
    }


    public LdapDN[] getApplyOnDns()
    {
        if ( currentSelectionButton.getSelection() )
        {
            int index = currentSelectionCombo.getSelectionIndex();
            return initCurrentSelectionDns[index];
        }
        else
        {
            return null;
        }
    }


    public ISearch getApplyOnSearch()
    {
        if ( searchButton.getSelection() )
        {
            return this.initSearch;
        }
        else
        {
            return null;
        }
    }


    private void enableCurrentSelectionWidgets( boolean b )
    {
        currentSelectionCombo.setEnabled( b );
    }


    private void enableSearchWidgets( boolean b )
    {
        spw.setEnabled( b );
    }


    private void prepareSearch()
    {
        ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService()
            .getSelection();
        this.initSearch = BrowserSelectionUtils.getExampleSearch( selection );
        this.initSearch.setName( null );

        // never follow referrals for a batch operation!
        this.initSearch.setReferralsHandlingMethod( ReferralHandlingMethod.IGNORE );
    }


    private void prepareCurrentSelection()
    {

        ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService()
            .getSelection();
        ISearch[] searches = BrowserSelectionUtils.getSearches( selection );
        IEntry[] entries = BrowserSelectionUtils.getEntries( selection );
        ISearchResult[] searchResults = BrowserSelectionUtils.getSearchResults( selection );
        IBookmark[] bookmarks = BrowserSelectionUtils.getBookmarks( selection );
        IAttribute[] attributes = BrowserSelectionUtils.getAttributes( selection );
        IValue[] values = BrowserSelectionUtils.getValues( selection );

        List<String> textList = new ArrayList<String>();
        List<LdapDN[]> dnsList = new ArrayList<LdapDN[]>();

        if ( attributes.length + values.length > 0 )
        {
            Set<LdapDN> internalDnSet = new LinkedHashSet<LdapDN>();
            for ( int v = 0; v < values.length; v++ )
            {
                if ( values[v].isString() )
                {
                    try
                    {
                        LdapDN dn = new LdapDN( values[v].getStringValue() );
                        internalDnSet.add( dn );
                    }
                    catch ( InvalidNameException e )
                    {
                    }
                }
            }

            for ( int a = 0; a < attributes.length; a++ )
            {
                IValue[] vals = attributes[a].getValues();
                for ( int v = 0; v < vals.length; v++ )
                {
                    if ( vals[v].isString() )
                    {
                        try
                        {
                            LdapDN dn = new LdapDN( vals[v].getStringValue() );
                            internalDnSet.add( dn );
                        }
                        catch ( InvalidNameException e )
                        {
                        }
                    }
                }
            }

            if ( !internalDnSet.isEmpty() )
            {
                dnsList.add( internalDnSet.toArray( new LdapDN[internalDnSet.size()] ) );
                textList.add( "DNs of selected Attributes (" + internalDnSet.size() + " Entries)" );
            }
        }
        if ( searches.length == 1 && searches[0].getSearchResults() != null )
        {
            Set<LdapDN> internalDnSet = new LinkedHashSet<LdapDN>();
            ISearchResult[] srs = searches[0].getSearchResults();
            for ( int i = 0; i < srs.length; i++ )
            {
                internalDnSet.add( srs[i].getDn() );
            }

            dnsList.add( internalDnSet.toArray( new LdapDN[internalDnSet.size()] ) );
            textList.add( "Search Results of '" + searches[0].getName() + "' (" + searches[0].getSearchResults().length
                + " Entries)" );
        }
        if ( entries.length + searchResults.length + bookmarks.length > 0 )
        {
            Set<LdapDN> internalDnSet = new LinkedHashSet<LdapDN>();
            for ( int i = 0; i < entries.length; i++ )
            {
                internalDnSet.add( entries[i].getDn() );
            }
            for ( int i = 0; i < searchResults.length; i++ )
            {
                internalDnSet.add( searchResults[i].getDn() );
            }
            for ( int i = 0; i < bookmarks.length; i++ )
            {
                internalDnSet.add( bookmarks[i].getDn() );
            }

            dnsList.add( internalDnSet.toArray( new LdapDN[internalDnSet.size()] ) );
            textList.add( "Selected Entries (" + internalDnSet.size() + " Entries)" );
        }

        this.initCurrentSelectionTexts = textList.toArray( new String[textList.size()] );
        this.initCurrentSelectionDns = dnsList.toArray( new LdapDN[0][0] );

    }


    public void saveDialogSettings()
    {
        this.spw.saveToSearch( initSearch );
    }

}