package dssp.brailleLib;

import java.awt.Rectangle;
import java.util.Arrays;

public class BrailleBox extends Rectangle
{
	/**
	 * BrailleInfoの番号
	 */
	private int brailleInfoIndex;
	/**
	 * 打つ点の位置
	 */
	private int[] dots;

	private boolean lineBreak = false;

	public BrailleBox()
	{
	}

	public BrailleBox(BrailleBox src)
	{
		super(src);
		this.brailleInfoIndex = src.brailleInfoIndex;
	}

	public BrailleBox(int x, int y, int w, int h, int brailleInfoIndex, int[] dots)
	{
		super(x, y, w, h);
		this.setBrailleInfoIndex(brailleInfoIndex);
		this.setDots(dots);
	}

	public boolean isLineBreak()
	{
		return lineBreak;
	}

	public static BrailleBox getLineBreak(int x, int y, int w, int h, int brailleIndex)
	{
		BrailleBox box = new BrailleBox(x, y, 0, h, brailleIndex, null);
		box.lineBreak = true;

		return box;
	}

	public void setBrailleInfoIndex(int index)
	{
		this.brailleInfoIndex = index;
	}

	public int getBrailleInfoIndex()
	{
		return this.brailleInfoIndex;
	}

	public int[] getDots()
	{
		return dots;
	}

	public void setDots(int[] dots)
	{
		this.dots = (null == dots ? null: Arrays.copyOf(dots, dots.length));
	}

}
