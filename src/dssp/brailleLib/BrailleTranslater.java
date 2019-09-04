package dssp.brailleLib;

import java.util.List;

/**
 * 墨字を点字に翻訳する<br/>
 *
 * @author DSSP/Minoru Yagi
 *
 */
public final class BrailleTranslater
{
	private final BrailleDict dict;
	private final List<BrailleInfo> gaijiQueue = Util.newArrayList();
	private final List<BrailleInfo> suufuQueue = Util.newArrayList();
	private List<BrailleInfo> codeList = null;

	/**
	 * 翻訳モード
	 *
	 * @author yagi
	 *
	 */
	public static enum MODE
	{
		/**
		 * テキスト(デフォルト)
		 */
		TEXT,
		/**
		 * 数式
		 */
		FORMULA;
	}
	private MODE mode = MODE.TEXT;

	private BrailleTranslater()
	{
		this.dict = null;
	}

	private BrailleTranslater(BrailleDict dict)
	{
		this.dict = dict;
	}

	/**
	 * インスタンスを生成する
	 *
	 * @param dict 辞書
	 * @return インスタンス
	 */
	public static BrailleTranslater newInstance(BrailleDict dict)
	{
		BrailleTranslater obj = new BrailleTranslater(dict);

		return obj;
	}

	/**
	 * 生成で渡された辞書を取得する
	 *
	 * @return 辞書
	 */
	public BrailleDict getDict()
	{
		return this.dict;
	}

	/**
	 * 翻訳モードを設定する
	 *
	 * @param mode MODE
	 */
	public void setMode(MODE mode)
	{
		this.mode = mode;
	}

	/**
	 * 翻訳モードを取得する
	 *
	 * @return 現在のMODE
	 */
	public MODE getMode()
	{
		return this.mode;
	}

	/**
	 * 墨字のテキストを点字に翻訳する<br/>
	 * ・辞書に点字が見つからない場合は、空の点字にする
	 *
	 * @param text 墨字のテキスト
	 * @param codeList 点字のリスト
	 * @param igonreLineBreak true=改行を無視する
	 * @param includeExtra true=点字リストに符号を含める
	 * @return 点字にした墨字の数
	 * @throws IllegalArgumentException codeListがnullの場合
	 */
	public int braileFromSumiji(String text, List<BrailleInfo> codeList, boolean ignoreLineBreak, boolean includeExtra)
	{
		if (null == codeList)
		{
			throw new IllegalArgumentException("codeListがnull");
		}
		if (0 == text.length())
		{
			return 0;
		}

		this.codeList = codeList;

		this.codeList.clear();

		int nBraille = 0;

		// 辞書の最大文字数から始めて、部分文字列を最長一致検索
		int start = 0;
		while (true)
		{
			BrailleInfo info = null;
			int end = Math.min(start + this.dict.getMaxSumijiLen(), text.length());
			String sumiji = null;
			while (end > start)
			{
				// 部分文字列
				sumiji = text.substring(start, end);
				if (null == sumiji)
				{
					break;
				}
				else if (sumiji.equals("\n\r") || sumiji.equals("\r\n"))
				{
					info = BrailleInfo.LINEBREAK;
					break;
				}
				else if (sumiji.equals("\n") || sumiji.equals("\r"))
				{
					info = BrailleInfo.LINEBREAK;
					break;
				}
				else
				{
					info = this.dict.getBrailleInfo(sumiji);
					if (false == info.isEmpty())
					{
						break;
					}
					if (info.isPostChar())
					{
						break;
					}
					// 部分文字列を1文字ずつ短くする
					end--;
				}
			}
			if (null == info || (false == info.isLineBreak() && false == info.isPostChar() && info.isEmpty()))
			{
				if (null != sumiji)
				{
					if (sumiji.equals(" ") || sumiji.equals("　"))
					{
						info = BrailleInfo.SPACE;
					}
					else
					{
						Util.logInfo("「%s」の点字が見つかりません", sumiji);
						info = BrailleInfo.UNKNOWN;
					}
				}
				start++;
			}
			else
			{
				start = end;
			}
			nBraille++;
			if (null != info)
			{
				if (includeExtra && info.haveExtra(BrailleInfo.EXTRA.GAIJIFU))
				{
					this.checkSuufu();
					this.gaijiQueue.add(info);
				}
				else if(includeExtra && info.haveExtra(BrailleInfo.EXTRA.SUUFU))
				{
					this.checkGaijifu();
					this.suufuQueue.add(info);
				}
				else
				{
					this.checkGaijifu();
					this.checkSuufu();
					if (info != BrailleInfo.LINEBREAK || false == ignoreLineBreak)
					{
						this.addBraille(info);
					}
				}
//				// 改行の処理
//				if (info != BrailleInfo.LINEBREAK || false == ignoreLineBreak)
//				{
//					this.addBraille(info);
//				}
			}
			if (end == text.length())
			{
				break;
			}
		}
		this.checkGaijifu();
		this.checkSuufu();

		return nBraille;
	}

	/**
	 * 点字列を墨字に翻訳する<br/>
	 * ・辞書に未登録の点字があればBrailleInfo.UNKNOWNにする
	 *
	 * @param boxList
	 * @return
	 */
	public List<BrailleInfo> sumijiFromBraille(List<BrailleBox> boxList)
	{
		List<BrailleInfo> list = Util.newArrayList();

		int start = 0;
		int count = 0;
		int nBox = boxList.size();
		for (int i = 0; i < nBox; i++)
		{
			BrailleBox box = boxList.get(i);
			if (box.isLineBreak())
			{
				count = i - start;
				if (0 < count)
				{
					List<BrailleBox> line = boxList.subList(start, start + count);
					list.addAll(this.sumijiLineFromBraille(line));
				}
				list.add(BrailleInfo.LINEBREAK);
				start = i+1;
			}
		}
		if (start < nBox)
		{
			count = nBox - start;
			if (0 == count)
			{
				list.add(BrailleInfo.LINEBREAK);
			}
			else
			{
				List<BrailleBox> line = boxList.subList(start, start + count);
				list.addAll(this.sumijiLineFromBraille(line));
			}
		}

		return list;
	}

	private List<BrailleInfo> sumijiLineFromBraille(List<BrailleBox> boxList)
	{
		List<BrailleInfo> list = Util.newArrayList();

		int start = 0;
		while (true)
		{
			int index = boxList.get(start).getBrailleInfoIndex();
			int count = 1;
			for (int i = (start+1); i < boxList.size(); i++)
			{
				BrailleBox box = boxList.get(i);
				if (box.getBrailleInfoIndex() != index)
				{
					break;
				}
				count++;
			}
			count = Math.min(this.dict.getMaxBoxCount(), count);

			while (true)
			{
				List<BrailleBox> seq = boxList.subList(start, start + count);
				BrailleInfo info = this.dict.getBraille(seq);
				if (null == info)
				{
					count--;
					if (0 == count)
					{
						Util.logInfo("UNKNOWN");
						list.add(BrailleInfo.UNKNOWN);
						start++;
						break;
					}
				}
				else
				{
					list.add(info);
					start += count;
					break;
				}
			}
			if (start >= boxList.size())
			{
				break;
			}
		}

		return list;
	}

	/**
	 * BrailleInfoのリストからテキストを取得する
	 *
	 * @param brailleList
	 * @return
	 */
	public String getTextFromSumiji(List<BrailleInfo> brailleList)
	{
		StringBuilder b = new StringBuilder();
		for (BrailleInfo info: brailleList)
		{
			if (BrailleInfo.TYPE.VISIBLE == info.getType())
			{
				b.append(info.getSumiji());
			}
		}

		return b.toString();
	}

	/**
	 * NABCC列からBrailleInfoのリストを取得する
	 *
	 * @param nabcc
	 * @return
	 */
	public List<BrailleInfo> getBrailleFromNABCC(String nabcc)
	{
		List<BrailleInfo> list = Util.newArrayList();
		for (int start = 0; start < nabcc.length(); )
		{
			int end = Math.min(nabcc.length(), start + this.dict.getMaxNABCCLen());

			while (end > start)
			{
				String t = nabcc.substring(start, end);
				if (t.startsWith(" "))
				{
					list.add(BrailleInfo.SPACE);
					start++;
					break;
				}

				BrailleInfo info = this.dict.getBraille(t);
				if (null != info)
				{
					list.add(info);
					start = end;
					break;
				}
				end--;
				if (end == start)
				{
					list.add(BrailleInfo.UNKNOWN);
					start++;
					break;
				}
			}
		}

		return list;
	}

	private void checkGaijifu()
	{
		int nExtra = Math.min(2, this.gaijiQueue.size());
		if (0 < nExtra)
		{
			BrailleInfo gaijiFlag = this.dict.getExtra(BrailleInfo.EXTRA.GAIJIFU);
			switch(this.mode)
			{
			case TEXT:
				for (int j = 0; j < nExtra; j++)
				{
					this.codeList.add(gaijiFlag);
				}
				break;
			case FORMULA:
//				if (0 < this.codeList.size())
//				{
//					BrailleInfo prev = this.codeList.get(this.codeList.size()-1);
//					if (false == prev.haveExtra(BrailleInfo.EXTRA.GAIJIFU))
//					{
//						this.codeList.add(gaijiFlag);
//					}
//				}
			}

			// 大文字符の処理
			BrailleInfo oomojiFlag = this.dict.getExtra(BrailleInfo.EXTRA.OOMOJIFU);
			int oomojiIndex = 0;
			int nOomoji = 0;
			for (int i = (this.gaijiQueue.size()-1); i >= 0; i--)
			{
				BrailleInfo info = this.gaijiQueue.get(i);
				if (info.haveExtra(BrailleInfo.EXTRA.OOMOJIFU))
				{
					if (0 == nOomoji)
					{
						oomojiIndex = i;
					}
					nOomoji = Math.min(2, nOomoji+1);
				}
				else
				{
					if (2 == nOomoji && oomojiIndex < (this.gaijiQueue.size() - 1))
					{
						this.gaijiQueue.add(oomojiIndex + 1, gaijiFlag);
					}
					oomojiIndex = 0;
					for (int j = 0; j < nOomoji; j++)
					{
						this.gaijiQueue.add(i+1, oomojiFlag);
					}
					nOomoji = 0;
				}
			}
			if (2 == nOomoji && oomojiIndex < (this.gaijiQueue.size() - 1))
			{
				this.gaijiQueue.add(oomojiIndex + 1, gaijiFlag);
			}
			for (int j = 0; j < nOomoji; j++)
			{
				this.gaijiQueue.add(0, oomojiFlag);
			}

			this.addBraile(this.gaijiQueue);
			this.gaijiQueue.clear();
		}

	}

	private void checkSuufu()
	{
		if (0 < this.suufuQueue.size())
		{
			BrailleInfo extra = this.dict.getExtra(BrailleInfo.EXTRA.SUUFU);
			this.codeList.add(extra);
			this.addBraile(this.suufuQueue);
			this.suufuQueue.clear();
		}
	}

	private void addBraile(List<BrailleInfo> list)
	{
		for (BrailleInfo info: list)
		{
			this.addBraille(info);
		}
	}

	private void addBraille(BrailleInfo info)
	{
		this.codeList.add(info);
	}
}
