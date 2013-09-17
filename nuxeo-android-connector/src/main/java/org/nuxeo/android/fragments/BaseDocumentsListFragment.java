package org.nuxeo.android.fragments;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.nuxeo.android.activities.BaseDocumentLayoutActivity;
import org.nuxeo.android.documentprovider.LazyDocumentsList;
import org.nuxeo.android.documentprovider.LazyUpdatableDocumentsList;
import org.nuxeo.android.layout.LayoutMode;
import org.nuxeo.ecm.automation.client.cache.CacheBehavior;
import org.nuxeo.ecm.automation.client.jaxrs.OperationRequest;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public abstract class BaseDocumentsListFragment extends BaseListFragment {

    public static final int ACTION_EDIT_DOCUMENT = 0;

    protected static final int ACTION_CREATE_DOCUMENT = 1;

    protected static final int MNU_NEW_LISTITEM = 10;

    protected static final int MNU_SORT = 100;

    protected static final int MNU_REFRESH = 2;

    protected static final int CTXMNU_VIEW_DOCUMENT = 0;

    protected static final int CTXMNU_EDIT_DOCUMENT = 1;

    protected static final int CTXMNU_VIEW_ATTACHEMENT = 2;

    protected static final int CTXMNU_DELETE = 3;

    protected boolean refresh = false;
    
    protected LazyUpdatableDocumentsList documentsList;

    protected LinkedHashMap<String, String> allowedDocumentTypes;
    
    public BaseDocumentsListFragment() {
    }
    
    // Executed on the background thread to avoid freezing the UI
    @Override
    protected Object retrieveNuxeoData() throws Exception {
        byte cacheParam = CacheBehavior.STORE;
        if (refresh) {
            cacheParam = (byte) (cacheParam | CacheBehavior.FORCE_REFRESH);
            refresh = false;
        }
        return fetchDocumentsList(cacheParam, "");
    }

    protected void forceRefresh() {
        refresh = true;
    }
    

    // Called on the UIThread when Nuxeo data has been retrieved
    @Override
    protected void onNuxeoDataRetrieved(Object data) {
        super.onNuxeoDataRetrieved(data);
        if (data != null) {
            // get the DocumentList from the async call result
            documentsList = (LazyUpdatableDocumentsList) data;
            displayDocumentList(listView, documentsList);
        }
    }
    
    public void doRefresh() {
        if (documentsList != null) {
            documentsList.refreshAll();
        } else {
            runAsyncDataRetrieval();
        }
    }

    protected LazyDocumentsList getDocumentsList() {
        return documentsList;
    }

    @Override
    public boolean isReady() {
        if (super.isReady()) {
            if (documentsList != null) {
                return documentsList.getLoadingPagesCount() == 0
                        && documentsList.getLoadedPageCount() > 0;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    
    protected abstract LazyUpdatableDocumentsList fetchDocumentsList(
            byte cacheParam, String order) throws Exception;

    @Deprecated
	/**
	 * @deprecated Since 2.0. prefer @fetchDocumentsList(byte cacheParam, String order)
	 */
    protected LazyUpdatableDocumentsList fetchDocumentsList(
            byte cacheParam) throws Exception{
    	return fetchDocumentsList(cacheParam, "");
    }
    
    protected abstract void displayDocumentList(ListView listView,
            LazyDocumentsList documentsList);

    protected abstract Document initNewDocument(String type);

    protected abstract Class<? extends BaseDocLayoutFragAct> getEditActivityClass();

    protected void onDocumentCreate(Document newDocument) {
        documentsList.createDocument(newDocument);
    }

    public void onDocumentUpdate(Document editedDocument) {
        documentsList.updateDocument(editedDocument);
    }

    protected Document getContextMenuDocument(int selectedPosition) {
        return documentsList.getDocument(selectedPosition);
    }

    protected void registerDocTypesForCreation(String type, String label) {
        if (allowedDocumentTypes == null) {
            allowedDocumentTypes = new LinkedHashMap<String, String>();
        }
        allowedDocumentTypes.put(type, label);
    }

    protected LinkedHashMap<String, String> getDocTypesForCreation() {
        if (allowedDocumentTypes == null) {
            LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
            map.put("File", "File Document");
            map.put("Note", "Note Document");
            map.put("Folder", "Folder Document");
            return map;
        } else {
            return allowedDocumentTypes;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MNU_REFRESH:
            doRefresh();
            break;
        default:
            if (item.getItemId() > MNU_NEW_LISTITEM) {
                int idx = item.getItemId() - MNU_NEW_LISTITEM - 1;
                if (idx < getDocTypesForCreation().size()) {
                    String type = new ArrayList<String>(
                            getDocTypesForCreation().keySet()).get(idx);
                    forceRefresh();
                    Document newDoc = initNewDocument(type);
                    if (newDoc != null) {
                    	mCallback.openDocument(newDoc, LayoutMode.CREATE);
                    }
                }

            } else if (item.getItemId() == MNU_NEW_LISTITEM) {
                if (getDocTypesForCreation().size() == 1) {
                    forceRefresh();
                    Document newDoc = initNewDocument(getDocTypesForCreation().keySet().iterator().next());
                    if (newDoc != null) {
                    	mCallback.openDocument(newDoc, LayoutMode.CREATE);
                    }
                }
            }
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    public interface Callback {
    	void openDocument(Document newDoc, LayoutMode create);
    }
    
    protected Callback mCallback;
    
    @Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callback)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallback = (Callback) activity;
	}

    // Content menu handling
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int selectedPosition = info.position;
        Document doc = getContextMenuDocument(selectedPosition);

        if (item.getItemId() == CTXMNU_VIEW_DOCUMENT) {
            mCallback.openDocument(doc, LayoutMode.VIEW);
            return true;
        } else if (item.getItemId() == CTXMNU_EDIT_DOCUMENT) {
            mCallback.openDocument(doc, LayoutMode.EDIT);
            return true;
        } else if (item.getItemId() == CTXMNU_VIEW_ATTACHEMENT) {
            Uri blobUri = doc.getBlob();
            if (blobUri == null) {
                Toast.makeText(getActivity().getBaseContext(), "No Attachement available ",
                        Toast.LENGTH_SHORT).show();
            } else {
                startViewerFromBlob(blobUri);
            }
            return true;
        } else if (item.getItemId() == CTXMNU_DELETE) {
        	deleteDocument(doc);
        	doRefresh();
        	return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }
    
    protected void deleteDocument(Document doc) {
        OperationRequest request = getNuxeoSession().newRequest(
                "Document.SetLifeCycle");
        request.setInput(doc);
        request.set("value", "delete");
        documentsList.updateDocument(doc, request);
    }

    @Override
    protected void onListItemClicked(int listItemPosition) {
		mCallback.openDocument(documentsList.getDocument(listItemPosition), LayoutMode.VIEW);
    }
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setHasOptionsMenu(true);
    }
    
    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater){
    	super.onCreateOptionsMenu(menu, inflater);
		LinkedHashMap<String, String> types = getDocTypesForCreation();
		if (Build.VERSION.SDK_INT >= 11) {
			if (types.size() > 0) {
				if (types.size() == 1) {
					menu.add(Menu.NONE, MNU_NEW_LISTITEM, 0, "New Item")
							.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
				} else {
					SubMenu subMenu = menu.addSubMenu(Menu.NONE,
							MNU_NEW_LISTITEM, 0, "New item");
					subMenu.getItem().setShowAsAction(
							MenuItem.SHOW_AS_ACTION_IF_ROOM);
					int idx = 1;
					for (String key : types.keySet()) {
						subMenu.add(Menu.NONE, MNU_NEW_LISTITEM + idx, idx,
								types.get(key));
						idx++;
					}
				}
			}
			menu.add(Menu.NONE, MNU_REFRESH, 1, "Refresh").setShowAsAction(
					MenuItem.SHOW_AS_ACTION_IF_ROOM);
		} else {
			if (types.size() > 0) {
				if (types.size() == 1) {
					menu.add(Menu.NONE, MNU_NEW_LISTITEM, 0, "New Item");
				} else {
					SubMenu subMenu = menu.addSubMenu(Menu.NONE,
							MNU_NEW_LISTITEM, 0, "New item");
					subMenu.getItem();
					int idx = 1;
					for (String key : types.keySet()) {
						subMenu.add(Menu.NONE, MNU_NEW_LISTITEM + idx, idx,
								types.get(key));
						idx++;
					}
				}
			}
			menu.add(Menu.NONE, MNU_REFRESH, 1, "Refresh");
		}
	}

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        if (v.getId() == listView.getId()) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            Document doc = documentsList.getDocument(info.position);
            menu.setHeaderTitle(doc.getTitle());
            populateContextMenu(doc, menu);
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    protected void populateContextMenu(Document doc, ContextMenu menu) {
        menu.add(Menu.NONE, CTXMNU_VIEW_DOCUMENT, 0, "View");
        menu.add(Menu.NONE, CTXMNU_EDIT_DOCUMENT, 1, "Edit");
        menu.add(Menu.NONE, CTXMNU_VIEW_ATTACHEMENT, 2, "View attachment");
        menu.add(Menu.NONE, CTXMNU_DELETE, 2, "Delete");
    }

}
