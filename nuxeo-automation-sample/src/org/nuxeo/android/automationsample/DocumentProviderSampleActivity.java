/*
 * (C) Copyright 2011 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 */

package org.nuxeo.android.automationsample;

import java.util.List;

import org.nuxeo.android.activities.BaseNuxeoActivity;
import org.nuxeo.android.documentprovider.DocumentProvider;
import org.nuxeo.android.documentprovider.LazyUpdatableDocumentsList;
import org.nuxeo.android.documentprovider.LazyUpdatableDocumentsListImpl;
import org.nuxeo.ecm.automation.client.jaxrs.OperationRequest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DocumentProviderSampleActivity extends BaseNuxeoActivity implements
        View.OnClickListener, OnItemClickListener {

    protected ListView listView;

    protected TextView waitingMessage;

    protected View refreshBtn;

    List<String> providerNames;

    public DocumentProviderSampleActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nxcp);
        waitingMessage = (TextView) findViewById(R.id.waitingMessage);
        refreshBtn = findViewById(R.id.refreshBtn);
        refreshBtn.setOnClickListener(this);
        listView = (ListView) findViewById(R.id.myList);
        registerForContextMenu(listView);
        listView.setOnItemClickListener(this);
    }

    protected void registerProviders() {

        Log.i(this.getClass().getSimpleName(), "register proviers .......");
        DocumentProvider docProvider = getAutomationClient().getDocumentProvider();

        // register a query
        String providerName1 = "Simple select";
        if (!docProvider.isRegistred(providerName1)) {
            String query = "select * from Document where ecm:mixinType != \"HiddenInNavigation\" AND ecm:isCheckedInVersion = 0 AND ecm:currentLifeCycleState != \"deleted\" order by dc:modified DESC";
            docProvider.registerNamedProvider(getNuxeoSession(), providerName1,
                    query, 10, false, false, null);
        }

        // register an operation
        String providerName2 = "Get Worklist operation";
        if (!docProvider.isRegistred(providerName2)) {
            // create the fetch operation
            OperationRequest getWorklistOperation = getNuxeoSession().newRequest(
                    "Seam.FetchFromWorklist");
            // define what properties are needed
            getWorklistOperation.setHeader("X-NXDocumentProperties",
                    "common,dublincore");
            // register provider from OperationRequest
            docProvider.registerNamedProvider(providerName2,
                    getWorklistOperation, null, false, false, null);
        }

        // register a documentList
        String providerName3 = "My Documents";
        if (!docProvider.isRegistred(providerName3)) {
            String query2 = "SELECT * FROM Document WHERE dc:contributors = ?";
            LazyUpdatableDocumentsList docList = new LazyUpdatableDocumentsListImpl(
                    getNuxeoSession(), query2,
                    new String[] { "Administrator" }, null, null, 10);
            docList.setName(providerName3);
            docProvider.registerNamedProvider(docList, false);
        }

        // register a query
        String providerName4 = "mypictures";
        if (!docProvider.isRegistred(providerName4)) {
            String query = "select * from Picture";
            docProvider.registerNamedProvider(getNuxeoSession(), providerName4,
                    query, 10, false, false, "image");
        }

        // register a query
        String providerName5 = "mynotes";
        if (!docProvider.isRegistred(providerName5)) {
            String query = "select * from Note";
            docProvider.registerNamedProvider(getNuxeoSession(), providerName5,
                    query, 10, false, false, "text");
        }

    }

    @Override
    protected Object retrieveNuxeoData() throws Exception {
        // be sure we won't start with an empty list
        registerProviders();

        // get declated providers
        DocumentProvider docProvider = getAutomationClient().getDocumentProvider();
        providerNames = docProvider.listProviderNames();

        return true;
    }

    protected void onNuxeoDataRetrievalStarted() {
        waitingMessage.setText("Loading data ...");
        waitingMessage.setVisibility(View.VISIBLE);
        refreshBtn.setEnabled(false);
    }

    @Override
    protected void onNuxeoDataRetrieved(Object data) {
        waitingMessage.setVisibility(View.INVISIBLE);
        refreshBtn.setEnabled(true);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, providerNames);
        listView.setAdapter(adapter);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        if (v.getId() == listView.getId()) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle("Menu for entry" + info.position);
            menu.add(Menu.NONE, 0, 0, "View DocumentsList");
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.listmenu, menu);
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    public void onClick(View arg0) {
        doRefresh();
    }

    protected void doRefresh() {
        runAsyncDataRetrieval();
    }

    // Activity menu handling
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.itemNew:
            startActivityForResult(new Intent(this,
                    DocumentProviderCreateActivity.class), 0);
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Content menu handling
    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getItemId() == 0) {
            startActivity(new Intent(this, DocumentLayoutActivity.class));
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    @Override
    protected boolean requireAsyncDataRetrieval() {
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parentView, View childView,
            int position, long id) {
        String providerName = providerNames.get(position);
        startActivity(new Intent(this, DocumentProviderViewActivity.class).putExtra(
                DocumentProviderViewActivity.PROVIDER_NAME_PARAM, providerName));
    }

}
