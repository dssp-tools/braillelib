package dssp.brailleLib;

import java.util.EventListener;
import java.util.List;

public interface BrailleEditListener extends EventListener {
    /**
     * BrailleInfoのリストにlistが追加されている
     *
     * @param boxList BrailleBoxリスト
     */
    public void addingBox(List<BrailleBox> boxList);

    /**
     * BrailleInfoのリストにlistが追加された
     *
     * @param boxList BrailleBoxリスト
     */
    public void addedBox(List<BrailleBox> boxList);

    /**
     * BrailleInfoのリストからboxListが削除された
     *
     * @param boxList BrailleBoxリスト
     */
    public void deletedBox(List<BrailleBox> boxList);
}
