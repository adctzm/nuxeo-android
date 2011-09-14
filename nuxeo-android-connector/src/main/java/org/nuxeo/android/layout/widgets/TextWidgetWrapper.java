package org.nuxeo.android.layout.widgets;

import org.nuxeo.android.adapters.DocumentAttributeResolver;
import org.nuxeo.android.layout.LayoutMode;
import org.nuxeo.android.layout.NuxeoWidget;
import org.nuxeo.android.layout.WidgetDefinition;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;

import android.app.Activity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class TextWidgetWrapper extends BaseAndroidWidgetWrapper implements AndroidWidgetWrapper {

	@Override
	public void applyChanges(View nativeWidget, LayoutMode mode, Document doc,
			String attributeName, NuxeoWidget nuxeoWidget) {
		TextView widget = (TextView) nativeWidget;
		DocumentAttributeResolver.put(doc, attributeName, widget.getText().toString());
	}

	@Override
	public void refresh(View nativeWidget, LayoutMode mode, Document doc,
			String attributeName, NuxeoWidget widget) {
		((TextView)nativeWidget).setText(DocumentAttributeResolver.getString(doc, attributeName));
	}

	@Override
	public View build(Activity ctx, LayoutMode mode, Document doc,
			String attributeName, WidgetDefinition widgetDef) {
		if (mode==LayoutMode.VIEW) {
			TextView widget = new TextView(ctx);
			widget.setText(DocumentAttributeResolver.getString(doc, attributeName));
			return widget;
		} else {
			EditText widget = new EditText(ctx);
			widget.setText(DocumentAttributeResolver.getString(doc, attributeName));
			return widget;
		}
	}

}
