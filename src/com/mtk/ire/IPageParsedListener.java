package com.mtk.ire;
import java.util.EventListener;
import java.util.List;

interface IPageParsedListener extends EventListener {
	void dispatch(List<Page> p);
}

