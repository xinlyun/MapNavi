package com.xiaopeng.xmapnavi.view.appwidget.selfview;

public interface OnDeleteListioner {
	public abstract boolean isCandelete(int position);
	public abstract void onDelete(int ID);
	public abstract void onBack();
}
