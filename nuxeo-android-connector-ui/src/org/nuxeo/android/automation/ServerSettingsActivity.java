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

package org.nuxeo.android.automation;

import java.util.HashMap;
import java.util.Map;

import org.nuxeo.android.activities.AbstractNuxeoSettingsActivity;
import org.nuxeo.android.config.NuxeoServerConfig;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ServerSettingsActivity extends AbstractNuxeoSettingsActivity
        implements OnClickListener {

    protected TextView login;

    protected TextView password;

    protected TextView serverUrl;

    protected Button saveButton;

    CharSequence initPasswordStr, initLoginStr, initUrlStr;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.settings);

        login = (TextView) findViewById(R.id.editLogin);
        initLoginStr = login.getText();
        password = (TextView) findViewById(R.id.editPassword);
        initPasswordStr = password.getText();
        serverUrl = (TextView) findViewById(R.id.editServerUrl);
        initUrlStr = serverUrl.getText();
        saveButton = (Button) findViewById(R.id.saveSettingsButton);
        saveButton.setOnClickListener(this);
        super.onCreate(savedInstanceState);
        refreshDisplay();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDisplay();
    }

    protected void refreshDisplay() {
        Map<String, Object> prefs = getNuxeoPreferences();
        serverUrl.setText(prefs.get(NuxeoServerConfig.PREF_SERVER_URL).toString());
        login.setText(prefs.get(NuxeoServerConfig.PREF_SERVER_LOGIN).toString());
        password.setText(prefs.get(NuxeoServerConfig.PREF_SERVER_PASSWORD).toString());
    }

    @Override
    public void onClick(View view) {
        if (view == saveButton) {
            Map<String, Object> prefs = new HashMap<String, Object>();

            if (!initUrlStr.equals(serverUrl.getText()) || !initLoginStr.equals(login.getText())
            		|| !initPasswordStr.equals(password.getText()))
            {
	            prefs.put(NuxeoServerConfig.PREF_SERVER_URL,
	                    serverUrl.getText().toString());
	            prefs.put(NuxeoServerConfig.PREF_SERVER_LOGIN,
	                    login.getText().toString());
	            prefs.put(NuxeoServerConfig.PREF_SERVER_PASSWORD,
	                    password.getText().toString());
	            setResult(RESULT_OK);
	            saveNuxeoPreferences(prefs);
            }

            finish();
        }
    }

    @Override
    protected boolean requireAsyncDataRetrieval() {
        return false;
    }

}
