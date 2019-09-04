package dssp.brailleLib;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

import dssp.brailleLib.BrailleBox;
import dssp.brailleLib.BrailleInfo;
import dssp.brailleLib.BrailleRenderer;
import dssp.brailleLib.Util;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class BrailleEditPanel extends JPanel
{
	private BrailleRenderer renderer;

	// 編集中のリスト
	private List<BrailleInfo> brailleList = Util.newArrayList();

	private List<BrailleBox> boxList = Util.newArrayList();

	private List<Integer> selBoxes = Util.newArrayList();

	private Point base = new Point(10, 10);

	private int caret = 0;

	private Image bufImage;

	private boolean showCursor = true;
	private int sw;
	private int bh;
	private Timer caretTimer;
	private Timer keyTimer;
	private int flickInterval = 500;

	private Color cColor = Color.GREEN;
	private Color bColor = Color.RED;

//	private int[] keyList = {KeyEvent.VK_D, KeyEvent.VK_F, KeyEvent.VK_G, KeyEvent.VK_J, KeyEvent.VK_K, KeyEvent.VK_L};
//	private List<Integer> dotList = Util.newArrayList();
	private boolean[] dotState = {false, false, false, false, false, false};

	private static final int WAIT_TIME = 300;

	private boolean adding = false;

	/**
	 * Create the panel.
	 */
	public BrailleEditPanel()
	{
		setBackground(Color.WHITE);
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				int keyCode = arg0.getKeyCode();
				switch(keyCode)
				{
				case KeyEvent.VK_RIGHT:
					moveRightCaret();
					break;
				case KeyEvent.VK_LEFT:
					moveLeftCaret();
					break;
				case KeyEvent.VK_UP:
					moveUpCaret();
					break;
				case KeyEvent.VK_DOWN:
					moveDownCaret();
					break;
				case KeyEvent.VK_D:
					dotState[0] = true;
					if (null == keyTimer)
					{
						waitKey();
					}
					break;
				case KeyEvent.VK_F:
					dotState[1] = true;
					if (null == keyTimer)
					{
						waitKey();
					}
					break;
				case KeyEvent.VK_G:
					dotState[2] = true;
					if (null == keyTimer)
					{
						waitKey();
					}
					break;
				case KeyEvent.VK_J:
					dotState[3] = true;
					if (null == keyTimer)
					{
						waitKey();
					}
					break;
				case KeyEvent.VK_K:
					dotState[4] = true;
					if (null == keyTimer)
					{
						waitKey();
					}
					break;
				case KeyEvent.VK_L:
					dotState[5] = true;
					if (null == keyTimer)
					{
						waitKey();
					}
					break;
				case KeyEvent.VK_SPACE:
					if (null != keyTimer)
					{
						addBox();
					}
					addBox();
					break;
				case KeyEvent.VK_ENTER:
					inputEnter();
					break;
				case KeyEvent.VK_BACK_SPACE:
					removePrevBox();
					break;
				case KeyEvent.VK_DELETE:
					removeBox();
					break;
				}
			}

//			@Override
//			public void keyReleased(KeyEvent arg0) {
//				int keyCode = arg0.getKeyCode();
//				for (int i = 0; i < keyList.length; i++)
//				{
//					if (keyCode == keyList[i])
//					{
//						if (false == dotList.contains(i))
//						{
//							dotList.add(i);
//						}
//					}
//				}
//			}
		});
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				requestFocus();
				setCaret(arg0.getX(), arg0.getY());
			}
		});
		addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent arg0) {
				caretTimer = new Timer();
				caretTimer.schedule(new TimerTask()
				{
					@Override
					public void run()
					{
						flickCursor(null);
					}
				}
				, 0, flickInterval);
			}
			@Override
			public void focusLost(FocusEvent e) {
				caretTimer.cancel();
				caretTimer = null;
				flickCursor(true);
				fixBox();
			}
		});

	}

	public void setRenderer(BrailleRenderer renderer)
	{
		this.renderer = renderer;

		this.renderer.setBgColor(Color.WHITE);
		this.renderer.setFgColor(Color.BLACK);

		this.renderer.setMode(BrailleRenderer.MODE.DISPLAY);

		this.bh = this.renderer.getBoxHeight() + this.renderer.getDotSize();
		this.sw = this.renderer.getBoxSpace() - this.renderer.getDotSize();
	}

	private void waitKey()
	{
		this.keyTimer = new Timer();
		this.keyTimer.schedule(new TimerTask()
		{

			@Override
			public void run()
			{
				addBox();
			}
		}, WAIT_TIME);
	}

	private void flickCursor(Boolean flag)
	{
		if (null != flag)
		{
			this.showCursor = flag;
		}
		else
		{
			this.showCursor = !this.showCursor;
		}
		this.repaint();
	}

	public void addBrailleEditListner(BrailleEditListener listener)
	{
		this.listenerList.add(BrailleEditListener.class, listener);
	}

	private void addingBox(List<BrailleBox> boxList)
	{
		for (BrailleEditListener listener: this.listenerList.getListeners(BrailleEditListener.class))
		{
			listener.addingBox(boxList);
		}
	}

	private void addedBox(List<BrailleBox> boxList)
	{
		for (BrailleEditListener listener: this.listenerList.getListeners(BrailleEditListener.class))
		{
			listener.addedBox(boxList);
		}
	}

	private void deletedBox(List<BrailleBox> boxList)
	{
		this.makeBufImage(false);
		this.repaint();
		this.checkAdding();

		for (BrailleEditListener listener: this.listenerList.getListeners(BrailleEditListener.class))
		{
			listener.deletedBox(boxList);
		}
	}

	public void setBraille(BrailleInfo info)
	{
		this.brailleList.clear();
		this.brailleList.add(info);

		this.makeBufImage(true);
		this.repaint();
	}

	public BrailleInfo getBraille(int index)
	{
		if (null == this.brailleList || index >= this.brailleList.size())
		{
			return null;
		}

		return this.brailleList.get(index);
	}

	public List<BrailleInfo> getBrailleList()
	{
		return brailleList;
	}

	public void setBrailleList(List<BrailleInfo> brailleList)
	{
		this.brailleList = brailleList;

		this.makeBufImage(true);
		this.repaint();
	}

	public BrailleRenderer getRenderer()
	{
		return renderer;
	}

	public void setcColor(Color cColor)
	{
		this.cColor = cColor;
	}

	public void setbColor(Color bColor)
	{
		this.bColor = bColor;
	}

	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (null == this.bufImage)
		{
			this.makeBufImage(true);
		}
		drawBraille(g);

		if (this.hasFocus())
		{
			Dimension size = this.getSize();
			g.setColor(Color.BLACK);
			g.drawRect(0, 0, size.width-1, size.height-1);
		}
	}

	public void clear()
	{
		this.brailleList = null;
		this.boxList = null;
		this.bufImage = null;
	}

	private void drawBraille(Graphics g)
	{
		g.setColor(this.getBackground());
		g.fillRect(0, 0, this.getWidth(), this.getHeight());

		if (null == this.bufImage)
		{
			this.drawCaret(g);
			return;
		}

		g.drawImage(this.bufImage, base.x, base.y, this.getBackground(), this);

		// 選択枠を表示する
		for (int index: this.selBoxes)
		{
			BrailleBox rect = boxList.get(index);
			rect.width += this.renderer.getDotSize();
			rect.height += this.renderer.getDotSize();
			g.setColor(this.bColor);
			g.drawRect(rect.x, rect.y, rect.width-1, rect.height-1);
		}

		// キャレットを表示する
		this.drawCaret(g);
	}

	/**
	 * 点字のイメージを作成する
	 */
	private void makeBufImage(boolean fromInfo)
	{
		if (false == this.isVisible() || null == this.renderer)
		{
			return;
		}

		if (fromInfo)
		{
//			if (null == this.brailleList || 0 == this.brailleList.size())
//			{
//				return;
//			}
			if (null == this.brailleList)
			{
				return;
			}
			// マスを取得する
			this.boxList = this.renderer.drawBraille(null, this.brailleList, 0, 0, true);
		}

		// 点字を描く領域の大きさを計算する
		Rectangle bound = null;
		for (BrailleBox box: this.boxList)
		{
			int w = box.width + this.renderer.getDotSize();
			int h = box.height + this.renderer.getDotSize();
			Rectangle b = new Rectangle(box.x, box.y, w, h);
			if (null == bound)
			{
				bound = b;
			}
			else
			{
				bound.add(b);
			}
		}

		// 今のイメージが小さいなら作りなおす
		int w = 0;
		int h = 0;
		if (null == this.bufImage)
		{
			if (null == bound)
			{
				return;
			}

			w = bound.width;
			h = bound.height;

			this.bufImage = this.createVolatileImage(bound.width, bound.height);
			if (null == this.bufImage)
			{
				Util.logError("Failed to create image buffer");
				return;
			}

			Dimension size = this.getSize();
			if (size.width < (bound.x + w))
			{
				size.width = bound.x + w;
			}
			if (size.height < (bound.y + h))
			{
				size.height = bound.y + h;
			}
			this.setPreferredSize(size);
			this.setSize(size);
		}
		else
		{
			w = this.bufImage.getWidth(this);
			h = this.bufImage.getHeight(this);
			if (null != bound)
			{
				if (bound.width > w || bound.height > h)
				{
					this.bufImage = this.createVolatileImage(bound.width, bound.height);
					if (null == this.bufImage)
					{
						Util.logError("Failed to create image buffer");
						return;
					}

					w = bound.width;
					h = bound.height;

					Dimension size = this.getSize();
					if (size.width < (bound.x + w))
					{
						size.width = bound.x + w;
					}
					if (size.height < (bound.y + h))
					{
						size.height = bound.y + h;
					}
					this.setPreferredSize(size);
					this.setSize(size);
				}
			}
		}
		Graphics g = this.bufImage.getGraphics();
		g.setColor(this.getBackground());
		g.fillRect(0, 0, w, h);

		for (BrailleBox box: this.boxList)
		{
			int index = box.getBrailleInfoIndex();
			if (0 <= index)
			{
				BrailleInfo info = this.brailleList.get(box.getBrailleInfoIndex());
				if (info.isLineBreak())
				{
				}
				else if (BrailleInfo.UNKNOWN == info)
				{
					this.renderer.drawUnknown(g, box);
				}
				else
				{
					this.renderer.drawDots(g, box.getDots(), box, this.getForeground());
				}
			}
			else
			{
				this.renderer.drawDots(g, box.getDots(), box, Color.RED);
			}
		}
//		List<Rectangle> tBoxList = this.renderer.drawBraille(g, this.brailleList, 0, 0);
	}

	/**
	 * キャレットを描画する
	 *
	 * @param g Graphics
	 */
	private void drawCaret(Graphics g)
	{
		if (this.showCursor)
		{
			int cx;
			int cy;
			if (null == this.bufImage)
			{
				g.setColor(this.cColor);
				cx = this.base.x - this.sw/2;
				cy = this.base.y;
			}
			else
			{
				BrailleBox cbox;
				if (this.caret < this.boxList.size())
				{
					cbox = this.boxList.get(this.caret);
					if (cbox.isLineBreak())
					{
						cx = this.base.x + cbox.x - this.sw/2;
						cy = this.base.y + cbox.y;
					}
					else
					{
						cx = this.base.x + cbox.x - this.sw/2;
						cy = this.base.y + cbox.y;
					}
				}
				else
				{
					this.caret = this.boxList.size();
					cx = this.base.x;
					cy = this.base.y;
					if (0 < this.caret)
					{
						cbox = this.boxList.get(this.caret-1);
						if (cbox.isLineBreak())
						{
							cy += cbox.y + cbox.height + this.renderer.getLineSpace();
						}
						else
						{
							cx += cbox.x + cbox.width + this.renderer.getDotSize() + this.sw/2;
							cy += cbox.y;
						}
					}
				}
			}

			g.setColor(this.cColor);
			g.drawLine(cx, cy, cx, cy + this.bh);
		}
	}

	/**
	 * カーソル位置からキャレットの位置を設定する
	 *
	 * @param x
	 * @param y
	 */
	private void setCaret(int x, int y)
	{
		if (null == this.boxList)
		{
			return;
		}

		Rectangle area = null;
		for (int i = 0; i < this.boxList.size(); i++)
		{
			BrailleBox box = this.boxList.get(i);
			if (null == area)
			{
				area = new Rectangle(box.x + base.x, box.y + base.y + this.renderer.getDotSize(), box.width, box.height + this.renderer.getDotSize());
			}
			else
			{
				area.x = box.x + base.x;
				area.y = box.y + base.y;
			}
			if (area.contains(x, y))
			{
				this.caret = i;
				break;
			}
		}
	}

	private void checkAdding()
	{
		if (false == this.adding)
		{
			return;
		}

		for (int i = 0; i < this.boxList.size(); i++)
		{
			if (this.boxList.get(i).getBrailleInfoIndex() < 0)
			{
				return;
			}
		}
		this.adding = false;
	}

	/**
	 * キャレットを右に動かす
	 */
	private void moveRightCaret()
	{
		if (this.caret >= this.boxList.size())
		{
			return;
		}

		if (this.adding)
		{
			int index = this.boxList.get(this.caret).getBrailleInfoIndex();
			if (0 <= index)
			{
				return;
			}
			this.caret++;
		}
		else
		{
			BrailleBox box = this.boxList.get(this.caret);
			int c = box.getBrailleInfoIndex();
			for (int i = (this.caret+1); i <= this.boxList.size(); i++)
			{
				if (i < this.boxList.size())
				{
					int n = this.boxList.get(i).getBrailleInfoIndex();
					if (n != c)
					{
						this.caret = i;
						break;
					}
				}
				else
				{
					this.caret = i;
				}
			}
		}
		this.repaint();
	}

	/**
	 * キャレットを左に動かす
	 */
	private void moveLeftCaret()
	{
		if (0 >= this.caret)
		{
			return;
		}

		if (this.adding)
		{
			int prev = this.boxList.get(this.caret-1).getBrailleInfoIndex();
			if (0 <= prev)
			{
				return;
			}
			this.caret--;
		}
		else
		{
			BrailleBox box = this.boxList.get(this.caret-1);
			int c = box.getBrailleInfoIndex();
			int next = 0;
			for (int i = (this.caret-1); i >=0 ; i--)
			{
				int n = this.boxList.get(i).getBrailleInfoIndex();
				if (n != c)
				{
					next = i+1;
					break;
				}
			}
			this.caret = next;
		}
		this.repaint();
	}

	/**
	 * キャレットを上に動かす
	 */
	private void moveUpCaret()
	{
		if (this.adding || 0 >= this.caret)
		{
			return;
		}

		BrailleBox box;
		if (this.caret < this.boxList.size())
		{
			box = this.boxList.get(this.caret);
		}
		else
		{
			box = this.boxList.get(this.boxList.size()-1);
		}
		// マスに対応するBrailleInfoの番号を見つける
		int index = box.getBrailleInfoIndex();
		// 上の行の改行を求める
		int last = 0;
		for (int i = index; i >= 0; i--)
		{
			BrailleInfo t = this.brailleList.get(i);
			if (t.isLineBreak())
			{
				last = i-1;
				break;
			}
		}
		if (0 == last)	// 上には行がない
		{
			return;
		}
		// 上の行のBrailleInfoを数える
		int count = 0;
		for (int i = last; i >= 0; i--)
		{
			BrailleInfo t = this.brailleList.get(i);
			if (t.isLineBreak())
			{
				break;
			}
			count++;
		}
		// 真上のマスか、最初のマスに移る
		index -= count+1;
		for (int i = (this.caret-1); i >= 0; i--)
		{
			BrailleBox b = this.boxList.get(i);
			if (index == b.getBrailleInfoIndex())
			{
				this.caret = i;
			}
		}
		this.repaint();
	}

	/**
	 * キャレットを下に動かす
	 */
	private void moveDownCaret()
	{
		if (this.adding || this.caret >= this.boxList.size())
		{
			return;
		}

		BrailleBox box;
		if (this.caret < this.boxList.size())
		{
			box = this.boxList.get(this.caret);
		}
		else
		{
			box = this.boxList.get(this.boxList.size()-1);
		}
		// マスに対応するBrailleInfoの番号を見つける
		int index = box.getBrailleInfoIndex();
		// キャレットの行の最初のBrailleInfoを見つける
		int first = 0;
		for (int i = index; i >= 0; i--)
		{
			BrailleInfo t = this.brailleList.get(i);
			if (t.isLineBreak())
			{
				first = i+1;
				break;
			}
		}
		// キャレットの行のBrailleInfoの数を求める
		int count = 0;
		for (int i = first; i < this.brailleList.size(); i++)
		{
			BrailleInfo t = this.brailleList.get(i);
			if (t.isLineBreak())
			{
				break;
			}
			count++;
		}
		if (0 == count)	// 下には行がない
		{
			return;
		}
		// 真下のマスか、最後のマスに移動する
		index += count+1;
		for (int i = this.caret; i <= this.boxList.size(); i++)
		{
			if (i < this.boxList.size())
			{
				BrailleBox b = this.boxList.get(i);
				if (index == b.getBrailleInfoIndex())
				{
					this.caret = i;
					break;
				}
			}
			else
			{
				this.caret = i;
			}
		}
		this.repaint();
	}

	/**
	 * マスを追加する
	 */
	private void addBox()
	{
		if (null != keyTimer)
		{
			keyTimer.cancel();
			keyTimer = null;
		}
		if (false == this.adding)
		{
			this.adding = true;
		}

		// 新しいマスを作る
		int[] dots = new int[dotState.length];
		Arrays.fill(dots, 0);
		int count = 0;
		for (int i = 0; i < dotState.length; i++)
		{
			if (dotState[i])
			{
				dots[count] = i+1;
				count++;
			}
			dotState[i] = false;
		}
		if (null == this.boxList)
		{
			this.boxList = Util.newArrayList();
		}

		BrailleBox box;
		if (this.caret < this.boxList.size())
		{
			BrailleBox src = this.boxList.get(this.caret);
			box = new BrailleBox(src.x, src.y, this.renderer.getBoxWidth(), this.renderer.getBoxHeight(), -1, Arrays.copyOf(dots, count));
		}
		else if (0 == this.caret)
		{
			box = new BrailleBox(0, 0, this.renderer.getBoxWidth(), this.renderer.getBoxHeight(), -1, Arrays.copyOf(dots, count));
		}
		else
		{
			BrailleBox src = this.boxList.get(this.caret-1);
			if (src.isLineBreak())
			{
				box = new BrailleBox(0, src.y + this.renderer.getBoxHeight() + this.renderer.getLineSpace(), this.renderer.getBoxWidth(), this.renderer.getBoxHeight(), -1, Arrays.copyOf(dots, count));
			}
			else
			{
				box = new BrailleBox(src.x + this.renderer.getBoxWidth() + this.renderer.getBoxSpace(), src.y, this.renderer.getBoxWidth(), this.renderer.getBoxHeight(), -1, Arrays.copyOf(dots, count));
			}
		}

		// マスを追加する
		this.boxList.add(this.caret, box);

		// キャレットを移動する
		this.caret++;

		// 行末までのマスをずらす
		for (int i = this.caret; i < this.boxList.size(); i++)
		{
			BrailleBox b = this.boxList.get(i);
			if (b.isLineBreak())
			{
				b.x += this.renderer.getBoxWidth() + this.renderer.getBoxSpace();
				break;
			}
			b.x += this.renderer.getBoxWidth() + this.renderer.getBoxSpace();
		}

		this.makeBufImage(false);
		this.repaint();

		this.addingBox(this.boxList);
	}

	private void inputEnter()
	{
		if (adding)
		{
			fixBox();
		}
		else
		{
			BrailleBox lb;
			if (this.caret > 0)
			{
				BrailleBox box = this.boxList.get(this.caret-1);
				lb = BrailleBox.getLineBreak(box.x + this.renderer.getBoxWidth() + this.renderer.getBoxSpace(), box.y, 0, this.renderer.getBoxHeight(), -1);
				this.boxList.add(this.caret, lb);
			}
			else
			{
				lb = BrailleBox.getLineBreak(0, 0, 0, this.renderer.getBoxHeight(), -1);
				this.boxList.add(this.caret, lb);
			}
			this.caret++;

			BrailleBox prev = lb;
			for (int i = (this.caret+1); i < this.boxList.size(); i++)
			{
				BrailleBox b = this.boxList.get(i);
				if (prev.isLineBreak())
				{
					b.x = 0;
					b.y = prev.y + this.renderer.getBoxHeight() + this.renderer.getLineSpace();
				}
				else
				{
					b.x = prev.x + this.renderer.getBoxWidth() + this.renderer.getBoxSpace();
					b.y = prev.y;
				}
				prev = b;
			}

			this.makeBufImage(false);
			this.addedBox(this.boxList);
		}
	}

	private void fixBox()
	{
		if (null != keyTimer)
		{
			keyTimer.cancel();
			keyTimer = null;
		}
		if (this.adding)
		{
			this.adding = false;
			this.addedBox(this.boxList);
		}
	}

	/**
	 * DELキーの処理
	 */
	private void removeBox()
	{
		if (this.caret >= this.boxList.size())
		{
			return;
		}

		if (this.adding)
		{
			// 右隣のマスを消す
			BrailleBox box = this.boxList.get(this.caret);
			int index = box.getBrailleInfoIndex();
			if (0 <= index)
			{
				return;
			}
			this.boxList.remove(this.caret);

			// 行末までの右側のマスを移動する
			BrailleBox prev = box;
			box = this.boxList.get(this.caret);
			box.x = prev.x;

			prev = box;
			for (int i = (this.caret+1); i < this.boxList.size(); i++)
			{
				box = this.boxList.get(i);
				if (prev.isLineBreak())
				{
					break;
				}
				box.x = prev.x + this.renderer.getBoxWidth() + this.renderer.getBoxSpace();;
				prev = box;
			}

			this.checkAdding();
			if (this.adding)
			{
				this.makeBufImage(false);
				this.addingBox(this.boxList);
			}
			else
			{
				this.addedBox(this.boxList);
			}
		}
		else
		{
			// 消すマスの番号を決める　条件:キャレットの右側で、同じBrailleInfo番号のマス
			int end = this.caret;
			BrailleBox box = this.boxList.get(this.caret);
			int index = box.getBrailleInfoIndex();
			for (int i = (this.caret+1); i < this.boxList.size(); i++)
			{
				int next = this.boxList.get(i).getBrailleInfoIndex();
				if (next != index)
				{
					end = i-1;
					break;
				}
			}

			// マスを消す
			for (int i = end; i >= this.caret; i--)
			{
				this.boxList.remove(i);
			}

			// 次以後のマスを移動する
			if (this.caret < this.boxList.size())
			{
				BrailleBox prev = box;
				for (int i = this.caret; i < this.boxList.size(); i++)
				{
					box = this.boxList.get(i);
					if (prev.isLineBreak())
					{
						box.x = 0;
						box.y = prev.y + this.renderer.getBoxHeight() + this.renderer.getLineSpace();
					}
					else
					{
						box.x = prev.x + this.renderer.getBoxWidth() + this.renderer.getBoxSpace();
						box.y = prev.y;
					}
					prev = box;
				}
			}
			this.deletedBox(this.boxList);
		}
	}

	/**
	 * BACKSPACEキーの処理
	 */
	private void removePrevBox()
	{
		if (0 >= this.caret)
		{
			return;
		}

		if (this.adding)
		{
			// 左隣のマスを消す
			BrailleBox box = this.boxList.get(this.caret-1);
			int index = box.getBrailleInfoIndex();
			if (0 <= index)
			{
				return;
			}
			this.caret--;
			this.boxList.remove(this.caret);

			if (this.caret >= this.boxList.size())
			{
				return;
			}

			// 行末までの右側のマスを移動する
			BrailleBox prev;
			if (this.caret > 0)
			{
				prev = this.boxList.get(this.caret-1);
				box = this.boxList.get(this.caret);
				box.x = prev.x + this.renderer.getBoxWidth() + this.renderer.getBoxSpace();
			}
			else
			{
				box = this.boxList.get(this.caret);
				box.x = 0;
			}
			prev = box;
			for (int i = (this.caret+1); i < this.boxList.size(); i++)
			{
				box = this.boxList.get(i);
				if (prev.isLineBreak())
				{
					break;
				}
				box.x = prev.x + this.renderer.getBoxWidth() + this.renderer.getBoxSpace();
				prev = box;
			}

			this.checkAdding();
			if (this.adding)
			{
				this.makeBufImage(false);
				this.addingBox(this.boxList);
			}
			else
			{
				this.addedBox(this.boxList);
			}
		}
		else
		{
			// 消すマスの番号を決める　条件:キャレットの左側で、同じBrailleInfo番号のマス
			int start = this.caret-1;
			BrailleBox box = this.boxList.get(this.caret-1);
			int index = box.getBrailleInfoIndex();
			for (int i = (this.caret-1); i >= 0; i--)
			{
				int next = this.boxList.get(i).getBrailleInfoIndex();
				if (next != index)
				{
					start = i+1;
					break;
				}
			}

			// マスを消す
			for (int i = (this.caret-1); i >= start; i--)
			{
				this.boxList.remove(i);
			}

			this.caret = start;
			if (this.caret >= this.boxList.size())
			{
				this.deletedBox(this.boxList);
				return;
			}

			// 次以後のマスを移動する
			if (0 < this.caret)
			{
				box = this.boxList.get(this.caret-1);
			}
			else
			{
				box = new BrailleBox(0, 0, this.renderer.getBoxWidth(), this.renderer.getBoxHeight(), -1, null);
			}
			BrailleBox prev = box;
			for (int i = this.caret; i < this.boxList.size(); i++)
			{
				box = this.boxList.get(i);
				if (prev.isLineBreak())
				{
					box.x = 0;
					box.y = prev.y + this.renderer.getBoxHeight() + this.renderer.getLineSpace();
				}
				else
				{
					box.x = prev.x + this.renderer.getBoxWidth() + this.renderer.getBoxSpace();
					box.y = prev.y;
				}
				prev = box;
			}
			this.deletedBox(this.boxList);
		}
	}
}
