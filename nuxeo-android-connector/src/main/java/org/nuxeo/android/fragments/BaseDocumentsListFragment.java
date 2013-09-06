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
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public abstract class BaseDocumentsListFragment extends BaseListFragment {

    public static final int ACTION_EDIT_DOCUMENT = 0;

    protected static final int ACTION_CREATE_DOCUMENT = 1;

    protected static final int MNU_NEW_LISTITEM = 10;

//    protected static final int MNU_VIEW_LIST_EXTERNAL = 1;

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
        return fetchDocumentsList(cacheParam);
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
            byte cacheParam) throws Exception;

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

    // Activity menu handling
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MNU_REFRESH:
            doRefresh();
            break;
//        case MNU_VIEW_LIST_EXTERNAL:
//            if (getDocumentsList() != null) {
//                Uri contentUri = getDocumentsList().getContentUri();
//                if (contentUri != null) {
//                    Intent intent = new Intent(Intent.ACTION_VIEW);
//                    intent.setData(contentUri);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    intent.putExtra("windowTitle", "Nuxeo Media Browser");
//                    try {
//                        startActivity(intent);
//                    } catch (android.content.ActivityNotFoundException e) {
//                        Toast.makeText(
//                        		getActivity().getBaseContext(),
//                                "No Application Available to View this uri "
//                                        + contentUri.toString(),
//                                Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    Toast.makeText(getActivity().getBaseContext(), "No Uri defined for this list",
//                            Toast.LENGTH_SHORT).show();
//                }
//            }
//            break;
        default:
            if (getEditActivityClass() == null) {
                return true;
            }
            if (item.getItemId() > MNU_NEW_LISTITEM) {
                int idx = item.getItemId() - MNU_NEW_LISTITEM - 1;
                if (idx < getDocTypesForCreation().size()) {
                    String type = new ArrayList<String>(
                            getDocTypesForCreation().keySet()).get(idx);
                    forceRefresh();
                    Document newDoc = initNewDocument(type);
                    if (newDoc != null) {
                        startActivityForResult(
                                new Intent(getActivity().getBaseContext(), getEditActivityClass()).putExtra(
                                        BaseDocumentLayoutActivity.DOCUMENT,
                                        newDoc).putExtra(
                                        BaseDocumentLayoutActivity.MODE,
                                        LayoutMode.CREATE),
                                ACTION_CREATE_DOCUMENT);
                    }
                }

            } else if (item.getItemId() == MNU_NEW_LISTITEM) {
                if (getDocTypesForCreation().size() == 1) {
                    forceRefresh();
                    Document newDoc = initNewDocument(getDocTypesForCreation().keySet().iterator().next());
                    if (newDoc != null) {
                        startActivityForResult(
                                new Intent(getActivity().getBaseContext(), getEditActivityClass()).putExtra(
                                        BaseDocumentLayoutActivity.DOCUMENT,
                                        newDoc).putExtra(
                                        BaseDocumentLayoutActivity.MODE,
                                        LayoutMode.CREATE),
                                ACTION_CREATE_DOCUMENT);
                    }
                }
            }
            break;
        }

        return super.onOptionsItemSelected(item);
    }

//    @Override
//	public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == ACTION_EDIT_DOCUMENT && resultCode == RESULT_OK) {
//            if (data.hasExtra(BaseDocumentLayoutActivity.DOCUMENT)) {
//                Document editedDocument = (Document) data.getExtras().get(
//                        BaseDocumentLayoutActivity.DOCUMENT);
//                onDocumentUpdate(editedDocument);
//            	doRefresh();
//                
//            }
//        } else if (requestCode == ACTION_CREATE_DOCUMENT
//                && resultCode == RESULT_OK) {
//            if (data.hasExtra(BaseDocumentLayoutActivity.DOCUMENT)) {
//                Document newDocument = (Document) data.getExtras().get(
//                        BaseDocumentLayoutActivity.DOCUMENT);
//                onDocumentCreate(newDocument);
//            	doRefresh();
//            }
//        }
//    }
    
    public interface Callback {
    	void viewDocument(Document doc);
    	void editDocument(Document doc);
		void viewDocument(LazyUpdatableDocumentsList documentsList, int listItemPosition);
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
            if (getEditActivityClass() == null) {
                Toast.makeText(getActivity().getBaseContext(), "No View activity defined ",
                        Toast.LENGTH_SHORT).show();
                return true;
            }
            mCallback.viewDocument(doc);
            return true;
        } else if (item.getItemId() == CTXMNU_EDIT_DOCUMENT) {
            if (getEditActivityClass() == null) {
                Toast.makeText(getActivity().getBaseContext(), "No Edit activity defined ",
                        Toast.LENGTH_SHORT).show();
                return true;
            }
            mCallback.editDocument(doc);
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
		mCallback.viewDocument(documentsList, listItemPosition);
    }
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setHasOptionsMenu(true);
    }
    
    @Override
    public void onPrepareOptionsMenu(Menu menu){
    	super.onPrepareOptionsMenu(menu);
    	menu.clear();
    	LinkedHashMap<String, String> types = getDocTypesForCreation();
        if (types.size() > 0) {
            if (types.size() == 1) {
                menu.add(Menu.NONE, MNU_NEW_LISTITEM, 0, "New Item").
        		setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            } else {
                SubMenu subMenu = menu.addSubMenu(Menu.NONE, MNU_NEW_LISTITEM,
                        0, "New item");
                subMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                int idx = 1;
                for (String key : types.keySet()) {
                    subMenu.add(Menu.NONE, MNU_NEW_LISTITEM + idx, idx,
                            types.get(key));
                    idx++;
                }
            }
        }
        menu.add(Menu.NONE, MNU_REFRESH, 1, "Refresh").
		setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
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
