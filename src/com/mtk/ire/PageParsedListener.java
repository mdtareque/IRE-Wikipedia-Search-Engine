package com.mtk.ire;
import java.util.List;

public class PageParsedListener implements IPageParsedListener {

	static int cnt = 0;

	public void dispatch(List<Page> pages) {
		// System.out.println("Listener got "+pages.size()+" pages.");
		for (Page p : pages) {

			// 4615, 4653, 2642, 2792
			// 1020 , 1495, 724: infobo
			// 5636 category
			// 5706 : check cite in refs
			// 5636, 3386, 635
			// if(p.docId == 339) { System.err.println("done");
			PageProcessor.processPage(p);
			// }
			cnt++;
			if (cnt % 500 == 0) System.out.println("pages processed so far " + cnt);
		}
	}
}
