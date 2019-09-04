package dssp.brailleLib;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * XML関連の便利クラス
 *
 * @author DSSP/Minoru Yagi
 *
 */
public class XmlUtil implements ErrorHandler
{
	private static XPath xPath;
	static
	{
		XPathFactory xPathfactory = XPathFactory.newInstance();
		xPath = xPathfactory.newXPath();
	}

	// インスタンス化を抑制
	private XmlUtil()
	{

	}
	private static XmlUtil instance = new XmlUtil();

	/**
	 * XPathを生成する
	 *
	 * @return XPath
	 */
	public static XPath getXPath()
	{
		return xPath;
	}

	/**
	 * 指定したパスのテキストを取得する<br/>
	 * [パスの形式]<br/>
	 * root/child1/child2
	 *
	 * @param doc Document
	 * @param xPathText パス
	 * @return テキスト。ノードがない場合は空文字
	 * @throws IllegalArgumentException 引数がnullか、xPathTextが空文字
	 * @throws XPathExpressionException パスが不正
	 */
	public static String getString(Document doc, String xPathText) throws XPathExpressionException
	{
		if (null == doc || null == xPathText || xPathText.isEmpty())
		{
			throw new IllegalArgumentException("引数がnullか、xPathTextが空文字");
		}
		XPath xPath = XmlUtil.getXPath();
		return xPath.evaluate(xPathText, doc);
	}

	/**
	 * 指定したパスにテキストを設定する<br/>
	 * ・パスの要素がなければ追加する<br/>
	 * ・パス以下の子ノードは削除する<br/>
	 * [パスの形式]<br/>
	 * root/child1/child2
	 *
	 * @param doc Document
	 * @param path パス
	 * @param val テキスト
	 * @throws IllegalArgumentException 引数がnullか、pathが空文字
	 * @throws XPathExpressionException パスが不正
	 */
	public static void setString(Document doc, String path, String val) throws XPathExpressionException
	{
		if (null == doc || null == path || path.isEmpty())
		{
			throw new IllegalArgumentException("引数がnullか、pathが空文字");
		}
		Element telm = addElement(doc, path, false);
//		StringTokenizer st = new StringTokenizer(path, "/");
//		Element parent = doc.getDocumentElement();
//		String tpath = st.nextToken();
//		Element telm = null;
//		while (st.hasMoreTokens())
//		{
//			String tag = st.nextToken();
//			tpath += (0 == tpath.length() ? tag : "/" + tag);
//			telm = (Element)XmlUtil.getNode(doc, tpath);
//			if (null == telm)
//			{
//				telm = doc.createElement(tag);
//				parent.appendChild(telm);
//			}
//			parent = telm;
//		}
		XmlUtil.addNodeText(doc, telm, val);
	}

	/**
	 * 指定したパスのノードを検索する<br/>
	 * [パスの形式]<br/>
	 * root/child1/child2
	 *
	 * @param doc Document
	 * @param xPathText パス
	 * @return 検索したノード
	 * @throws IllegalArgumentException 引数がnullか、xPathTextが空文字
	 * @throws XPathExpressionException パスが不正
	 */
	public static Node getNode(Document doc, String xPathText) throws XPathExpressionException
	{
		if (null == doc || null == xPathText || xPathText.isEmpty())
		{
			throw new IllegalArgumentException("引数がnullか、xPathTextが空文字");
		}
		XPath xPath = XmlUtil.getXPath();
		Node node = (Node) xPath.evaluate(xPathText, doc, XPathConstants.NODE);
		return node;
	}

	/**
	 * 指定したパスのノードリストを検索する<br/>
	 * [パスの形式]<br/>
	 * root/child1/child2
	 *
	 * @param doc DOcument
	 * @param xPathText パス
	 * @return 検索したノードリスト
	 * @throws IllegalArgumentException 引数がnullか、xPathTextが空文字
	 * @throws XPathExpressionException パスが不正
	 */
	public static NodeList getNodeList(Document doc, String xPathText) throws XPathExpressionException
	{
		if (null == doc || null == xPathText || xPathText.isEmpty())
		{
			throw new IllegalArgumentException("引数がnullか、xPathTextが空文字");
		}
		XPath xPath = XmlUtil.getXPath();
		NodeList nodeList = (NodeList)xPath.evaluate(xPathText, doc, XPathConstants.NODESET);
		return nodeList;
	}

	/**
	 * Documentを生成する
	 *
	 * @param namespace	名前空間
	 * @param name ルートのタグ名
	 * @return Document
	 * @throws IllegalArgumentException nameがnullか、空文字
	 * @throws ParserConfigurationException パスが不正
	 */
	public static Document createDocument(String namespace, String name) throws ParserConfigurationException
	{
		if (null == name || name.isEmpty())
		{
			throw new IllegalArgumentException("nameがnullか、空文字");
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		DOMImplementation domImpl =builder.getDOMImplementation();
		if (null == namespace)
		{
			namespace = "";
		}
		Document document = domImpl.createDocument(namespace, name, null);

		return document;
	}

	/**
	 * Elementを追加する
	 *
	 * @param doc Document
	 * @param parent 親ノード
	 * @param name 追加するElementの名前
	 * @return 追加されたElement
	 * @throws IllegalArgumentException 引数がnullか、nameが空文字
	 */
	public static Element addElement(Document doc, Node parent, String name)
	{
		if (null == doc || null == parent || null == name || name.isEmpty())
		{
			throw new IllegalArgumentException("引数がnullか、nameが空文字");
		}
		Element elm = doc.createElement(name);
		parent.appendChild(elm);

		return elm;
	}

	/**
	 * Elementを追加する<br/>
	 * ・パス途中のNodeがなければ追加する
	 *
	 * @param doc Document
	 * @param path 追加するElementのパス
	 * @param forced true=既に同じパスのElemenがあっても追加する false=追加しないで既にあるElementを返す
	 * @return 追加されたElement
	 * @throws IllegalArgumentException 引数がnullか、nameが空文字か、既にあるルートパスと異なる
	 * @throws XPathExpressionException パスが不正
	 */
	public static Element addElement(Document doc, String path, boolean forced) throws XPathExpressionException
	{
		if (null == doc || null == path || path.isEmpty())
		{
			throw new IllegalArgumentException("引数がnullか、pathが空文字");
		}

		Element parent = doc.getDocumentElement();
		String[] names = path.split("/");
		if (1 == names.length)
		{
			if (names[0].equals(parent.getTagName()))
			{
				return parent;
			}
			else
			{
				throw new IllegalArgumentException("既にあるルートパスと異なる");
			}
		}

		Element telm = null;
		StringBuilder tpath = new StringBuilder(names[0]);
		int maxDepth = (forced ? names.length-1 : names.length);
		for (int i = 1; i < maxDepth; i++)
		{
			tpath.append("/");
			tpath.append(names[i]);
			telm = (Element) XmlUtil.getNode(doc, tpath.toString());
			if (null == telm)
			{
				telm = doc.createElement(names[i]);
				parent.appendChild(telm);
			}

			parent = telm;
		}
		if (forced)
		{
			telm = doc.createElement(names[names.length-1]);
			parent.appendChild(telm);
		}

//		StringTokenizer st = new StringTokenizer(path, "/");
//		Element parent = doc.getDocumentElement();
//		String tpath = st.nextToken();
//		Element telm = null;
//		while (st.hasMoreTokens())
//		{
//			String tag = st.nextToken();
//			tpath += (0 == tpath.length() ? tag : "/" + tag);
//			telm = (Element)XmlUtil.getNode(doc, tpath);
//			if (null == telm)
//			{
//				telm = doc.createElement(tag);
//				parent.appendChild(telm);
//			}
//			parent = telm;
//		}

		return telm;
	}

	/**
	 * ファイルからDocumentを生成する
	 *
	 * ・ファイルがない場合は、新しいDocumentを生成する
	 *
	 * @param file ファイル名。nullの場合は新しいDocumentを生成する
	 * @return Document
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static Document parse(File file) throws IOException, SAXException, ParserConfigurationException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setErrorHandler(XmlUtil.instance);
		Document document = null;
		if (null != file && file.exists())
		{
			document = builder.parse(file);
		}
		else
		{
			document = builder.newDocument();
		}

		return document;
	}

	/**
	 * テキストからDocumentを生成する
	 *
	 * @param text XMLテキスト
	 * @return Document
	 * @throws IllegalArgumentException 引数がnullか、空文字
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static Document parse(String text) throws IOException, SAXException, ParserConfigurationException
	{
		if (null == text || text.isEmpty())
		{
			throw new IllegalArgumentException("引数がnullか、空文字");
		}
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setErrorHandler(XmlUtil.instance);
		InputSource src = new InputSource(new StringReader(text));
		Document doc = builder.parse(src);

		return doc;
	}

	/**
	 * Documentをファイルに書き込む
	 *
	 * @param document Document
	 * @param file ファイル名
	 * @throws IllegalArgumentException 引数がnullか、空文字
	 * @throws TransformerException
	 * @throws FileNotFoundException
	 */
	public static void write(Document document, File file) throws IOException, TransformerException, FileNotFoundException
	{
		if (null == document || null == file)
		{
			throw new IllegalArgumentException("引数がnullか、空文字");
		}

		TransformerFactory transFactory = TransformerFactory.newInstance();
		Transformer transformer = transFactory.newTransformer();

		DOMSource source = new DOMSource(document);
		FileOutputStream os = new FileOutputStream(file);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
		StreamResult result = new StreamResult(writer);
		transformer.transform(source, result);

		writer.close();
	}

	/**
	 * Nodeをテキストに変換する<br/>
	 * インデントなしで、{@link #getXmlText(Node, boolean)}を呼ぶ
	 *
	 * @param node
	 * @return テキスト
	 * @throws TransformerException
	 */
	public static String getXmlText(Node node) throws TransformerException
	{
		return getXmlText(node, false);
	}

	/**
	 * Nodeをテキストに変換する
	 *
	 * @param node
	 * @param indent true=インデント fase = インデントなし
	 * @return テキスト
	 * @throws IllegalArgumentException nodeがnull
	 * @throws TransformerException
	 */
	public static String getXmlText(Node node, boolean indent) throws TransformerException
	{
		if (null == node)
		{
			throw new IllegalArgumentException("nodeがnull");
		}
		TransformerFactory transFactory = TransformerFactory.newInstance();
		Transformer transformer = transFactory.newTransformer();

		if (indent)
		{
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			// インデントのためだけにライブラリを増やしたくないので直書き
//			transformer.setOutputPropert(org.apache.xml.serializer.OutputPropertiesFactory.S_KEY_INDENT_AMOUNT, "2" );
			transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");
		}

		DOMSource source = new DOMSource(node);
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		transformer.transform(source, result);

		String text = writer.getBuffer().toString();

		return text;
	}

	/**
	 * Nodeの値を取得する
	 *
	 * @param node Node
	 * @return 値の文字列
	 * @throws IllegalArgumentException nodeがnull
	 * @see #addNodeText(Document, Node, String)
	 */
	public static String getNodeText(Node node)
	{
		if (null == node)
		{
			throw new IllegalArgumentException("nodeがnull");
		}

		StringBuffer text = new StringBuffer();
		NodeList children = node.getChildNodes();
		int nChild = children.getLength();
		for (int i = 0; i < nChild; i++)
		{
			Node child = children.item(i);
			if (Node.TEXT_NODE == child.getNodeType())
			{
				text.append(child.getNodeValue());
			}
		}

		return text.toString();
	}

	/**
	 * Nodeに値を追加する
	 *
	 * @param doc
	 * @param node
	 * @param text
	 * @throws IllegalArgumentException 引数がnull
	 * @see #getNodeText(Node)
	 */
	public static void addNodeText(Document doc, Node node, String text)
	{
		if (null == doc || null == node || null == text)
		{
			throw new IllegalArgumentException("引数がnull空文字");
		}

		Text textNode = doc.createTextNode(text);
		node.appendChild(textNode);
	}

	@Override
	public void error(SAXParseException exception) throws SAXException
	{
		throw exception;
	}

	@Override
	public void fatalError(SAXParseException exception) throws SAXException
	{
		throw exception;
	}

	@Override
	public void warning(SAXParseException exception) throws SAXException
	{
		throw exception;
	}
}

