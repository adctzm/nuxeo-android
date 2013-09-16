package org.nuxeo.android.testsfrag;

import org.nuxeo.android.automationsample.R;
import org.nuxeo.android.fragments.BaseDocLayoutFragAct;
import org.nuxeo.android.fragments.BaseDocumentLayoutFragment;

public class DocumentLayoutFragActivity extends BaseDocLayoutFragAct {

	@Override
	protected BaseDocumentLayoutFragment createDocumentLayoutFrag() {
		return new DocumentLayoutFragment();
	}

	protected int getActivityLayout() {
		return R.layout.activity_document_layout_frag;
	}

	@Override
	public int getLayoutFragmentContainerId() {
		return R.id.edit_frag_container;
	}

}