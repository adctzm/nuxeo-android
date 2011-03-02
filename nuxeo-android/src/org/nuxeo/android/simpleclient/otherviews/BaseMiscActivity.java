package org.nuxeo.android.simpleclient.otherviews;

import org.nuxeo.android.simpleclient.NuxeoAndroidApplication;
import org.nuxeo.android.simpleclient.menus.SettingsActivity;
import org.nuxeo.android.simpleclient.ui.TitleBarAggregate;
import org.nuxeo.android.simpleclient.ui.TitleBarRefreshFeature;
import org.nuxeo.android.simpleclient.ui.TitleBarShowHomeFeature;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;

import android.widget.ImageView;

import com.smartnsoft.droid4me.app.AppPublics;
import com.smartnsoft.droid4me.app.SmartActivity;
import com.smartnsoft.droid4me.app.AppPublics.BroadcastListener;
import com.smartnsoft.droid4me.app.AppPublics.BroadcastListenerProvider;
import com.smartnsoft.droid4me.app.AppPublics.SendLoadingIntent;
import com.smartnsoft.droid4me.download.ImageDownloader;
import com.smartnsoft.droid4me.framework.LifeCycle.BusinessObjectsRetrievalAsynchronousPolicy;

public abstract class BaseMiscActivity extends SmartActivity<TitleBarAggregate>
        implements BusinessObjectsRetrievalAsynchronousPolicy,
        SendLoadingIntent, BroadcastListenerProvider, TitleBarShowHomeFeature,
        TitleBarRefreshFeature {

    public static final String DOCUMENT = "document";

    protected boolean refresh = false;

    protected ImageView icon;

    public BroadcastListener getBroadcastListener() {
        return new AppPublics.LoadingBroadcastListener(this, true) {
            @Override
            protected void onLoading(boolean isLoading) {
                getAggregate().getAttributes().toggleRefresh(isLoading);
            }
        };
    }

    protected String getTargetDocId() {
        return getTargetDoc().getId();
    }

    protected Document getTargetDoc() {
        return (Document) getIntent().getSerializableExtra(DOCUMENT);
    }


    protected void fetchIcon(Document targetDocument) {
        if (icon!=null) {
            final String serverUrl = getSharedPreferences(
                    "org.nuxeo.android.simpleclient_preferences", 0).getString(
                    SettingsActivity.PREF_SERVER_URL, "");
            String urlImage = serverUrl + (serverUrl.endsWith("/") ? "" : "/")
                    + targetDocument.getString("common:icon", "");
            ImageDownloader.getInstance().get(icon, urlImage, null,
                    this.getHandler(),
                    NuxeoAndroidApplication.CACHE_IMAGE_INSTRUCTIONS);
        }
    }

    @Override
    public void onTitleBarRefresh() {
        refresh = true;
        refreshBusinessObjectsAndDisplay(true);
    }

}
