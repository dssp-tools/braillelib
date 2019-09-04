package dssp.brailleLib;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * 墨字と点字の情報
 *
 * @author DSSP/Minoru Yagi
 *
 */
public class BrailleInfo implements Cloneable
{
	public static final BrailleInfo UNKNOWN = new BrailleInfo();
	public static final BrailleInfo SPACE = new BrailleInfo();
	public static final BrailleInfo LINEBREAK = new BrailleInfo();
	static {
		UNKNOWN.setNABCC(" ");
		UNKNOWN.setSumiji("■");
		UNKNOWN.setType(TYPE.VISIBLE);
		SPACE.setNABCC(" ");
		SPACE.setSumiji("　");
		SPACE.setType(TYPE.VISIBLE);
		LINEBREAK.setSumiji("\n");
		LINEBREAK.setType(TYPE.VISIBLE);
//		LINEBREAK.setLineBreak(true);
	}

	public static enum TYPE
	{
		/**
		 * 墨字を持つ
		 */
		VISIBLE,
		/**
		 * 点字固有
		 */
		ADDITIONAL,
		/**
		 * 未定
		 */
		UNKNOWN;
	}
	private TYPE type = TYPE.UNKNOWN;

	/**
	 * 外字符、大文字符などの符号
	 *
	 * @author yagi
	 *
	 */
	public static enum EXTRA
	{
		/**
		 * 外字符
		 */
		GAIJIFU("外字符"),
		/**
		 * 大文字符
		 */
		OOMOJIFU("大文字符"),
		/**
		 * 小文字符
		 */
		KOMOJIFU("小文字符"),
		/**
		 * 数符
		 */
		SUUFU("数符");

		private final String text;
		private static final Map<String, EXTRA> map = Util.newHashMap();
		static
		{
			for (EXTRA extra: EXTRA.values())
			{
				map.put(extra.toString(), extra);
			}
		}

		EXTRA(String text)
		{
			this.text = text;
		}

		@Override
		public String toString()
		{
			return this.text;
		}

		/**
		 * toString()の名前が一致するEXTRAを取得する
		 *
		 * @param text 名前
		 * @return EXTRA なければnull
		 */
		public static EXTRA fromString(String text)
		{
			return map.get(text);
		}
	};

	/**
	 * マスの点の最大数
	 */
	public static final int MAX_DOT_COUNT = 6;

	/**
	 * 行列、表の位置情報<br/>
	 * ・宣言の順に開始と終了とをチェックするので、順場を変えてはいけない
	 *
	 * @author yagi
	 *
	 */
	public static enum TABLE
	{
		/**
		 * 開始
		 */
		TABLE_OPEN,
		/**
		 * 行の始まり
		 */
		ROW_START,
		/**
		 * セルの始まり
		 */
		CELL_START,
		/**
		 * セルの終わり
		 */
		CELL_END,
		/**
		 * 行の終わり
		 */
		ROW_END,
		/**
		 * 終了
		 */
		TABLE_CLOSE;
	}
	private EnumSet<TABLE> tableSet = EnumSet.noneOf(TABLE.class);

	/**
	 * 行列や表のオプション情報
	 *
	 * @author yagi
	 *
	 */
	public static enum TABLE_OPTION
	{
		/**
		 * 結合する列数<br/>
		 * ・情報は整数
		 * ・CELL_STARTの場合のみ有効
		 */
		ROWSPAN("rowspan"),
		/**
		 * 結合する行数<br/>
		 * ・情報は整数
		 * ・CELL_STARTの場合のみ有効
		 */
		COLUMNSPAN("columnspan"),
		/**
		 * 枠線の種類<br/>
		 * ・情報は文字列
		 */
		FRAME("frame"),
		/**
		 * 行間罫線の種類<br/>
		 * ・情報は文字列
		 */
		ROWLINES("rowlines"),
		/**
		 * 列間罫線の種類<br/>
		 * ・情報は文字列
		 */
		COLUMNLINES("columnlines");

		final String attrName;
		TABLE_OPTION(String name)
		{
			this.attrName = name;
		}

		public String getAttrName()
		{
			return this.attrName;
		}
	}
	private EnumMap<TABLE_OPTION, Object> tableOptionMap = new EnumMap<TABLE_OPTION, Object>(TABLE_OPTION.class);

//	private boolean lineBreak = false;

	private EnumMap<EXTRA, Boolean> extraMap = new EnumMap<EXTRA, Boolean>(EXTRA.class);

	private String sumiji = "";

	private String nabcc = "";

	private String desc = "";

	private BrailleDict dict = null;

	private List<int[]> boxList = Util.newArrayList();

	/**
	 * この字の前後に数式の区切り符号の確認が必要か
	 */
	public static enum CHECK
	{
		/**
		 * 前の確認
		 */
		PRECHECK,
		/**
		 * 後の確認
		 */
		POSTCHECK;
	}
	private EnumMap<CHECK, Boolean> checkMap = new EnumMap<CHECK, Boolean>(CHECK.class);

	/**
	 * 「ちゃ」の「ゃ」のように後置文字か
	 */
	private boolean postChar = false;

	public boolean isPostChar()
	{
		return postChar;
	}

	public void setPostChar(boolean postChar)
	{
		this.postChar = postChar;
	}

	private BrailleInfo()
	{
		for (EXTRA extra: EXTRA.values())
		{
			this.setExtra(extra, false);
		}

		for (CHECK check: CHECK.values())
		{
			this.setCheck(check, false);
		}
	}

	@Override
	public String toString()
	{
		return String.format("%s[墨字=%s nabcc=%s 説明=%s]", this.getClass().getName(), this.getSumiji(), this.getNABCC(false), this.getDesc());
	}

	/**
	 * インスタンスを生成する
	 *
	 * @param dict 辞書
	 * @return 生成したインスタンス
	 * @throws IllegalArgumentException dictがnullの場合
	 */
	public static BrailleInfo newBrailleInfo(BrailleDict dict)
	{
		if (null == dict)
		{
			throw new IllegalArgumentException("dictがnull");
		}
		BrailleInfo info = new BrailleInfo();
		info.dict = dict;

		return info;
	}

	/**
	 * 複製する
	 *
	 * @return 複製したインスタンス
	 */
	@Override
	public BrailleInfo clone()
	{
		try
		{
			BrailleInfo info = (BrailleInfo) super.clone();

			info.sumiji = this.sumiji;
			info.extraMap = this.extraMap.clone();
			info.checkMap = this.checkMap.clone();
			info.tableSet = this.tableSet.clone();
			info.dict = this.dict;
//			info.lineBreak = this.lineBreak;
			info.desc = this.desc;

			info.tableOptionMap = this.tableOptionMap.clone();

			info.boxList = Util.newArrayList();
			for (int i = 0; i < this.boxList.size(); i++)
			{
				int[] dotsSrc = this.boxList.get(i);
				if (null != dotsSrc)
				{
					info.addBox(dotsSrc);
				}
			}

			return info;
		}
		catch (Exception ex)
		{
			Util.logException(ex);
			return null;
		}
	}

	@Override
	public boolean equals(Object o)
	{
		if (null == o)
		{
			return false;
		}
		if (o == this)
		{
			return true;
		}

		if (false == (o instanceof BrailleInfo))
		{
			return false;
		}

		BrailleInfo src = (BrailleInfo)o;
		if (null == this.getSumiji())
		{
			if (null != src.getSumiji())
			{
				return false;
			}
		}
		else if (false == this.getSumiji().equals(src.getSumiji()))
		{
			return false;
		}

		if (false == Arrays.deepEquals(this.extraMap.values().toArray(), src.extraMap.values().toArray()))
		{
			return false;
		}
		if (false == Arrays.deepEquals(this.checkMap.values().toArray(), src.checkMap.values().toArray()))
		{
			return false;
		}
		if (false == Arrays.deepEquals(this.tableSet.toArray(), src.tableSet.toArray()))
		{
			return false;
		}
		if (this.dict != src.dict)
		{
			return false;
		}
//		if (this.lineBreak != src.lineBreak)
//		{
//			return false;
//		}

		if (this.type != src.type)
		{
			return false;
		}
		if (null == this.nabcc)
		{
			if (null != src.nabcc)
			{
				return false;
			}
		}
		else if (false == this.nabcc.equals(src.nabcc))
		{
			return false;
		}

//		if (false == this.desc.equals(src.desc))
//		{
//			return false;
//		}
		if (false == Arrays.deepEquals(this.tableOptionMap.values().toArray(), src.tableOptionMap.values().toArray()))
		{
			return false;
		}

		if (this.getBoxCount() != src.getBoxCount())
		{
			return false;
		}
		for (int i = 0; i < this.getBoxCount(); i++)
		{
			int[] dots1 = this.getBox(i);
			int[] dots2 = src.getBox(i);
			if (false == Arrays.equals(dots1,  dots2))
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public int hashCode()
	{
		int result = 17;
		result = 31 * result + this.sumiji.hashCode();
		result = 31 * result + this.extraMap.hashCode();
		result = 31 * result + this.checkMap.hashCode();
		result = 31 * result + this.tableSet.hashCode();
		result = 31 * result + this.type.hashCode();
		result = 31 * result + this.nabcc.hashCode();
		result = 31 * result + this.dict.hashCode();
		result = 31 * result + this.desc.hashCode();
		result = 31 * result + this.tableOptionMap.hashCode();

		for (int i = 0; i < this.getBoxCount(); i++)
		{
			int[] dots = this.getBox(i);
			result = 31 * result + Arrays.hashCode(dots);
		}

		return result;
	}

	/**
	 * 辞書を取得する
	 *
	 * @return
	 */
	public BrailleDict getDict()
	{
		return dict;
	}

	/**
	 * 墨字を取得する
	 *
	 * @return 墨字
	 */
	public String getSumiji()
	{
		return this.sumiji;
	}

	/**
	 * 墨字を登録する
	 *
	 * @param sumiji 墨字
	 */
	public void setSumiji(String sumiji)
	{
		this.sumiji = sumiji;
	}


	/**
	 * TYPEを取得する
	 *
	 * @return
	 */
	public TYPE getType()
	{
		return type;
	}

	/**
	 * TYPEを設定する
	 *
	 * @param type
	 */
	public void setType(TYPE type)
	{
		this.type = type;
	}

	/**
	 * NABCCコードを取得する
	 *
	 * @param withExtra true=符号を含める
	 * @return NABCCコード
	 */
	public String getNABCC(boolean withExtra)
	{
		if (withExtra)
		{
			StringBuilder buf =new StringBuilder();
			for (BrailleInfo.EXTRA extra: BrailleInfo.EXTRA.values())
			{
				if (this.haveExtra(extra))
				{
					buf.append(this.getExtra(extra).getNABCC(false));
				}
			}
			buf.append(this.nabcc);
			return buf.toString();
		}

		return nabcc;
	}

	/**
	 * NABCCコードを登録する
	 *
	 * @param nabcc NABCCコード
	 */
	public void setNABCC(String nabcc)
	{
		this.nabcc = nabcc;
	}

	/**
	 * マスの数を取得する<br/>
	 *
	 * ・外字符、大文字符などの符号は含まない
	 *
	 * @return マスの数
	 */
	public int getBoxCount()
	{
		return this.boxList.size();
	}

	/**
	 * マスの点のリストを取得する
	 *
	 * @param index マスの番号(0から始まる)
	 * @return 点のリスト
	 * @throws IndexOutOfBoundsException indexが負か、マスの数以上
	 */
	public int[] getBox(int index)
	{
		if (0 > index || this.boxList.size() <= index)
		{
			throw new IndexOutOfBoundsException("indexが負か、マスの数以上");
		}
		return this.boxList.get(index);
	}

	/**
	 * マスを追加する<br/>
	 *
	 * ・マスの番号に-1を指定して、addDotListを呼ぶ
	 *
	 * @param dots マスの点のリスト
	 * @return 追加されたマスの番号(0から始まる)
	 */
	public int addBox(int[] dots)
	{
		return this.addBox(-1, dots);
	}

	/**
	 * 番号を指定してマスを追加する<br/>
	 *
	 * ・指定した番号以後にマスがある場合は右に移動する
	 *
	 * @param index マスの番号。-1の場合はマスのリストの最後に追加する
	 * @param dots マスの点のリスト
	 * @return 追加されたマスの番号(0から始まる)
	 */
	public int addBox(int index, int[] dots)
	{
		int[] tmp = Arrays.copyOf(dots, dots.length);
		if (0 > index || this.boxList.size() <= index)
		{
			this.boxList.add(tmp);
			return this.boxList.size()-1;
		}
		else
		{
			this.boxList.add(index, tmp);
			return index;
		}
	}

	/**
	 * マスを入れ替える
	 *
	 * @param index マスの番号(0から始まる)
	 * @param dots マスの点のリスト
	 * @throws IndexOutOfBoundsException indexが負か、マスの数以上
	 */
	public void setBox(int index, int[] dots)
	{
		if (0 > index || this.boxList.size() <= index)
		{
			throw new IndexOutOfBoundsException("indexが負か、マスの数以上");
		}
		int[] tmp = Arrays.copyOf(dots, dots.length);
		this.boxList.set(index, tmp);
	}

	/**
	 * マスを削除する
	 *
	 * @param index マスの番号(0から始まる)
	 * @return true=成功 false=失敗
	 * @throws IndexOutOfBoundsException indexが負か、マスの数以上
	 */
	public boolean delBox(int index)
	{
		if (0 > index || this.boxList.size() <= index)
		{
			throw new IndexOutOfBoundsException("indexが負か、マスの数以上");
		}
		this.boxList.remove(index);
		return true;
	}

	/**
	 * マスを全て削除する
	 */
	public void delAllBox()
	{
		this.boxList.clear();
	}

	/**
	 * マスを左に移動する<br/>
	 * ・indexが1より小さいか、マスの数以上の場合は何もしない
	 *
	 * @param index マスの番号(0から始まる)
	 * @return 移動後のマスの番号(0から始まる)
	 */
	public int moveLeft(int index)
	{
		if (1 > index || this.boxList.size() <= index)
		{
			return index;
		}
		int[] dots = this.getBox(index);
		this.addBox(index-1, dots);
		this.delBox(index+1);

		return index-1;
	}

	/**
	 * マスを右に移動する<br/>
	 * ・indexが負か、(マスの数-1)以上の場合は何もしない
	 *
	 * @param index マスの番号(0から始まる)
	 * @return 移動後のマスの番号(0から始まる)
	 */
	public int moveRight(int index)
	{
		if (0 > index || (this.boxList.size()-1) <= index)
		{
			return index;
		}
		int[] dots = this.getBox(index);
		this.addBox(index+2, dots);
		this.delBox(index);

		return index+1;
	}

	/**
	 * 符号のBrailleInfoを取得する
	 *
	 * @param extra 符号
	 * @return 辞書が無い場合、見つからない場合はnull
	 */
	public BrailleInfo getExtra(EXTRA extra)
	{
		if (null == this.dict)
		{
			return null;
		}
		return this.dict.getExtra(extra);
	}

	/**
	 * 外字符、大文字符などの符号の有無を設定する
	 *
	 * @param extra 符号
	 * @param flag true=あり false=なし
	 */
	public void setExtra(EXTRA extra, boolean flag)
	{
		this.extraMap.put(extra, flag);
	}

	/**
	 * 外字符、大文字付などの符号の有無を取得する
	 *
	 * @param extra 符号
	 * @return true=あり false=なし
	 */
	public boolean haveExtra(EXTRA extra)
	{
		return this.extraMap.get(extra);
	}

	/**
	 * 符号を全て無しにする
	 */
	public void clearExtra()
	{
		for (EXTRA extra: EXTRA.values())
		{
			this.extraMap.put(extra, false);
		}
	}

	/**
	 * 外字符、大文字符などの符号かどうかを取得する<br/>
	 *
	 * @return true=符号 false=符号ではない
	 */
	public boolean isExtra()
	{
		for (EXTRA extra: EXTRA.values())
		{
			if (this.isExtraOf(extra))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * 外字符、大文字符などの符号かどうかを取得する<br/>
	 *
	 * @param extra 確かめる符号
	 * @return true=符号 false=符号ではない
	 */
	public boolean isExtraOf(EXTRA extra)
	{
		return (null == extra ? false: extra.text.equals(this.sumiji));
	}

	/**
	 * 行列や表のオプション情報を設定する<br/>
	 * ・オプション情報は{@code TABLE_OPTION}
	 *
	 * @param mark 位置情報
	 * @param options オプション情報(ない場合はnull)
	 */
	public void setTable(TABLE mark, EnumMap<TABLE_OPTION, Object> options)
	{
		this.tableSet.add(mark);
		this.setDesc(mark.toString());
		if (null != options)
		{
			this.tableOptionMap.clear();
			this.tableOptionMap.putAll(options);
		}
	}

	/**
	 * 行列や表の位置情報を削除する
	 *
	 * @param mark 位置情報
	 */
	public void unsetTable(TABLE mark)
	{
		this.tableSet.remove(mark);
	}

	/**
	 * 行列や表の位置情報を確認する
	 *
	 * @param mark 位置情報
	 * @return true=位置情報がある false=ない
	 */
	public boolean haveTable(TABLE mark)
	{
		return this.tableSet.contains(mark);
	}

	/**
	 * 行列や表のオプション情報を取得するKbr/>
	 *
	 * @param key 情報の種類
	 * @return 情報(ない場合はnullか0)
	 */
	@SuppressWarnings("unchecked")
	public <T> T getTableOption(TABLE_OPTION key)
	{
		T obj = null;
		Object val = this.tableOptionMap.get(key);
		switch(key)
		{
		case ROWSPAN:
			if (null == val)
			{
				obj = (T) Integer.valueOf(0);
			}
			else
			{
				obj = (T) (this.haveTable(TABLE.CELL_START) ? (val instanceof Integer ? (Integer) val : Integer.valueOf(0)) : Integer.valueOf(0));
			}
			break;
		case COLUMNSPAN:
			if (null == val)
			{
				obj = (T) Integer.valueOf(0);
			}
			else
			{
				obj = (T) (this.haveTable(TABLE.CELL_START) ? (val instanceof Integer ? (Integer) val : Integer.valueOf(0)) : Integer.valueOf(0));
			}
			break;
		default:
			if (null == val)
			{
				return null;
			}
			obj = (T) (this.haveTable(TABLE.TABLE_OPEN) ? (val instanceof String ? (String) val : null) : null);
			break;
		}

		return obj;
	}

	/**
	 * 改行文字の設定をする
	 *
	 * @param flag true=改行文字にする false = 設定を解除する
	 */
//	private void setLineBreak(boolean flag)
//	{
//		this.lineBreak = flag;
//	}

	/**
	 * 改行文字の設定を取得する
	 *
	 * @return true=改行文字<br/>改行文字ではない
	 */
	public boolean isLineBreak()
	{
		return (this == BrailleInfo.LINEBREAK);
//		return this.lineBreak;
	}

	/**
	 * 説明を取得する
	 *
	 * @return 説明のテキスト
	 */
	public String getDesc()
	{
		return this.desc;
	}

	/**
	 * 説明を設定する
	 *
	 * @param desc 説明のテキスト
	 */
	public void setDesc(String desc)
	{
		this.desc = desc;
	}

	/**
	 * この字の前後に数式の区切り符号の確認が必要かを設定する
	 *
	 * @param check PRECHECK=前 POSTCHECK=後
	 * @param flag　true=必要 false=不要
	 */
	public void setCheck(CHECK check, boolean flag)
	{
		this.checkMap.put(check, flag);
	}
	/**
	 * この字の前後に数式の区切り符号の確認が必要かを取得する
	 *
	 * @param check PRECHECK=前 POSTCHECK=後
	 * @return
	 */
	public boolean needCheck(CHECK check)
	{
		Boolean flag = this.checkMap.get(check);
		return (null == flag ? false: flag);
	}

	/**
	 * 空点字かどうかを取得する
	 *
	 * @return true=空点字
	 */
	public boolean isEmpty()
	{
		return (0 == this.boxList.size());
	}
}

