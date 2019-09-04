package dssp.brailleLib;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * URLにアクセスしてソフト情報を取得する
 *
 * @author yagi
 *
 */
public class CheckInfo
{
	/**
	 * ソフト情報
	 *
	 * @param xmlText
	 */
	public static class SoftInfo
	{
		private Map<String, String> map = Util.newHashMap();

		public SoftInfo()
		{
		}

		public String getValue(String name)
		{
			return this.map.get(name);
		}

		/**
		 * XMLからソフト情報を抽出して初期化する
		 *
		 * @param xmlText
		 */
		public static SoftInfo fromString(String xmlText)
		{
			try
			{
				Document doc = XmlUtil.parse(xmlText);
				NodeList nodeList = doc.getElementsByTagName("version");
				int nNode = nodeList.getLength();
				SoftInfo info = new SoftInfo();
				info.map.clear();
				for (int i = 0; i < nNode; i++)
				{
					Node node = nodeList.item(i);
					NamedNodeMap attrs = node.getAttributes();
					int nAttr = attrs.getLength();
					for (int j = 0; j < nAttr; j++)
					{
						Node attr = attrs.item(j);
						info.map.put(attr.getNodeName(), attr.getTextContent());
					}
				}

				return info;
			}
			catch (IOException | SAXException | ParserConfigurationException e)
			{
				Util.logException(e);
			}
			return null;
		}
	}

	/**
	 * 応答ハンドラ
	 *
	 * @author yagi
	 *
	 */
	public static interface CheckInfoListener
	{
		/**
		 * 応答ハンドラ
		 *
		 * @param info ソフト情報
		 */
		public void checked(SoftInfo info);
	}

	public CheckInfo()
	{
		// TODO 自動生成されたコンストラクター・スタブ
	}

	private CheckInfoListener listener;
	private String urlText;
	private Thread thread;

	/**
	 * 応答ハンドラを登録して、URLにアクセスする
	 *
	 * @param urlText URL
	 * @param listener 応答ハンドラ
	 */
	public void check(String urlText, CheckInfoListener listener)
	{
		this.listener = listener;
		this.urlText = urlText;
		this.thread = new Thread(new Runnable(){
			@Override
			public void run()
			{
				checkInfo();
			}
		});
		this.thread.start();
	}

	/**
	 * URLにリクエストして応答ハンドラを呼び出す
	 */
	private void checkInfo()
	{
		if (null == this.listener)
		{
			return;
		}
		String xmlText = this.getHtml(this.urlText);
		if (null == xmlText)
		{
			this.listener.checked(null);
			return;
		}
		SoftInfo info = SoftInfo.fromString(xmlText);
		this.listener.checked(info);
	}

	/**
	 * 指定したURLからHTMLを取得する
	 *
	 * @param urlText URL
	 * @return HTMLテキスト
	 */
	private String getHtml(String urlText)
	{
		try
		{
			URL url = new URL(urlText);

			BufferedInputStream stream = new BufferedInputStream(url.openStream());
			List<ByteBuffer> bufList = Util.newArrayList();
			while (true)
			{
				byte[] buf = new byte[1024];
				int nbyte = stream.read(buf);
				if (0 > nbyte)
				{
					stream.close();
					break;
				}

				bufList.add(ByteBuffer.wrap(buf, 0, nbyte));
			}

			StringBuffer sbuf = new StringBuffer();
			Charset cs = Charset.forName("utf-8");
			for (ByteBuffer buf: bufList)
			{
				sbuf.append(cs.decode(buf));
			}

			return sbuf.toString();
		}
		catch (IOException e)
		{
			Util.logException(e);
		}

		return null;
	}
}
