package dssp.brailleLib;

import java.util.EventListener;
import java.util.List;

import dssp.brailleLib.BrailleBox;

public interface BrailleEditListener extends EventListener
{
	/**
	 * BrailleInfoのリストにlistが追加されている
	 *
	 * @param boxList
	 */
	public void addingBox(List<BrailleBox> boxList);
	/**
	 * BrailleInfoのリストにlistが追加された
	 *
	 * @param boxList
	 */
	public void addedBox(List<BrailleBox> boxList);
	/**
	 * BrailleInfoのリストからboxListが削除された
	 *
	 * @param boxList
	 */
	public void deletedBox(List<BrailleBox> boxList);
}
