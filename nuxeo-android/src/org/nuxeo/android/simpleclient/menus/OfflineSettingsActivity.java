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
package org.nuxeo.android.simpleclient.menus;

import org.nuxeo.android.simpleclient.R;
import org.nuxeo.android.simpleclient.service.NuxeoAndroidServices;
import org.nuxeo.android.simpleclient.ui.TitleBarAggregate;
import org.nuxeo.android.simpleclient.ui.TitleBarShowHomeFeature;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.smartnsoft.droid4me.app.SmartActivity;
import com.smartnsoft.droid4me.framework.LifeCycle;
import com.smartnsoft.droid4me.framework.LifeCycle.BusinessObjectUnavailableException;

public class OfflineSettingsActivity extends SmartActivity<TitleBarAggregate>
        implements LifeCycle.BusinessObjectsRetrievalAsynchronousPolicy,
        TitleBarShowHomeFeature {

    private int operationCount;

    private long cacheSize;

    private boolean offline;

    private TextView operationCountView;

    private TextView cacheSizeView;

    private TextView offlineView;

    private Button refreshOpAction;

    private Button clearCacheAction;

    @Override
    public void onFulfillDisplayObjects() {
        cacheSizeView.setText("Current cache size : " + (cacheSize / 1024)
                + " KB");
        operationCountView.setText(operationCount
                + " operation definitions (cached)");
        if (offline) {
            offlineView.setText("Nuxeo Client is currently offline");
        } else {
            offlineView.setText("Nuxeo Client is currently online");
        }
    }

    @Override
    public void onRetrieveBusinessObjects()
            throws BusinessObjectUnavailableException {
        operationCount = NuxeoAndroidServices.getInstance().getKnownOperationsCount();
        offline = NuxeoAndroidServices.getInstance().isOfflineMode();
        cacheSize = NuxeoAndroidServices.getInstance().getCacheSize();

    }

    @Override
    public void onRetrieveDisplayObjects() {
        setContentView(R.layout.offline_screen);
        operationCountView = (TextView) findViewById(R.id.operationCount);
        cacheSizeView = (TextView) findViewById(R.id.cacheSize);
        offlineView = (TextView) findViewById(R.id.offlineStatus);

        refreshOpAction = (Button) findViewById(R.id.refetchOperations);
        refreshOpAction.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                NuxeoAndroidServices.getInstance().refreshOperationCache();
            }
        });

        clearCacheAction = (Button) findViewById(R.id.clearCache);
        clearCacheAction.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                NuxeoAndroidServices.getInstance().flushCache();
            }
        });
    }

    @Override
    public void onSynchronizeDisplayObjects() {
        operationCount = NuxeoAndroidServices.getInstance().getKnownOperationsCount();
        offline = NuxeoAndroidServices.getInstance().isOfflineMode();
        cacheSize = NuxeoAndroidServices.getInstance().getCacheSize();
        cacheSizeView.setText("Current cache size : " + (cacheSize / 1024)
                + " KB");
        operationCountView.setText(operationCount
                + " operation definitions (cached)");
        if (offline) {
            offlineView.setText("Nuxeo Client is currently offline");
        } else {
            offlineView.setText("Nuxeo Client is currently online");
        }
    }

}
