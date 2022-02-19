package dssp.brailleLib;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Queue;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * 便利クラス
 *
 * @author DSSP/Minoru Yagi
 *
 */
public final class Util {
    private static final Util INSTANCE = new Util();

    public enum LOG {
        INFO, WARNING, ERROR, EXCEPTION, DEBUG
    };

    private static final String FOLDER = "user.dir";
    public static final String LOG_FILE = "status.log";
    private static JFileChooser dlg = null;
    private static boolean debug = false;

    private FileHandler fh = null;

    private Util() {
    }

    /**
    * PATHから検索してnameのパスを取得する
    *
    * @param name
    * @return
    */
    public static String exePath(String name) {
        File f = new File(System.getProperty("user.dir") + File.separator + name);
        if (f.exists()) {
            return f.getAbsolutePath();
        }

        String com = System.getProperty("sun.java.command");
        String[] coms = com.split(" ");
        File exe = new File(coms[0]);
        String p = exe.getParent();
        if (null == p) {
            return name;
        }
        return p + File.separator + name;
    }

    /**
    * ログファイルの初期化
    * ・ファイルサイズ10MB
    *
    * @param fileName ファイル名
    */
    public static void initLog(String fileName) {
        initLog(fileName, 10);
    }

    /**
    * ログファイルの初期化
    *
    * @param fileName ファイル名
    * @param limit ファイルサイズの上限(MB)
    */
    public static void initLog(String fileName, int limit) {
        try {
            INSTANCE.fh = new FileHandler(fileName, limit * 1024 * 1024, 1, true);
            INSTANCE.fh.setFormatter(new LogFormatter());
        } catch (SecurityException | IOException e) {
            warning(String.format("ログファイル%sを作れませんでした", fileName));
            e.printStackTrace();
        }
    }

    public static void close() {
        if (null != INSTANCE.fh) {
            INSTANCE.fh.close();
        }
    }

    private static void initDlg() {
        if (null == dlg) {
            Properties prop = System.getProperties();
            String folder = prop.getProperty(FOLDER);
            dlg = new JFileChooser(folder);

            try {
                javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                javax.swing.SwingUtilities.updateComponentTreeUI(dlg);
            } catch (Exception e) {
                Util.logException(e);
            }
        }
    }

    // クリップボード

    /**
    * システムクリップボードにテキストまたは画像を登録する
    *
    * @param obj StringまたはImage
    * @return true/false
    */
    public static <T> boolean setToClipboard(T obj) {
        if (obj instanceof String) {
            String text = (String) obj;
            StringSelection selection = new StringSelection(text);

            Toolkit tk = Toolkit.getDefaultToolkit();
            Clipboard clipBoard = tk.getSystemClipboard();
            clipBoard.setContents(selection, selection);
        } else if (obj instanceof Image) {
            Image image = (Image) obj;

            Toolkit tk = Toolkit.getDefaultToolkit();
            Clipboard clipBoard = tk.getSystemClipboard();
            ImageSelection selection = new ImageSelection(image);
            clipBoard.setContents(selection, selection);
        } else {
            return false;
        }

        return true;
    }

    /**
    * システムクリップボードからテキストまたは画像を取得する
    *
    * @param c 取得するオブジェクトと同じクラスのオブジェクト
    * @return 空の場合、失敗した場合はnull
    */
    public static <T> T getFromClipboard(T c) {
        if (null == c) {
            return null;
        }

        Toolkit tk = Toolkit.getDefaultToolkit();
        Clipboard clipBoard = tk.getSystemClipboard();
        Transferable tr = clipBoard.getContents(null);

        DataFlavor[] fs = tr.getTransferDataFlavors();

        for (DataFlavor f : fs) {
            if (f != DataFlavor.stringFlavor && f != DataFlavor.imageFlavor) {
                continue;
            }

            try {
                Object obj = tr.getTransferData(f);
                if (obj.getClass().equals(c.getClass())) {
                    @SuppressWarnings("unchecked")
                    T ret = (T) obj;
                    return ret;
                }
            } catch (UnsupportedFlavorException | IOException e) {
                Util.logException(e);
            }
        }

        return null;
    }

    // List、Stack、Queueなどの生成

    /**
    * Queueを生成する<br/>
    *
    * ・ArrayDequeを生成する<br/>
    * [使用例]<br/>
    * {@code Queue<String> stack = Util.newQueue();}
    *
    * @return Stack
    */
    public static <T> Queue<T> newQueue() {
        return new ArrayDeque<T>();
    }

    /**
    * Stackを生成する<br/>
    *
    * [使用例]<br/>
    * {@code Stack<String> stack = Util.newStack();}
    *
    * @return Stack
    */
    public static <T> Stack<T> newStack() {
        return new Stack<T>();
    }

    /**
    * Vectorを生成する<br/>
    *
    * [使用例]<br/>
    * {@code Vector<String> vector = Util.newVector();}
    *
    * @return Vector
    */
    public static <T> Vector<T> newVector() {
        return new Vector<T>();
    }

    /**
    * ArrayListを生成する<br/>
    *
    * [使用例]<br/>
    * {@code List<String> list = Util.newArrayList();}
    *
    * @return ArrayList
    */
    public static <T> ArrayList<T> newArrayList() {
        return new ArrayList<T>();
    }

    /**
    * ArrayListを複製する<br/>
    *
    * [使用例]<br/>
    * {@code List<String> list = Util.newArrayList();}
    *
    * @param src
    * @return ArrayList
    */
    public static <T> ArrayList<T> newArrayList(Collection<T> src) {
        return new ArrayList<T>(src);
    }

    /**
    * TreeSetを生成する<br/>
    *
    * [使用例]<br/>
    * {@code Set<String> set = Util.newTreeSet();}
    *
    * @param comp 要素を比較するComparator
    * @return TreeSet
    */
    public static <T> TreeSet<T> newTreeSet(Comparator<T> comp) {
        return new TreeSet<T>(comp);
    }

    /**
    * TreeSetを生成する<br/>
    *
    * [使用例]<br/>
    * {@code Set<String> set = Util.newTreeSet();}
    *
    * @return TreeSet
    */
    public static <T> TreeSet<T> newTreeSet() {
        return new TreeSet<T>();
    }

    /**
    * Hashtableを生成する<br/>
    *
    * [使用例]<br/>
    * {@code Map<String, String> map = Util.newHashtable();}
    *
    * @return Hashtable
    */
    public static <K, V> Hashtable<K, V> newHashtable() {
        return new Hashtable<K, V>();
    }

    /**
    * HashMapを生成する<br/>
    *
    * [使用例]<br/>
    * {@code Map<String, String> map = Util.newHashMap();}
    *
    * @return HashMap
    */
    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<K, V>();
    }

    /**
    * TreeMapを生成する<br/>
    *
    * [使用例]<br/>
    * {@code Map<String, String> map = Util.newTreeMap();}
    *
    * @return TreeMap
    */
    public static <K, V> TreeMap<K, V> newTreeMap() {
        return new TreeMap<K, V>();
    }

    // 表示関係

    /**
    * 画面の解像度を取得する<br/>
    *
    * @return 解像度(DPI)
    */
    public static int getScreenResolution() {
        return Toolkit.getDefaultToolkit().getScreenResolution();
    }

    /**
    * コンポーネントの位置を設定する<br/>
    * ・マウスの位置にコンポーネントの中心を置く<br/>
    * ・画面の外にはみ出すようなら、画面際に置く
    *
    * @param obj コンポーネント
    */
    public static void setLocationUnderMouse(Component obj) {
        PointerInfo info = MouseInfo.getPointerInfo();
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension size = tk.getScreenSize();

        int w = obj.getWidth();
        int h = obj.getHeight();

        Point loc = info.getLocation();
        loc.x = Math.min(Math.max(0, loc.x - w / 2), size.width - w);
        loc.y = Math.min(Math.max(0, loc.y - h / 2), size.height - h);

        obj.setLocation(loc);
    }

    /**
    * 実寸を画素値に変換する<br/>
    * ・解像度に0以下を指定すると、現在の画面の解像度を使用する
    *
    * @param dpi 解像度(DPI)
    * @param length 変換する長さ(0.1mm単位)
    * @return 画素値での長さ
    */
    public static int mmToPixel(int dpi, int length) {
        if (0 >= dpi) {
            dpi = Toolkit.getDefaultToolkit().getScreenResolution();
        }
        return (int) Math.round((double) (length * dpi) / 250.0);
    }

    /**
    * 画素値を実寸に変換する<br/>
    * ・解像度に0以下を指定すると、現在の画面の解像度を使用する
    *
    * @param dpi 解像度(DPI)
    * @param length 画素値の長さ
    * @return 実寸(0.1mm単位)
    */
    public static int pixelToMM(int dpi, int length) {
        if (0 >= dpi) {
            dpi = Toolkit.getDefaultToolkit().getScreenResolution();
        }

        return (int) Math.round((double) (length * 250) / dpi);
    }

    /**
    * 異なる解像度の画素値に変換する<br/>
    * ・解像度に0以下を指定すると、現在の画面の解像度を使用する
    *
    * @param srcDpi 変換前の解像度(DPI)
    * @param dstDpi 返還後の解像度(DPI)
    * @param length 長さ
    * @return 変換後の長さ
    */
    public static int pixelToPixel(int srcDpi, int dstDpi, int length) {
        if (srcDpi == dstDpi) {
            return length;
        }

        if (0 >= srcDpi) {
            srcDpi = Toolkit.getDefaultToolkit().getScreenResolution();
        }
        if (0 >= dstDpi) {
            dstDpi = Toolkit.getDefaultToolkit().getScreenResolution();
        }

        //        return (int)Math.round((double)(length*dstDpi)/(double)srcDpi);
        return (int) Math.ceil((double) (length * dstDpi) / (double) srcDpi);
    }

    /**
    * 文字列からColorを生成する<br/>
    * [文字列の型式]<br/>
    * RRGGBB または #RRGGBB
    *
    * @param text 文字列
    * @return Color
    */
    public static Color getColor(String text) {
        if (6 > text.length() || 7 < text.length()) {
            return null;
        }
        if (7 == text.length()) {
            text = text.substring(1, text.length());
        }

        int[] part = new int[3];
        for (int i = 0; i < 6; i += 2) {
            String val = text.substring(i, i + 2);
            part[i / 2] = Integer.parseInt(val, 16);
        }

        Color color = new Color(part[0], part[1], part[2]);

        return color;
    }

    /**
    * Colorの文字列を生成する
    * [文字列の型式]<br/>
    * RRGGBB
    *
    * @param color
    * @return 色の文字列, colorがnullの場合は空文字
    */
    public static String colorString(Color color) {
        if (null == color) {
            return "";
        }
        return String.format("%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    // 配列の深い複製

    /**
    * 配列の深い複製<br/>
    *
    * ・各要素のコピーコンストラクタを呼び出して複製する<br/>
    * 　{@code deepCopy(Arrays.asList(src), Arrays.asList(dst))}と同じ<br/>
    * ・複製元と複製先との長さが異なる場合は、短い方の要素まで複製する<br/>
    * ・例外が発生したらlogExceptionを使ってログに出力する<br/>
    *
    * @param src 複製元
    * @param dst 複製先
    * @return true=複製が成功 false=失敗
    * @throws IllgalArgumentException 引数がnull、引数のクラスが異なる、
    */
    public static <T> boolean deepCopy(T[] src, T[] dst) {
        return deepCopy(Arrays.asList(src), Arrays.asList(dst));
    }

    /**
    * Collectionの深い複製<br/>
    *
    * ・各要素のコピーコンストラクタで複製する<br/>
    * ・複製元の長さが0の場合は、複製先がクリアされる<br/>
    * ・例外が発生したらlogExceptionを使ってログに出力する
    *
    * @param src 複製元
    * @param dst 複製先
    * @return true=複製が成功 false=失敗
    * @throws IllgalArgumentException 引数がnull、引数のクラスが異なる、
    */
    public static <T> boolean deepCopy(Collection<T> src, Collection<T> dst) {
        if (null == src || null == dst || false == src.getClass().equals(dst.getClass())) {
            throw new IllegalArgumentException("引数がnull、または、クラスが異なる");
        }
        if (0 == src.size()) {
            dst.clear();
            return true;
        }

        dst.clear();

        for (T obj : src) {
            try {
                Class<?> classObj = obj.getClass();
                Constructor<? extends Object> constructor = classObj.getConstructor(classObj);

                // Tのコピーコンストラクタを使うから、キャストは正しい
                @SuppressWarnings("unchecked")
                T ret = (T) constructor.newInstance(obj);
                dst.add(ret);
            } catch (Exception ex) {
                Util.logException(ex);
                return false;
            }
        }

        return true;
    }

    /**
    * Collectionの深い複製<br/>
    *
    * ・各要素のclone()で複製する<br/>
    * 　{@code deepCopyByMethod("clone", Arrays.asList(src), Arrays.asList(dst))}と同じ<br/>
    * ・複製元の長さが0の場合は、複製先がクリアされる<br/>
    * ・例外が発生したらlogExceptionを使ってログに出力する
    *
    * @param src 複製元
    * @param dst 複製先
    * @return true=複製が成功 false=失敗
    * @throws IllgalArgumentException 引数がnull、引数のクラスが異なる、
    */
    public static <T extends Cloneable> boolean deepCopyByClone(T[] src, T[] dst) {
        return deepCopyByMethod("clone", src, dst);
    }

    /**
    * Collectionの深い複製<br/>
    *
    * ・指定されたメソッドで各要素を複製する<br/>
    * 　{@code deepCopyByMethod(copy, Arrays.asList(src), Arrays.asList(dst))}と同じ<br/>
    * ・複製元の長さが0の場合は、複製先がクリアされる<br/>
    * ・例外が発生したらlogExceptionを使ってログに出力する
    *
    * @param copy 複製に使う引数なし、返り値がTのメソッド名
    * @param src 複製元
    * @param dst 複製先
    * @return true=複製が成功 false=失敗
    * @throws IllgalArgumentException 引数がnull、引数のクラスが異なる、
    */
    @SuppressWarnings("unchecked")
    public static <T> boolean deepCopyByMethod(String copy, T[] src, T[] dst) {
        try {
            if (null == src || null == dst || false == src.getClass().equals(dst.getClass())) {
                throw new IllegalArgumentException("引数がnull、または、クラスが異なる");
            }
            if (0 == src.length) {
                return true;
            }

            for (int i = 0; i < src.length; i++) {
                T obj = src[i];
                Method method = obj.getClass().getMethod(copy);
                // メソッドの返り値の代入互換性チェック
                if (false == (method.getReturnType().isInstance(obj))) {
                    throw new IllegalArgumentException("複製元と複製先の要素のクラスが異なる");
                }
                dst[i] = (T) method.invoke(obj);
            }
        } catch (Exception ex) {
            Util.logException(ex);
            return false;
        }

        return true;
    }

    /**
    * Collectionの深い複製<br/>
    *
    * ・各要素のclone()で複製する<br/>
    * 　{@code deepCopyByMethod("clone", src, dst)}と同じ<br/>
    * ・複製元の長さが0の場合は、複製先がクリアされる<br/>
    * ・例外が発生したらlogExceptionを使ってログに出力する
    *
    * @param src 複製元
    * @param dst 複製先
    * @return true=複製が成功 false=失敗
    * @throws IllgalArgumentException 引数がnull、引数のクラスが異なる、
    */
    public static <T extends Cloneable> boolean deepCopyByClone(Collection<T> src, Collection<T> dst) {
        return deepCopyByMethod("clone", src, dst);
    }

    /**
    * Collectionの深い複製<br/>
    *
    * ・指定されたメソッドで各要素を複製する<br/>
    * 　メソッドは返り値が複製するオブジェクトのクラスでなければならない<br/>
    * ・複製元の長さが0の場合は、複製先がクリアされる<br/>
    * ・例外が発生したらlogExceptionを使ってログに出力する
    *
    * @param copy 複製に使う引数なし、返り値がTのメソッド名
    * @param src 複製元
    * @param dst 複製先
    * @return true=複製が成功 false=失敗
    * @throws IllgalArgumentException 引数がnull、引数のクラスが異なる、
    */
    public static <T> boolean deepCopyByMethod(String copy, Collection<T> src, Collection<T> dst) {
        try {
            if (null == src || null == dst || false == src.getClass().equals(dst.getClass())) {
                throw new IllegalArgumentException("引数がnull、または、クラスが異なる");
            }
            if (0 == src.size()) {
                dst.clear();
                return true;
            }

            dst.clear();

            for (T obj : src) {
                Method method = obj.getClass().getMethod(copy);
                if (null == method) {
                    throw new IllegalArgumentException(String.format("複製に指定されたメソッド%sが見つからない", copy));
                }
                // メソッドの返り値の代入互換性チェック
                if (false == (method.getReturnType().isInstance(obj))) {
                    throw new IllegalArgumentException("複製元と複製先の要素のクラスが異なる");
                }
                @SuppressWarnings("unchecked")
                T ret = (T) method.invoke(obj);
                dst.add(ret);
            }
        } catch (Exception ex) {
            Util.logException(ex);
            return false;
        }

        return true;
    }

    // ファイル関係

    /**
    * 作業フォルダのファイルを取得する
    *
    * @param fileName ファイル名
    * @return File 作業フォルダが見つからない場合はnull
    */
    public static File getFile(String fileName) {
        Properties prop = System.getProperties();
        String folder = prop.getProperty(FOLDER);
        if (null == folder) {
            return null;
        }

        File file = new File(folder + File.separator + fileName);
        return file;
    }

    /**
    * 読み取り用にファイルを開く<br/>
    * [使用例]
    * <pre>
    * FileNameExtensionFilter[] extList = {new FileNameExtensionFilter("csvファイル", "csv", "dat")};
    * File[] files = Util.openFile(fileName, extList, true);
    * </pre>
    *
    * @param fileName ダイアログに表示するファイル名。nullの場合は前回選んだファイル名
    * @param extList 拡張子
    * @param multi true=複数選択可　false=不可
    * @return 選択したファイル、失敗した場合、キャンセルした場合はnull
    */
    public static File[] openFiles(String fileName, FileNameExtensionFilter[] extList, boolean multi) {
        Util.initDlg();

        if (null == extList) {
            dlg.setFileFilter(null);
        } else {
            int count = extList.length;
            for (int i = 0; i < count; i++) {
                dlg.addChoosableFileFilter(extList[i]);
            }
            dlg.setFileFilter(extList[0]);
        }

        if (null != fileName) {
            dlg.setSelectedFile(new File(fileName));
        }
        dlg.setMultiSelectionEnabled(multi);
        if (JFileChooser.CANCEL_OPTION == dlg.showOpenDialog(null)) {
            return null;
        }

        return dlg.getSelectedFiles();
    }

    /**
    * ファイルを開く<br/>
    * [使用例]
    * <pre>
    * FileNameExtensionFilter[] extList = {new FileNameExtensionFilter("csvファイル", "csv", "dat")};
    * File file = Util.selectFile(fileName, "sample.csv", extList, false);
    * </pre>
    *
    * @param fileName ダイアログに表示するファイル名。nullの場合は前回選んだファイル名かdefFileNameを使う。どちらもない場合は表示しない。
    * @param defFileName fileNameがnullの場合のデフォルトファイル名。指定しない場合はnull
    * @param extList 拡張子リスト、指定しない場合はnull
    * @param toSave true=保存用に開く false=読み取り用に開く
    * @return 選択したファイル、失敗した場合、キャンセルした場合はnull
    */
    public static File selectFile(String fileName, String defFileName, FileNameExtensionFilter[] extList,
            boolean toSave) {
        Util.initDlg();

        FileFilter[] filters = dlg.getChoosableFileFilters();
        for (FileFilter f : filters) {
            dlg.removeChoosableFileFilter(f);
        }
        if (null != extList) {
            int count = extList.length;
            for (int i = 0; i < count; i++) {
                dlg.addChoosableFileFilter(extList[i]);
            }
            dlg.setFileFilter(extList[0]);
        }

        File file = null;
        if (null != fileName) {
            file = new File(fileName);
            if (file.isDirectory()) {
                dlg.setCurrentDirectory(file);
            } else {
                dlg.setSelectedFile(file);
            }
        } else {
            file = dlg.getSelectedFile();
            if (null == file) {
                if (null != defFileName && false == defFileName.isEmpty()) {
                    FileNameExtensionFilter filter = (FileNameExtensionFilter) dlg.getFileFilter();
                    String[] exts = filter.getExtensions();
                    if (0 < exts.length) {
                        String name = String.format("%s.%s", defFileName, exts[0]);
                        file = new File(name);
                        dlg.setSelectedFile(file);
                    }
                }
            } else {
                FileNameExtensionFilter filter = (FileNameExtensionFilter) dlg.getFileFilter();
                String[] exts = filter.getExtensions();
                if (0 < exts.length) {
                    String name = file.getName();
                    int index = name.lastIndexOf(".") + 1;
                    name = name.substring(0, index) + exts[0];
                    file = new File(name);
                    dlg.setSelectedFile(file);
                }
            }
        }

        if (toSave) {
            if (JFileChooser.CANCEL_OPTION == dlg.showSaveDialog(null)) {
                return null;
            }

            file = dlg.getSelectedFile();

            FileNameExtensionFilter filter = (FileNameExtensionFilter) dlg.getFileFilter();
            String[] exts = filter.getExtensions();
            if (0 < exts.length) {
                String name = file.getName();
                int index = name.lastIndexOf("." + exts[0]);
                if (0 > index || index != (name.length() - exts[0].length() - 1)) {
                    File tmp = new File(file.getParent() + File.separator + name + "." + exts[0]);
                    file = tmp;
                }
            }

            if (file.exists()) {
                String message = String.format("%sを上書きしますか？", file.getName());
                if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(null, message, "確認",
                        JOptionPane.YES_NO_OPTION)) {
                    return null;
                }
            }
        } else {
            if (JFileChooser.CANCEL_OPTION == dlg.showOpenDialog(null)) {
                return null;
            }

            file = dlg.getSelectedFile();
        }

        return file;
    }

    /**
    * ブラウザでURLを開く
    *
    * @param uriString URL
    */
    public static void showWeb(String uriString) {
        Desktop desktop = Desktop.getDesktop();
        try {
            URI uri = new URI(uriString);
            desktop.browse(uri);
        } catch (Exception ex) {
            Util.logException(ex);
        }
    }

    /**
    * 画像をBASE64の文字列から取得する
    *
    * @param mime MIMEタイプ
    * @param data BASE64エンコードされた画像データ
    * @return 画像, null=取得できない場合
    */
    public static BufferedImage decodeImage(String mime, String data) {
        byte[] b = Base64.getDecoder().decode(data);
        InputStream s = new ByteArrayInputStream(b);
        BufferedImage image = null;
        try {
            ImageInputStream is = ImageIO.createImageInputStream(s);
            Iterator<ImageReader> it = ImageIO.getImageReadersByMIMEType(mime);
            while (it.hasNext()) {
                ImageReader r = it.next();
                r.setInput(is);

                image = r.read(0);
                break;
            }
        } catch (IOException e) {
            Util.logException(e);
        }

        return image;
    }

    private static WaitBoard mdlg = null;

    public static void waitBoard(String title, Icon icon, String message, boolean show) {
        if (show) {
            if (null == mdlg) {
                mdlg = new WaitBoard();
            }
            Toolkit tk = Toolkit.getDefaultToolkit();
            Dimension size = tk.getScreenSize();
            mdlg.setMessage(title, icon, message);
            mdlg.setLocation((size.width - mdlg.getWidth()) / 2, (size.height - mdlg.getHeight()) / 2);
        }
        mdlg.setVisible(show);
    }

    /**
    * コマンドを実行する
    *
    * @param command コマンド
    * @param wait true=コマンド終了を待つ
    */
    public static void exec(String command, boolean wait) {
        Runtime runTime = Runtime.getRuntime();
        try {
            Process p = runTime.exec(command);
            if (wait) {
                p.waitFor();
            }
        } catch (Exception ex) {
            Util.logException(ex);
        }
    }

    // ログ出力、メッセージ表示

    /**
    * デバッグモードのON/OFF<br/>
    *
    * ・デバッグモードではログを標準出力に出力する
    *
    * @param flag true=デバッグモード false=通常モード
    */
    public static void setDebug(boolean flag) {
        debug = flag;
        if (debug) {
            Util.logInfo("DEBUG mode");
        }
    }

    private void writeln(String text) throws java.io.IOException {
        if (debug) {
            System.out.println(text);
        } else {
            if (null == this.fh) {
                initLog(Util.exePath(LOG_FILE));
            }
            LogRecord lr = new LogRecord(Level.INFO, text);
            this.fh.publish(lr);
        }
    }

    /**
    * 現在の日時を取得kする
    *
    * [形式]<br/>
    * YYYY-MM-DD HH24:mm:SS
    *
    * @return 日時の文字列
    */
    public static String now() {
        Calendar cal = Calendar.getInstance();
        return String.format("%4d-%02d-%02d %02d:%02d:%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
                cal.get(Calendar.SECOND));
    }

    private static String logFormat(LOG type, String fileName, int lineNo, String format, Object... args) {
        return String.format("%s %s:%d " + format, type.name(), fileName, lineNo, args);
    }

    private static void invokeLog(LOG type, String format, Object... args) {
        String text = (null == args ? format : String.format(format, args));
        Throwable obj = new Throwable();
        StackTraceElement[] trace = obj.getStackTrace();
        String line = logFormat(type, trace[2].getFileName(), trace[2].getLineNumber(), text);
        try {
            INSTANCE.writeln(line);
            ;
        } catch (Exception e) {
            System.err.println(line);
            System.err.println("Logging failed.");
            e.printStackTrace();
        }
    }

    private static void invokeLog(LOG type, Exception ex) {
        StackTraceElement[] trace = ex.getStackTrace();
        StringBuilder text = new StringBuilder(String.format("%s %s\nStack trace", ex.toString(), ex.getMessage()));
        for (int i = 0; i < trace.length; i++) {
            text.append("\n\t");
            text.append(trace[i]);
        }

        Throwable obj = new Throwable();
        trace = obj.getStackTrace();
        String line = logFormat(type, trace[2].getFileName(), trace[2].getLineNumber(), text.toString());
        try {
            INSTANCE.writeln(line);
            ;
        } catch (Exception e) {
            System.err.println(line);
            System.err.println("Logging failed.");
            e.printStackTrace();
        }
    }

    private static void invokeLog(LOG type, Exception ex, String format, Object... args) {
        String message = String.format(format, args);
        StackTraceElement[] trace = ex.getStackTrace();
        StringBuilder text = new StringBuilder(
                String.format("%s %s: %s\nStack trace", ex.toString(), ex.getMessage(), message));
        for (int i = 0; i < trace.length; i++) {
            text.append("\n\t");
            text.append(trace[i]);
        }

        Throwable obj = new Throwable();
        trace = obj.getStackTrace();
        String line = logFormat(type, trace[2].getFileName(), trace[2].getLineNumber(), text.toString());
        try {
            INSTANCE.writeln(line);
            ;
        } catch (Exception e) {
            System.err.println(line);
            System.err.println("Logging failed.");
            e.printStackTrace();
        }
    }

    /**
    * ログを出力する<br/>
    *
    * ・出力できない場合は標準エラー出力に出力する<br/>
    * [ログの形式]<br/>
    *  日時 ログのタイプ ファイル名:行番号 メッセージ
    *
    * @param type ログのタイプ。nullの場合はLOG.INFOで出力する
    * @param format 書式
    * @param args 引数
    */
    public static void log(LOG type, String format, Object... args) {
        invokeLog(type, format, args);
    }

    /**
    * 例外のログを出力する<br/>
    *
    * ・メッセージをgetMessage()で取得する<br/>
    * ・出力できない場合は標準エラー出力に出力する<br/>
    * [ログの形式]<br/>
    *  日時 ログのタイプ ファイル名:行番号 例外のメッセージ<br/>
    *  Stack trace<br/>
    *  スタックトレースの出力<br/>
    *
    * @param type ログのタイプ。nullの場合はLOG.INFO
    * @param ex 例外
    */
    public static void log(LOG type, Exception ex) {
        invokeLog(type, ex);
    }

    /**
    * 例外のメッセージを出力する<br/>
    *
    * ・getMessage()で取得してメッセージと合わせて出力する<br/>
    * ・出力できない場合は標準エラー出力に出力する<br/>
    * [ログの形式]<br/>
    *  日時 ログのタイプ ファイル名:行番号 メッセージ 例外のメッセージ<br/>
    *  Stack trace<br/>
    *  スタックトレースの出力<br/>
    *
    * @param type ログのタイプ。nullの場合はLOG.INFO
    * @param ex 例外
    * @param format 書式
    * @param args 引数
    */
    public static void log(LOG type, Exception ex, String format, Object... args) {
        String text = (null == args ? format : String.format(format, args));
        StackTraceElement[] trace = ex.getStackTrace();
        StringBuilder line = new StringBuilder(
                String.format("%s %s %s\nStack trace", text, ex.toString(), ex.getMessage()));
        for (int i = 0; i < trace.length; i++) {
            line.append("\n\t");
            line.append(trace[i]);
        }
        invokeLog(type, line.toString());
    }

    /**
    * LOG.INFOを指定してログを出力する<br/>
    *
    * ・出力できない場合は標準エラー出力に出力する
    * [ログの形式]<br/>
    *  日時 ログのタイプ ファイル名:行番号 メッセージ
    *
    * @param format 書式
    * @param args 引数
    */
    public static void logInfo(String format, Object... args) {
        invokeLog(LOG.INFO, format, args);
    }

    /**
    * LOG.WARNINGを指定してログを出力する<br/>
    *
    * ・出力できない場合は標準エラー出力に出力する
    * [ログの形式]<br/>
    *  日時 ログのタイプ ファイル名:行番号 メッセージ
    *
    * @param format 書式
    * @param args 引数
    */
    public static void logWarning(String format, Object... args) {
        invokeLog(LOG.WARNING, format, args);
    }

    /**
    * LOG.ERRORを指定してログを出力する<br/>
    *
    * ・出力できない場合は標準エラー出力に出力する
    * [ログの形式]<br/>
    *  日時 ログのタイプ ファイル名:行番号 メッセージ
    *
    * @param format 書式
    * @param args 引数
    */
    public static void logError(String format, Object... args) {
        invokeLog(LOG.ERROR, format, args);
    }

    /**
    * LOG.EXCEPTIONを指定してログを出力する<br/>
    *
    * ・出力できない場合は標準エラー出力に出力する
    * [ログの形式]<br/>
    *  日時 ログのタイプ ファイル名:行番号 メッセージ
    *
    * @param ex 例外
    */
    public static void logException(Exception ex) {
        invokeLog(LOG.EXCEPTION, ex);
    }

    public static void logException(Exception ex, String format, Object... args) {
        invokeLog(LOG.EXCEPTION, ex, format, args);
    }

    private static int showSelectDialog(String title, int messageType, String[] selections, int defIndex,
            boolean catchMouse, String format, Object... args) {
        String message = (null == args ? format : String.format(format, args));
        JOptionPane pane = new JOptionPane(message, messageType, JOptionPane.DEFAULT_OPTION, null, selections,
                selections[defIndex]);

        JDialog dlg = pane.createDialog(title);

        if (catchMouse) {
            PointerInfo info = MouseInfo.getPointerInfo();
            Point p = info.getLocation();
            p.x -= dlg.getWidth() / 2;
            p.y -= dlg.getHeight() / 2;
            dlg.setLocation(p);
        }
        dlg.setVisible(true);
        ;

        Object val = pane.getValue();
        for (int i = 0; i < selections.length; i++) {
            if (selections[i] == val) {
                return i;
            }
        }

        return -1;
    }

    private static int showQuestionDialog(String title, int messageType, int optionType, String format,
            Object... args) {
        return showQuestionDialog(title, messageType, optionType, true, format, args);
    }

    private static int showQuestionDialog(String title, int messageType, int optionType, boolean catchMouse,
            String format, Object... args) {
        String message = (null == args ? format : String.format(format, args));
        JOptionPane pane = new JOptionPane(message, messageType, optionType);

        JDialog dlg = pane.createDialog(title);

        if (catchMouse) {
            PointerInfo info = MouseInfo.getPointerInfo();
            Point p = info.getLocation();
            p.x -= dlg.getWidth() / 2;
            p.y -= dlg.getHeight() / 2;
            dlg.setLocation(p);
        }
        dlg.setVisible(true);
        ;

        Object val = pane.getValue();
        if (val instanceof Integer) {
            return ((Integer) val).intValue();
        }

        switch (optionType) {
        case JOptionPane.YES_NO_CANCEL_OPTION:
        case JOptionPane.OK_CANCEL_OPTION:
            return JOptionPane.CANCEL_OPTION;
        default:
            return JOptionPane.NO_OPTION;
        }
    }

    private static void showMessageDialog(String title, int messageType, String format, Object... args) {
        showMessageDialog(title, messageType, true, format, args);
    }

    private static void showMessageDialog(String title, int messageType, boolean catchMouse, String format,
            Object... args) {
        String message = (null == args ? format : String.format(format, args));
        JOptionPane pane = new JOptionPane(message, messageType);
        JDialog dlg = pane.createDialog(title);

        if (catchMouse) {
            PointerInfo info = MouseInfo.getPointerInfo();
            Point p = info.getLocation();
            p.x -= dlg.getWidth() / 2;
            p.y -= dlg.getHeight() / 2;
            dlg.setLocation(p);
        }
        dlg.setVisible(true);
        ;
    }

    /**
    * YES/NOのダイアログを表示する<br/>
    *
    * ・select1(message, true)と同じ
    *
    * @param format 書式
    * @param args 引数
    * @return true=YES、false=NO
    */
    public static boolean select1(String format, Object... args) {
        return select1(true, format, args);
    }

    /**
    * YES/NOのダイアログを表示する
    *
    * @param catchMouse true=マウス位置に表示する false=画面の中心に表示する
    * @param format 書式
    * @param args 引数
    * @return true=YES、false=NO
    */
    public static boolean select1(boolean catchMouse, String format, Object... args) {
        return (showQuestionDialog("選択", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, catchMouse, format,
                args) == JOptionPane.YES_OPTION);
    }

    /**
    * YES/NO/CANCELのダイアログを表示する<br/>
    *
    * ・選択結果はJOptinPaneのYES_OPTION/NO_OPTION/CANCEL_OPTIONで返す。<br/>
    * ・select2(message, true)と同じ
    *
    * @param format 書式
    * @param args 引数
    * @return 選択結果
    */
    public static int select2(String format, Object... args) {
        return select2(true, format, args);
    }

    /**
    * YES/NO/CANCELのダイアログを表示する<br/>
    *
    * ・選択結果はJOptinPaneのYES_OPTION/NO_OPTION/CANCEL_OPTIONで返す。
    *
    * @param catchMouse true=マウス位置に表示する false=画面の中心に表示する
    * @param format 書式
    * @param args 引数
    * @return 選択結果
    */
    public static int select2(boolean catchMouse, String format, Object... args) {
        return showQuestionDialog("選択", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, catchMouse,
                format, args);
    }

    /**
    * 選択肢から選ぶダイアログを表示する
    *
    * @param title タイトル
    * @param selections 選択肢のテキスト
    * @param defIndex デフォルトの選択肢の番号
    * @param catchMouse true=マウス位置に表示する false=画面の中心に表示する
    * @param format 書式
    * @param args 引数
    * @return 選択結果
    */
    public static int select3(String title, String[] selections, int defIndex, boolean catchMouse, String format,
            Object... args) {
        return showSelectDialog(title, JOptionPane.QUESTION_MESSAGE, selections, defIndex, catchMouse, format, args);
    }

    /**
    * 入力するダイアログを表示する
    *
    * @param title タイトル
    * @param defValue 入力値の初期値
    * @param catchMouse true=マウス位置に表示する false=画面の中心に表示する
    * @param format 書式
    * @param args 引数
    * @return 入力文字列 null=取り消された場合
    */
    public static String select4(String title, String defValue, boolean catchMouse, String format, Object... args) {
        String message = (null == args ? format : String.format(format, args));
        JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        pane.setWantsInput(true);
        if (null != defValue) {
            pane.setInitialSelectionValue(defValue);
        }
        JDialog dlg = pane.createDialog(title);

        if (catchMouse) {
            PointerInfo info = MouseInfo.getPointerInfo();
            Point p = info.getLocation();
            p.x -= dlg.getWidth() / 2;
            p.y -= dlg.getHeight() / 2;
            dlg.setLocation(p);
        }
        dlg.setVisible(true);
        ;

        if (JOptionPane.CANCEL_OPTION == (int) pane.getValue()) {
            return null;
        }

        return (String) pane.getInputValue();
    }

    /**
    * Abountダイアログを表示する
    *
    * @param icon アイコン
    * @param format 書式
    * @param args 引数
    */
    public static void about(Icon icon, String format, Object... args) {
        String message = (null == args ? format : String.format(format, args));
        JEditorPane area = new JEditorPane("text/html", message);
        area.setOpaque(false);
        area.setEditable(false);
        area.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (MouseEvent.MOUSE_CLICKED == e.getInputEvent().getID()) {
                    Desktop desktop = Desktop.getDesktop();
                    URI uri;
                    try {
                        uri = new URI(e.getURL().toString());
                        desktop.browse(uri);
                    } catch (URISyntaxException | IOException e1) {
                        Util.logException(e1);
                    }
                }
            }
        });
        JOptionPane pane = null;
        if (null == icon) {
            pane = new JOptionPane(area, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION);
        } else {
            pane = new JOptionPane(area, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, icon);
        }
        JDialog dlg = pane.createDialog("About");

        PointerInfo info = MouseInfo.getPointerInfo();
        Point p = info.getLocation();
        p.x -= dlg.getWidth() / 2;
        p.y -= dlg.getHeight() / 2;
        dlg.setLocation(p);

        dlg.setVisible(true);
        ;
    }

    /**
    * OKのダイアログを表示する
    *
    * @param format 書式
    * @param args 引数
    */
    public static void notify(String format, Object... args) {
        showMessageDialog("確認", JOptionPane.PLAIN_MESSAGE, format, args);
    }

    /**
    * OK/CANCELダイアログを表示する
    *
    * @param format 書式
    * @param args 引数
    * @return true=OK false=CANCEL
    */
    public static boolean confirm(String format, Object... args) {
        return (showQuestionDialog("確認", JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, format,
                args) == JOptionPane.OK_OPTION);
    }

    /**
    * LOG.INFOのログを出力してメッセージを表示する<br/>
    *
    * ・JOptionPane.showMessageDialogでメッセージを表示する
    *
    * @param format 書式
    * @param args 引数
    */
    public static void info(String format, Object... args) {
        logInfo(format, args);
        showMessageDialog("情報", JOptionPane.INFORMATION_MESSAGE, format, args);
    }

    /**
    * LOG.WARNINGのログを出力してメッセージを表示する<br/>
    *
    * ・JOptionPane.showMessageDialogでメッセージを表示する
    *
    * @param format 書式
    * @param args 引数
    */
    public static void warning(String format, Object... args) {
        logWarning(format, args);
        showMessageDialog("警告", JOptionPane.WARNING_MESSAGE, format, args);
    }

    /**
    * LOG.ERRORのログを出力してメッセージを表示する<br/>
    *
    * ・JOptionPane.showMessageDialogでメッセージを表示する
    *
    * @param format 書式
    * @param args 引数
    */
    public static void error(String format, Object... args) {
        logError(format, args);
        showMessageDialog("エラー", JOptionPane.ERROR_MESSAGE, format, args);
    }

    /**
    * LOG.EXCEPTIONのログを出力してメッセージを表示する<br/>
    *
    * ・JOptionPane.showMessageDialogでメッセージを表示する
    *
    * @param ex
    * @param format 書式
    * @param args 引数
    */
    public static void exception(Exception ex, String format, Object... args) {
        log(LOG.EXCEPTION, ex, format, args);
        showMessageDialog("エラー", JOptionPane.ERROR_MESSAGE, format, args);
    }
}
