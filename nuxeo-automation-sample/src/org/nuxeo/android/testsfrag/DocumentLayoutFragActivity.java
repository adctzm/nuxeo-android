package org.nuxeo.android.testsfrag;

import org.nuxeo.android.automationsample.R;
import org.nuxeo.android.fragments.BaseDocLayoutFragAct;
import org.nuxeo.android.fragments.BaseDocumentLayoutFragment;
import android.content.Intent;
import android.widget.Toast;

public class DocumentLayoutFragActivity extends BaseDocLayoutFragAct {

	@Override
	protected BaseDocumentLayoutFragment createDocumentLayoutFrag() {
		return new DocumentLayoutFragment();
	}

	protected int getActivityLayout() {
		return R.layout.activity_document_layout_frag;
	}

	@Override
	public int getFragmentContainerId() {
		return R.id.edit_frag_container;
	}
	
//	@Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		//should be called anyway, but isn't...
//		getDocumentLayoutFrag().onActivityResult(requestCode, resultCode, data);
//        super.onActivityResult(requestCode, resultCode, data);
//    }

}