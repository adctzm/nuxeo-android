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
package org.nuxeo.android.simpleclient.listing.ui;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.nuxeo.android.simpleclient.R;

import android.content.Context;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

public class TaskItemAttributeUpdater implements ObjectItemViewUpdater {

    private final TextView title;

    private final TextView comment;

    private final TextView dates;

    protected final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "dd/MM/yyyy");

    protected Date readDate(JSONObject dateObject) throws JSONException {
        return new Date(dateObject.getLong("time"));
    }

    public TaskItemAttributeUpdater(View view) {
        title = (TextView) view.findViewById(R.id.title);
        comment = (TextView) view.findViewById(R.id.comment);
        dates = (TextView) view.findViewById(R.id.dates);
    }

    public void update(Context context, Handler handler, Object item) {
        JSONObject task = (JSONObject) item;

        try {
            String directive = task.getString("directive");
            String txtcomment = task.getString("comment");
            Date creationDate = readDate(task.getJSONObject("startDate"));
            Date dueDate = null;
            if (task.has("dueDate")) {
                dueDate = readDate(task.getJSONObject("dueDate"));
            }

            if ("workflowDirectiveValidation".equals(directive)) {
                title.setText("Validate Document");
            } else if ("workflowDirectiveOpinion".equals(directive)) {
                title.setText("Give your opinion");
            } else {
                title.setText(directive);
            }
            comment.setText(txtcomment);
            StringBuffer datesBuffer = new StringBuffer();
            datesBuffer.append("<b>created : </b>");
            datesBuffer.append(dateFormat.format(creationDate));
            if (dueDate != null) {
                datesBuffer.append(" <b style=\"color:red\">due : </b>");
                datesBuffer.append(dateFormat.format(dueDate));
            }
            dates.setText(Html.fromHtml(datesBuffer.toString()));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
