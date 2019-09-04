package dssp.brailleLib;

import java.awt.Image;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class ImageSelection implements Transferable, ClipboardOwner {

	protected Image data;

	/** コンストラクター */
	public ImageSelection(Image image) {
		this.data = image;
	}

	/** 対応しているフレーバーを返す */
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { DataFlavor.imageFlavor };
	}

	/** フレーバーが対応しているかどうか */
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return DataFlavor.imageFlavor.equals(flavor);
	}

	/** 保持している画像を返す */
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (DataFlavor.imageFlavor.equals(flavor)) {
			return data;
		}
		throw new UnsupportedFlavorException(flavor);
	}

	/** クリップボードのデータとして不要になった時に呼ばれる */
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		this.data = null;
	}
}
