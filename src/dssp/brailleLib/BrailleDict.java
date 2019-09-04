package dssp.brailleLib;

import java.util.List;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import dssp.brailleLib.BrailleInfo.TYPE;

/**
 * 墨字と点字の辞書
 *
 * @author DSSP/Minoru Yagi
 *
 */
public final class BrailleDict
{
	private static class BrailleInfoComparator implements Comparator<BrailleInfo>
	{
		@Override
		public int compare(BrailleInfo o1, BrailleInfo o2)
		{
			return o1.getSumiji().compareTo(o2.getSumiji());
		}
	}
	private static BrailleInfoComparator comparator = new BrailleInfoComparator();

	private final Map<BrailleInfo.EXTRA, BrailleInfo> extraMap = Util.newTreeMap();
	private final TreeSet<BrailleInfo> brailleInfoList = Util.newTreeSet(comparator);


	private final static String NAME_DICT = "dict";
	private final static String NAME_EXTRA = "extra";
	private final static String NAME_CHARS = "chars";
	private final static String NAME_CHAR = "char";
	private final static String NAME_SUMIJI = "sumiji";
	private final static String NAME_TYPE = "type";
	private final static String NAME_BRAILE = "braile";
	private final static String NAME_CODE = "code";
	private final static String NAME_INDEX = "index";
	private final static String NAME_NABCC = "nabcc";
	private final static String NAME_POSTCHAR = "postChar";

	private final static String NAME_DESC = "desc";

	private final static EnumMap<BrailleInfo.CHECK, String> checkNameMap = new EnumMap<BrailleInfo.CHECK, String>(BrailleInfo.CHECK.class);
	static
	{
		checkNameMap.put(BrailleInfo.CHECK.PRECHECK, "preCheck");
		checkNameMap.put(BrailleInfo.CHECK.POSTCHECK, "postCheck");
	}

	private final static String PATH_CHARS = String.format("/%s/%s", NAME_DICT, NAME_CHARS);
	private final static String PATH_CHAR = String.format("%s/%s", PATH_CHARS, NAME_CHAR);
	private int maxSumijiLen = 0;
	private int maxNABCCLen = 0;
	private int maxBoxCount = 0;

	private final static EnumMap<BrailleInfo.EXTRA, String> extraNameMap = new EnumMap<BrailleInfo.EXTRA, String>(BrailleInfo.EXTRA.class);
	static
	{
		extraNameMap.put(BrailleInfo.EXTRA.GAIJIFU, "gaijifu");
		extraNameMap.put(BrailleInfo.EXTRA.OOMOJIFU, "oomojifu");
		extraNameMap.put(BrailleInfo.EXTRA.KOMOJIFU, "komojifu");
		extraNameMap.put(BrailleInfo.EXTRA.SUUFU, "suufu");
	};

	private final static EnumMap<BrailleInfo.EXTRA, String> extraPathMap = new EnumMap<BrailleInfo.EXTRA, String>(BrailleInfo.EXTRA.class);
	static
	{
		for (BrailleInfo.EXTRA extra: BrailleInfo.EXTRA.values())
		{
			extraPathMap.put(extra, String.format("/%s/%s/%s", NAME_DICT, NAME_EXTRA, extraNameMap.get(extra)));
		}
	};

	private final static String DOT_SEPARATOR = ",";

	/**
	 * 辞書を読み込む<br/>
	 * ・例外が起きた場合はUtil.logでログ出力する
	 *
	 * @param file 辞書ファイル
	 * @return true=成功 false=失敗
	 */
	public boolean load(File file)
	{
		try
		{
			Util.logInfo("loading %s", file.getPath());
			Document doc = XmlUtil.parse(file);
			this.loadBraille(doc);

			return true;
		}
		catch (Exception ex)
		{
			Util.logException(ex);
			return false;
		}
	}

	private boolean loadExtra(Document doc)
	{
		for (BrailleInfo.EXTRA extra : BrailleInfo.EXTRA.values())
		{
			this.loadExtra(doc, extra);
		}

		return true;
	}

	private boolean loadExtra(Document doc, BrailleInfo.EXTRA extra)
	{
		try
		{
			Element codeNode = (Element)XmlUtil.getNode(doc, BrailleDict.extraPathMap.get(extra));
			if (null == codeNode)
			{
				return false;
			}
			BrailleInfo info = this.newBrailleInfo();
			info.setSumiji(extra.toString());

			info.setType(BrailleInfo.TYPE.ADDITIONAL);

			info.setPostChar(false);

			this.loadCode(codeNode, info);

			String nabcc = codeNode.getAttribute(NAME_NABCC);
			info.setNABCC(nabcc);

			this.setExtra(extra, info);

			return true;
		}
		catch (Exception ex)
		{
			Util.logException(ex);
			return false;
		}
	}

	private BrailleInfo loadCode(Element codeNode, BrailleInfo info)
	{
		for (BrailleInfo.EXTRA extra: BrailleInfo.EXTRA.values())
		{
			String flag = codeNode.getAttribute(BrailleDict.extraNameMap.get(extra));
			if (0 == flag.compareToIgnoreCase("true"))
			{
				info.setExtra(extra, true);
			}
		}

		for (BrailleInfo.CHECK check: BrailleInfo.CHECK.values())
		{
			String flag = codeNode.getAttribute(BrailleDict.checkNameMap.get(check));
			if (0 == flag.compareToIgnoreCase("true"))
			{
				info.setCheck(check, true);
			}
		}

		NodeList codeList = codeNode.getElementsByTagName(NAME_CODE);
		for (int i = 0; i < codeList.getLength(); i++)
		{
			Element dotListNode = (Element)codeList.item(i);
			String dotList = dotListNode.getTextContent();
			StringTokenizer st = new StringTokenizer(dotList, DOT_SEPARATOR);
			int[] dots = new int[st.countTokens()];
			for(int j = 0; st.hasMoreTokens(); j++)
			{
				dots[j] = Integer.parseInt(st.nextToken());
			}
			info.addBox(dots);
		}

		return info;
	}

	private static String SAGARI = "下がり ";

	private boolean loadBraille(Document doc)
	{
		try
		{
			this.loadExtra(doc);

			NodeList nodeList = XmlUtil.getNodeList(doc, PATH_CHAR);
			for (int i = 0; i < nodeList.getLength(); i++)
			{
				Element elm = (Element)nodeList.item(i);
				BrailleInfo info = this.newBrailleInfo();

				String sumiji = elm.getAttribute(NAME_SUMIJI);
				info.setSumiji(sumiji);

				String type = elm.getAttribute(NAME_TYPE);
				if (type.isEmpty())
				{
					info.setType(BrailleInfo.TYPE.VISIBLE);
				}
				else
				{
					info.setType(TYPE.valueOf(type));
				}

				String postChar = elm.getAttribute(NAME_POSTCHAR);
				if (type.isEmpty())
				{
					info.setPostChar(false);;
				}
				else
				{
					info.setPostChar(Boolean.valueOf(postChar));;
				}


				String nabcc = elm.getAttribute(NAME_NABCC);
				info.setNABCC(nabcc);

				String desc = elm.getAttribute(NAME_DESC);
				info.setDesc(desc);

				NodeList codeList = elm.getElementsByTagName(NAME_BRAILE);
				Element codeNode = (Element)codeList.item(0);
				this.loadCode(codeNode, info);

//				this.braileInfoMap.put(elm, info);
				if (false == this.brailleInfoList.add(info))
				{
					Set<BrailleInfo> infos = this.brailleInfoList.subSet(info, true, info, true);
					for (BrailleInfo b: infos)
					{
						this.brailleInfoList.remove(b);
					}
					this.brailleInfoList.add(info);
				}

				this.setMax(info);
			}

			return true;
		}
		catch (Exception ex)
		{
			Util.logException(ex);
			return false;
		}
	}

	private void setMax(BrailleInfo info)
	{
		int len = info.getSumiji().length();
		if (this.maxSumijiLen < len)
		{
			this.maxSumijiLen = len;
		}

		int count = info.getBoxCount();
		if (this.maxBoxCount < count)
		{
			this.maxBoxCount = count;
		}

		len = info.getNABCC(true).length();
		if (this.maxNABCCLen < len)
		{
			this.maxNABCCLen = len;
		}
	}

	/**
	 * 墨字の長さの最大値を取得する
	 *
	 * @return　墨字の長さ
	 */
	public int getMaxSumijiLen()
	{
		return this.maxSumijiLen;
	}

	/**
	 * 点字マスの数の最大値を取得する
	 *
	 * @return マスの数の最大値
	 */
	public int getMaxBoxCount()
	{
		return this.maxBoxCount;
	}

	/**
	 * 符号を含めたNABCCの長さの最大数を取得する
	 *
	 * @return NABCCの長さの最大数
	 */
	public int getMaxNABCCLen()
	{
		return maxNABCCLen;
	}

	private Element createElement(Document doc, Element chars, BrailleInfo info)
	{
		Element elm = XmlUtil.addElement(doc, chars, NAME_CHAR);

		elm.setAttribute(NAME_SUMIJI, info.getSumiji());

		elm.setAttribute(NAME_TYPE, info.getType().name());

		elm.setAttribute(NAME_POSTCHAR, Boolean.toString(info.isPostChar()));

		elm.setAttribute(NAME_NABCC, info.getNABCC(false));

		if (null != info.getDesc() && false == info.getDesc().isEmpty())
		{
			elm.setAttribute(NAME_DESC, info.getDesc());
		}

		Element braile = XmlUtil.addElement(doc, elm, NAME_BRAILE);
		for (BrailleInfo.EXTRA extra: BrailleInfo.EXTRA.values())
		{
			if (info.haveExtra(extra))
			{
				braile.setAttribute(BrailleDict.extraNameMap.get(extra), "true");
			}
		}

		for (BrailleInfo.CHECK check: BrailleInfo.CHECK.values())
		{
			if (info.needCheck(check))
			{
				braile.setAttribute(BrailleDict.checkNameMap.get(check), "true");
			}
		}

		this.storeDots(doc, braile, info);

		return elm;
	}

	/**
	 * 辞書をファイルに書き出す<br/>
	 * ・例外が起きた場合はUtil.logでログ出力する
	 *
	 * @param file 辞書ファイル
	 * @return true=成功 false=失敗
	 */
	public boolean save(File file)
	{
		try
		{
			Document doc = XmlUtil.createDocument(null, NAME_DICT);

			this.storeExtra(doc);

			Element chars = XmlUtil.addElement(doc, doc.getDocumentElement(), NAME_CHARS);

			for(BrailleInfo info: this.brailleInfoList)
			{
				this.createElement(doc, chars, info);
			}

			XmlUtil.write(doc, file);
		}
		catch (Exception ex)
		{
			Util.logException(ex);
			return false;
		}

		return true;
	}

	private void storeDots(Document doc, Element parent, BrailleInfo info)
	{
		int nDotList = info.getBoxCount();
		StringBuilder dotText = new StringBuilder();
		for (int i = 0; i < nDotList; i++)
		{
			Element code = XmlUtil.addElement(doc, parent, NAME_CODE);

			int[] dots = info.getBox(i);
			dotText.delete(0, dotText.length());
			for (int dot: dots)
			{
				if (0 < dotText.length())
				{
					dotText.append(DOT_SEPARATOR);
				}
				dotText.append(dot);
			}

			code.setAttribute(NAME_INDEX, Integer.toString(i+1));
			code.setTextContent(dotText.toString());
		}
	}

	private void storeExtra(Document doc)
	{
		Element elm = doc.createElement(NAME_EXTRA);
		for (BrailleInfo.EXTRA extra: BrailleInfo.EXTRA.values())
		{
			this.storeExtra(doc, elm, extra);
		}
		if (0 < elm.getChildNodes().getLength())
		{
			doc.getDocumentElement().appendChild(elm);
		}
	}

	private boolean storeExtra(Document doc, Element parent, BrailleInfo.EXTRA extra)
	{
		BrailleInfo info = this.getExtra(extra);
		if (null == info)
		{
			return true;
		}
		Element elm = XmlUtil.addElement(doc, parent, BrailleDict.extraNameMap.get(extra));

		elm.setAttribute(NAME_TYPE, info.getType().name());

		elm.setAttribute(NAME_POSTCHAR, Boolean.toString(info.isPostChar()));

		elm.setAttribute(NAME_NABCC, info.getNABCC(false));

		this.storeDots(doc, elm, info);

		return true;
	}

	/**
	 * 行列・表の位置指示の文字を取得する
	 *
	 * @param key 位置指示
	 * @param options オプション情報
	 * @return 位置指示の文字
	 */
	public BrailleInfo getTable(BrailleInfo.TABLE key, EnumMap<BrailleInfo.TABLE_OPTION, Object> options)
	{
		BrailleInfo info = BrailleInfo.newBrailleInfo(this);
		info.setTable(key, options);

		return info;
	}

	/**
	 * 外字符、大文字符などの符号のBrileInfoを辞書から取得する
	 *
	 * @param extra 符号
	 * @return BraillleInfo
	 */
	public BrailleInfo getExtra(BrailleInfo.EXTRA extra)
	{
		return this.extraMap.get(extra);
	}

	/**
	 * 外字符、大文字付などの符号のBrailleInfoを辞書に登録する<br/>
	 * ・既に登録されている場合は置き換える
	 *
	 * @param extra 符号
	 * @param info BrailleInfo
	 */
	public void setExtra(BrailleInfo.EXTRA extra, BrailleInfo info)
	{
		this.extraMap.put(extra, info);
	}

	/**
	 * 外字符、大文字付などの符号を辞書から削除する
	 *
	 * @param extra 符号
	 */
	public void delExtra(BrailleInfo.EXTRA extra)
	{
		this.extraMap.remove(extra);
	}

	/**
	 * 登録されている墨字のリストを取得する
	 *
	 * @return 墨字のリスト
	 */
	public TreeSet<String> getSumijiList()
	{
		TreeSet<String> list = Util.newTreeSet();
		for (BrailleInfo info: this.brailleInfoList)
		{
			list.add(info.getSumiji());
		}

		return list;
	}

	/**
	 * BrailleInfoを生成する<br/>
	 * ・thisを引数にしてBrailleInfoのnewBrailleInfoを呼ぶ
	 *
	 * @return BrailleInfo
	 */
	public BrailleInfo newBrailleInfo()
	{
		BrailleInfo info = BrailleInfo.newBrailleInfo(this);

		return info;
	}

	/**
	 * BrailleInfoを検索する<br/>
	 * ・符号を指定した場合は、符号を持つBrailleInfoだけを検索する
	 *
	 * @param sumiji 墨字
	 * @param extras 符号
	 * @return BrailleInfo 見つからない場合はBrailleInfo.SPACE
	 */
	public BrailleInfo getBrailleInfo(String sumiji, BrailleInfo.EXTRA... extras)
	{
		for (BrailleInfo info: this.brailleInfoList)
		{
			if (null != extras)
			{
				boolean flag = false;
				for (BrailleInfo.EXTRA extra: extras)
				{
					if (false == info.haveExtra(extra))
					{
						flag = true;
						break;
					}
				}
				if (flag)
				{
					continue;
				}
			}
			if (sumiji.equals(info.getSumiji()))
			{
				return info;
			}
		}

		return BrailleInfo.SPACE;
	}

	/**
	 * 下がり文字を検索する<br/>
	 * 下がり文字は0以上9以下の数字だけなので、0以上の数値のみ対象
	 *
	 * @param val 0以上の数値の文字列
	 * @return　BrailleInfoのリスト 下がり文字がない場合はnull
	 */
	public List<BrailleInfo> getSagari(String val)
	{
		List<BrailleInfo> list = null;
		try
		{
			int num = Integer.parseInt(val);

			if (0 <= num)
			{
				list = Util.newArrayList();

				BrailleInfo pre = this.getBrailleInfo("下がり文字符号");
				list.add(pre);

				for (int i = 0; i < val.length(); i++)
				{
					String one = String.valueOf(val.charAt(i));
					BrailleInfo info = this.getBrailleInfo(SAGARI + one, BrailleInfo.EXTRA.SUUFU);
					list.add(info);
				}
			}
		}
		catch (NumberFormatException ex)
		{
		}

		return list;
	}

	/**
	 * 点字マスからBrailleInfoを検索する
	 *
	 * @param boxList 点字マスのリスト
	 * @return BrailleInfo
	 */
	public BrailleInfo getBraille(List<BrailleBox> boxList)
	{
//		for (BrailleInfo info: this.braileInfoMap.values())
		for (BrailleInfo info: this.brailleInfoList)
		{
			// 符号を比較
			int nExtra = 0;
			boolean flag = true;
			for (BrailleInfo.EXTRA extra: BrailleInfo.EXTRA.values())
			{
				if (info.haveExtra(extra))
				{
					BrailleInfo e = this.getExtra(extra);
					for (int i = 0; i < e.getBoxCount(); i++)
					{
						int[] s = boxList.get(i).getDots();
						int[] c = e.getBox(i);
						if (false == Arrays.equals(s, c))
						{
							flag = false;
							break;
						}
					}
					if (flag)
					{
						nExtra++;
					}
					else
					{
						break;
					}
				}
			}
			if (false == flag)	// 符号が違う
			{
				continue;
			}
			// 本体を比較
			if (info.getBoxCount() == (boxList.size() - nExtra))
			{
				for (int i = nExtra; i < boxList.size(); i++)
				{
					int[] s = boxList.get(i).getDots();
					int[] c = info.getBox(i-nExtra);
					if (false == Arrays.equals(s, c))
					{
						flag = false;
						break;
					}
				}
				if (flag)
				{
					return info;
				}
			}
		}

		return null;
	}

	/**
	 * 符号を含めたNABCCからBrailleInfoを検索する
	 *
	 * @param nabcc NABCC
	 * @return 見つからない場合はnull
	 */
	public BrailleInfo getBraille(String nabcc)
	{
		for (BrailleInfo info: this.brailleInfoList)
		{
			String n = info.getNABCC(true);

			if (nabcc.equals(n))
			{
				return info;
			}
		}

		return null;
	}

	/**
	 * BrailleInfoを置き換える<br/>
	 * ・oldInfoがnullの場合、oldInfoが登録されていない場合は、newInfoを追加する<br/>
	 * ・oldInfoがnullでなく、登録済の場合は置き換える
	 *
	 * @param oldInfo 置き換えられるBrailleInfo
	 * @param newInfo 置き換えるBrailleInfo
	 * @return true/false
	 */
	public boolean replaceBrailleInfo(BrailleInfo oldInfo, BrailleInfo newInfo)
	{
		if (null == oldInfo || false == this.brailleInfoList.contains(oldInfo))
		{
			this.brailleInfoList.add(newInfo);
		}
		else if (this.brailleInfoList.contains(oldInfo))
		{
			this.brailleInfoList.remove(oldInfo);
			this.brailleInfoList.add(newInfo);
		}

		this.setMax(newInfo);

		return true;
	}

	/**
	 * BrailleInfoを削除する<br/>
	 * ・符号を指定した場合は、符号を持つBrailleInfoを削除する
	 *
	 * @param sumiji 墨字
	 * @param extras 符号
	 * @return true=成功 false=失敗
	 */
	public boolean delBrailleInfo(String sumiji, BrailleInfo.EXTRA... extras)
	{
		BrailleInfo b = this.getBrailleInfo(sumiji, extras);
		if (b != BrailleInfo.SPACE)
		{
			this.brailleInfoList.remove(b);
		}

		this.maxSumijiLen = 0;
		this.maxBoxCount = 0;
		this.maxNABCCLen = 0;
		for (BrailleInfo info: this.brailleInfoList)
		{
			this.setMax(info);
		}

		return true;
	}
}
