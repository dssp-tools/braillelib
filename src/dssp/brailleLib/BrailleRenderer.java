package dssp.brailleLib;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;

import dssp.brailleLib.BrailleInfo.EXTRA;
import dssp.brailleLib.BrailleInfo.TABLE;

/**
 * 点字を描画する
 *
 * @author DSSP/Minoru Yagi
 *
 */
public class BrailleRenderer {
    public static final int DEFAULT_DOT_SIZE = 4;
    protected int DOT_SIZE = DEFAULT_DOT_SIZE;
    protected int SPACE_X;
    protected int SPACE_Y;
    protected int BOX_SPACE;
    protected int LINE_SPACE;

    protected Color bgColor = Color.LIGHT_GRAY;
    protected Color fgColor = Color.BLACK;

    //    List<BrailleInfo> brailleListCache;
    //    List<BrailleBox> boxListCache;

    /**
    * 描画モード
    *
    * @author yagi
    *
    */
    public static enum MODE {
        /**
        * 表示
        */
        DISPLAY,
        /**
        * 印刷
        */
        PRINT;
    }

    protected MODE mode = MODE.DISPLAY;

    protected BrailleRenderer() {
    }

    /**
    * インスタンスを生成する
    *
    * @return インスタンス
    */
    public static BrailleRenderer newInstance() {
        return new BrailleRenderer();
    }

    /**
    * 描画モードを設定する
    *
    * @param mode 描画モード
    */
    public void setMode(MODE mode) {
        this.mode = mode;
    }

    /**
    * 描画モードを取得する
    *
    * @return 描画モード
    */
    public MODE getMode() {
        return this.mode;
    }

    /**
    * 点の直径を取得する
    *
    * @return 点の直径
    */
    public int getDotSize() {
        return this.DOT_SIZE;
    }

    /**
    * 点の直径を設定する
    *
    * @param size 点の直径
    */
    public void setDotSize(int size) {
        this.DOT_SIZE = size;
    }

    /**
    * 点の横方向の間隔
    *
    * @return 間隔
    */
    public double getDotSpaceX() {
        return this.SPACE_X;
    }

    /**
    * 点の横方向の間隔
    *
    * @param size 間隔
    */
    public void setDotSpaceX(int size) {
        this.SPACE_X = size;
    }

    /**
    * 点の縦方向の間隔
    *
    * @return 間隔
    */
    public double getDotSpaceY() {
        return this.SPACE_Y;
    }

    /**
     * 点の縦方向の間隔
     *
     * @param size 間隔
     */
    public void setDotSpaceY(int size) {
        this.SPACE_Y = size;
    }

    /**
    * マスの幅を取得する
    *
    * @return マスの幅
    */
    public int getBoxWidth() {
        return this.SPACE_X;
    }

    /**
    * マスの高さを取得する
    *
    * @return マスの高さ
    */
    public int getBoxHeight() {
        return this.SPACE_Y * 2;
    }

    /**
    * マスの間隔を取得する
    *
    * @return マスの間隔
    */
    public int getBoxSpace() {
        return this.BOX_SPACE;
    }

    /**
    * マスの間隔
    *
    * @param size 間隔
    */
    public void setBoxSpace(int size) {
        this.BOX_SPACE = size;
    }

    /**
    * マスの行間を取得する
    *
    * @return マスの行間
    */
    public int getLineSpace() {
        return this.LINE_SPACE;
    }

    public void setLineSpace(int size) {
        this.LINE_SPACE = size;
    }

    /**
    * 行列の行の間隔
    *
    * @return 行の間隔
    */
    public int getRowSpace() {
        return this.LINE_SPACE;
    }

    /**
    * 行列の列の間隔
    *
    * @return 列の間隔
    */
    public int getColumnSpace() {
        return this.getBoxSpace() * 3;
    }

    /**
    * マスの背景色を取得する
    *
    * @return 背景色
    */
    public Color getBgColor() {
        return this.bgColor;
    }

    /**
    * マスの背景色を設定する
    *
    * @param color 背景色
    */
    public void setBgColor(Color color) {
        this.bgColor = (null == color ? Color.LIGHT_GRAY : color);
    }

    /**
    * 点の色を取得する
    *
    * @return 点の色
    */
    public Color getFgColor() {
        return this.fgColor;
    }

    /**
    * 点の色を設定する
    *
    * @param color 点の色
    */
    public void setFgColor(Color color) {
        this.fgColor = (null == color ? Color.BLACK : color);
    }

    /**
    * 点字の表示幅を取得する<br>
    *
    * ・表示幅はマスとマスの間隔の合計
    *
    * @param braile BrailleInfo
    * @param withExtra true=符号を含む fale=含まない
    * @return 表示幅
    */
    public int getWidth(BrailleInfo braile, boolean withExtra) {
        int bW = this.getBoxWidth();
        int bS = this.getBoxSpace();
        int extraWidth = 0;
        if (withExtra) {
            for (EXTRA extra : BrailleInfo.EXTRA.values()) {
                if (braile.haveExtra(extra)) {
                    extraWidth += bW + bS;
                }
            }
        }
        int nBox = braile.getBoxCount();
        return nBox * bW + (nBox - 1) * bS + extraWidth;
    }

    /**
    * １点字のマスの四角のリストを取得する
    *
    * @param braille BrailleInfo
    * @param index BrilleInfoのリスト内の番号
    * @param x 描画位置のX座標
    * @param y 描画位置のY座標
    * @param drawExtra true=符号を含む false=含まない
    * @return 四角のリスト
    */
    public List<BrailleBox> getBoxList(BrailleInfo braille, int index, int x, int y, boolean drawExtra) {
        int bX = x;
        int bY = y;
        int bW = this.getBoxWidth();
        int bH = this.getBoxHeight();
        int bS = this.getBoxSpace();
        List<BrailleBox> rectList = Util.newArrayList();
        if (BrailleInfo.LINEBREAK == braille) {
            if (drawExtra) {
                BrailleBox box = BrailleBox.getLineBreak(bX, bY, 0, this.getBoxHeight(), index);
                rectList.add(box);
            }
            return rectList;
        } else if (BrailleInfo.SPACE == braille || BrailleInfo.UNKNOWN == braille) {
            rectList.add(new BrailleBox(bX, bY, bW, bH, index, null));
            return rectList;
        }
        if (drawExtra) {
            for (EXTRA extra : BrailleInfo.EXTRA.values()) {
                if (braille.haveExtra(extra)) {
                    BrailleInfo eb = braille.getExtra(extra);
                    if (null != eb) {
                        BrailleBox box = new BrailleBox(bX, bY, bW, bH, index, eb.getBox(0));
                        rectList.add(box);
                        bX += bW + bS;
                    }
                }
            }
        }

        int nBox = braille.getBoxCount();
        if (0 == nBox) {
            //            rectList.add(new BrailleBox(bX, bY, bW, bH));
        } else {
            for (int i = 0; i < nBox; i++) {
                BrailleBox box = new BrailleBox(bX, bY, bW, bH, index, braille.getBox(i));
                rectList.add(box);
                bX += bW + bS;
            }
        }
        return rectList;
    }

    /**
    * マスの四角のリストを取得する
    *
    * @param brailleList 点字のリスト(符号も含む)
    * @param bX 描画位置のX座標
    * @param bY 描画位置のY座標
    * @param drawExtra true=符号を含む false=含まない
    * @return マスの領域のリスト
    */
    public List<BrailleBox> getBoxList(List<BrailleInfo> brailleList, int bX, int bY, boolean drawExtra) {
        //        if (null != this.boxListCache && this.brailleListCache.equals(brailleList))
        //        {
        //            return this.boxListCache;
        //        }
        return this.drawBraille(null, brailleList, bX, bY, drawExtra);
    }

    /**
    * マスを囲む四角を取得する<br>
    * {@link getBoxList(List<BrailleInfo> brailleList, int bX, int bY, boolean drawExtra)}を呼んだ後、各マスを合わせた四角と同じ
    *
    * @param brailleList 点字のリスト(符号も含む)
    * @param bX 描画位置のX座標
    * @param bY 描画位置のY座標
    * @param drawExtra true=符号を含む false=含まない
    * @return マスを囲む四角
    */
    public Rectangle getBound(List<BrailleInfo> brailleList, int bX, int bY, boolean drawExtra) {
        List<BrailleBox> boxList = this.getBoxList(brailleList, bX, bY, drawExtra);
        if (0 == boxList.size()) {
            return null;
        }
        BrailleBox b = boxList.get(0);
        Rectangle bound = new Rectangle(b.x, b.y, b.width, b.height);
        for (BrailleBox box : boxList) {
            bound.add(box);
        }

        return bound;
    }

    public void drawUnknown(Graphics g, BrailleBox b) {
        g.setColor(this.getFgColor());
        g.fillRect(b.x, b.y, b.width + this.getDotSize(), b.height + this.getDotSize());
    }

    /**
    * １点字を描画する<br>
    * ・gがnullなら描画しないで、マウの領域のリストを計算するだけと同じ)
    *
    * @param g Graphics
    * @param braille BrailleInfo
    * @param index BrailleInfoのリスト内の番号
    * @param bX 描画位置のX座標
    * @param bY 描画位置のY座標
    * @param drawExtra true=符号を表示する<br>false=表示しない
    * @return マスの領域のリスト
    * @see #getBoxList(BrailleInfo, int, int, boolean)
    */
    public List<BrailleBox> drawBraille(Graphics g, BrailleInfo braille, int index, int bX, int bY, boolean drawExtra) {
        List<BrailleBox> boxList = this.getBoxList(braille, index, bX, bY, drawExtra);

        if (braille == BrailleInfo.UNKNOWN && null != g) {
            this.drawUnknown(g, boxList.get(0));
            return boxList;
        }

        int boxIndex = 0;
        if (drawExtra) {
            for (EXTRA extra : BrailleInfo.EXTRA.values()) {
                if (braille.haveExtra(extra)) {
                    BrailleInfo eb = braille.getExtra(extra);
                    if (null != eb) {
                        Rectangle rect = boxList.get(boxIndex);
                        boxIndex++;
                        BrailleInfo code = braille.getExtra(extra);
                        if (null == code) {
                            rect = new Rectangle(0, bY, this.getBoxWidth(), this.getBoxHeight());
                            rect.x = bX + boxIndex * (this.getBoxWidth() + this.getBoxSpace());
                            g.setColor(Color.BLACK);
                            g.fillRect(rect.x, rect.y, rect.width, rect.height);
                            //                        this.drawDots(g, null, rect);
                        } else {
                            this.drawDots(g, code.getBox(0), rect);
                        }
                    }
                }
            }
        }

        int nBox = braille.getBoxCount();
        if (0 == nBox) {
            //            this.drawDots(g, null, rect);
        } else {
            for (int i = 0; i < nBox; i++) {
                Rectangle rect = boxList.get(boxIndex);
                boxIndex++;

                int[] dots = braille.getBox(i);
                this.drawDots(g, dots, rect);
            }
        }
        return boxList;
    }

    /**
    * 点字を描画する
    *
    * @param g Graphics
    * @param brailleList 点字のリスト(符号も含む)
    * @param bX 描画位置のX座標
    * @param bY 描画位置のY座標
    * @return マスの領域のリスト
    */
    public List<BrailleBox> drawBraille(Graphics g, List<BrailleInfo> brailleList, int bX, int bY, boolean drawExtra) {
        Rectangle bound = new Rectangle(bX, bY, 0, 0);
        List<BrailleBox> boxList = Util.newArrayList();

        this.drawLine(g, brailleList, 0, bound, boxList, drawExtra);

        //        this.brailleListCache = brailleList;
        //        this.boxListCache = boxList;

        return boxList;
    }

    protected int drawLine(Graphics g, List<BrailleInfo> brailleList, int start, Rectangle bound,
            List<BrailleBox> boxList, boolean drawExtra) {
        Rectangle charBound = new Rectangle(bound);
        int index;
        for (index = start; index < brailleList.size(); index++) {
            BrailleInfo braille = brailleList.get(index);

            if (braille.haveTable(BrailleInfo.TABLE.TABLE_OPEN)) {
                index = this.drawTable(g, brailleList, boxList, index, charBound, drawExtra);

                bound.add(charBound);
                charBound.setBounds(bound.x + bound.width, bound.y, 0, 0);
            } else {
                List<BrailleBox> list = this.drawBraille(g, braille, index, charBound.x, charBound.y, drawExtra);

                boxList.addAll(list);

                for (Rectangle rect : list) {
                    charBound.add(rect);
                }

                bound.add(charBound);

                if (braille.isLineBreak()) {
                    charBound.x = bound.x;
                    charBound.y = bound.y + bound.height + this.getLineSpace();
                } else {
                    charBound.x += charBound.width + this.getBoxSpace();
                    charBound.width = charBound.height = 0;
                }
            }
        }

        return index;
    }

    protected int drawTable(Graphics g, List<BrailleInfo> brailleList, List<BrailleBox> boxList, int start,
            Rectangle tableBound, boolean drawExtra) {
        // 各行の高さ、各列の幅を計算する
        this.layoutTable(brailleList, start, tableBound);

        // 描画する
        int index = this.drawTableLayouted(g, brailleList, boxList, start, tableBound, drawExtra);

        return index;
    }

    // 入れ子になったテーブルの行の高さ、列の幅を記憶する
    protected Map<Integer, Map<Integer, Rectangle>> colBoxMapCashe = Util.newHashMap();
    protected Map<Integer, Map<Integer, Rectangle>> rowBoxMapCache = Util.newHashMap();

    protected Map<Integer, Integer> colSpaceCache = Util.newHashMap();
    protected Map<Integer, Integer> rowSpaceCache = Util.newHashMap();

    /**
    * 各行の高さ、各列の幅を計算する
    *
    * @param brailleList
    * @param start
    * @param tableBound
    * @return TABLE.TABLE_CLOSEのbrailleの番号
    */
    protected int layoutTable(List<BrailleInfo> brailleList, int start, Rectangle tableBound) {
        int colSpace = this.getBoxSpace();
        int rowSpace = this.getLineSpace();
        BrailleInfo.TABLE_OPTION[] names = { BrailleInfo.TABLE_OPTION.FRAME, BrailleInfo.TABLE_OPTION.ROWLINES,
                BrailleInfo.TABLE_OPTION.COLUMNLINES };
        for (BrailleInfo.TABLE_OPTION name : names) {
            BrailleInfo braille = brailleList.get(start);
            String val = braille.getTableOption(name);
            if (null != val) {
                // 罫線つきなので、行、列の間隔を広くする
                colSpace = this.getColumnSpace();
                rowSpace = this.getRowSpace();
                break;
            }
        }
        start++;

        Map<Integer, Rectangle> colBoxMap = Util.newHashMap();
        this.colBoxMapCashe.put(start, colBoxMap);

        Map<Integer, Rectangle> rowBoxMap = Util.newHashMap();
        this.rowBoxMapCache.put(start, rowBoxMap);

        Map<Integer, Map<Integer, Integer>> spanMap = Util.newHashMap();

        Rectangle rowBound = new Rectangle();
        Rectangle cellBound = new Rectangle();
        Rectangle charBound = new Rectangle();
        int nRow = 0;
        int nColumn = 0;
        int index;
        TABLE pos = null;
        for (index = start; index < brailleList.size(); index++) {
            BrailleInfo braille = brailleList.get(index);
            boolean drawable = true;
            for (BrailleInfo.TABLE key : BrailleInfo.TABLE.values()) {
                if (braille.haveTable(key)) {
                    pos = key;
                    switch (key) {
                    case TABLE_OPEN:
                        // セル内の表
                        index = this.layoutTable(brailleList, index, charBound);
                        drawable = false;
                        break;
                    case ROW_START:
                        if (0 == nRow) {
                            rowBound.setBounds(tableBound.x, tableBound.y, 0, 0);
                        } else {
                            rowBound.setBounds(tableBound.x, rowBound.y + rowBound.height + rowSpace, 0, 0);
                        }
                        charBound.setBounds(rowBound);
                        drawable = false;
                        nColumn = 0;
                        break;
                    case CELL_START:
                        // セルの結合を考慮して列番号を修正する
                        Map<Integer, Integer> spanList = spanMap.get(nRow);
                        if (null != spanList) // この行には結合したセルがある
                        {
                            Integer colSpan = spanList.get(nColumn);
                            if (null != colSpan) // このセルは上の行から結合している
                            {
                                // 結合しているセル分だけ列番号をスキップする
                                nColumn += colSpan;
                            }
                        }
                        if (0 == nColumn) {
                            if (0 == nRow) {
                                // この行列/表でのセルの間隔を記憶する
                                this.colSpaceCache.put(start, colSpace);
                                this.rowSpaceCache.put(start, rowSpace);
                            }
                            cellBound.setBounds(charBound.x, charBound.y, 0, 0);
                        } else {
                            cellBound.setBounds(cellBound.x + cellBound.width + colSpace, cellBound.y, 0, 0);
                        }
                        charBound.setBounds(cellBound);

                        // セルの結合を記憶する
                        int rowSpan = braille.getTableOption(BrailleInfo.TABLE_OPTION.ROWSPAN);
                        int colSpan = braille.getTableOption(BrailleInfo.TABLE_OPTION.COLUMNSPAN);
                        if (1 < rowSpan || 1 < colSpan) {
                            if (0 == rowSpan) {
                                rowSpan = 1;
                            }
                            if (0 == colSpan) {
                                colSpan = 1;
                            }
                            for (int i = nRow; i < (nRow + rowSpan); i++) {
                                spanList = spanMap.get(i);
                                if (null == spanList) {
                                    spanList = Util.newHashMap();
                                    spanMap.put(i, spanList);
                                }
                                spanList.put(nColumn, colSpan);
                            }
                        }

                        drawable = false;
                        break;
                    case TABLE_CLOSE:
                        drawable = false;
                        tableBound.width += this.getBoxSpace();
                        break;
                    case ROW_END:
                    case CELL_END:
                        drawable = false;
                        break;
                    default:
                    }
                    break;
                }
            }

            if (drawable) {
                List<BrailleBox> list = this.getBoxList(braille, index, charBound.x, charBound.y, false);

                for (Rectangle rect : list) {
                    charBound.add(rect);
                }

                if (pos == TABLE.ROW_START || pos == TABLE.CELL_END) {
                    rowBound.add(charBound);
                } else {
                    cellBound.add(charBound);
                }

                if (braille.isLineBreak()) {
                    charBound.setBounds(cellBound.x, cellBound.y + this.getBoxHeight() + this.getLineSpace(), 0, 0);
                } else {
                    charBound.setBounds(charBound.x + charBound.width + this.getBoxSpace(), charBound.y, 0, 0);
                }

                if (pos == TABLE.ROW_START) {
                    // カッコつきなので、行、列の間隔を広くする
                    colSpace = this.getColumnSpace();
                    rowSpace = this.getRowSpace();
                }
            }

            Rectangle rect;
            switch (pos) {
            case TABLE_OPEN: // 内部の行列、表をcellBoundに追加する
                cellBound.add(charBound);
                charBound.setBounds(charBound.x + charBound.width + this.getBoxSpace(), charBound.y, 0, 0);
                pos = TABLE.CELL_START;
                break;
            case TABLE_CLOSE:
                return index;
            case CELL_END:
                rowBound.add(cellBound);
                // 次のセルに列番号を変更する
                Map<Integer, Integer> spanList = spanMap.get(nRow);
                if (null == spanList) // この行には結合しているセルはない
                {
                    // 列の幅の最大値を記憶する
                    rect = colBoxMap.get(nColumn);
                    if (null == rect) {
                        colBoxMap.put(nColumn, (Rectangle) cellBound.clone());
                    } else if (cellBound.width > rect.width) {
                        rect.width = cellBound.width;
                    }
                    // 次のセルに移動する
                    nColumn++;
                } else // この行には結合しているセルがある
                {
                    Integer colSpan = spanList.get(nColumn);
                    if (null == colSpan) // このセル(列)は結合していない
                    {
                        // 列の幅の最大値を記憶する
                        rect = colBoxMap.get(nColumn);
                        if (null == rect) {
                            colBoxMap.put(nColumn, (Rectangle) cellBound.clone());
                        } else if (cellBound.width > rect.width) {
                            rect.width = cellBound.width;
                        }
                        // 次のセルに移動する
                        nColumn++;
                    } else // このセル(列)は統合している
                    {
                        // 結合した列の幅を調整する
                        rect = colBoxMap.get(nColumn);
                        if (null == rect) // この列には最大幅が記憶されていない
                        {
                            double w = Math.max(colSpan, cellBound.width - colSpace * (colSpan - 1));
                            int dw = (int) (Math.ceil(w / (double) colSpan));
                            Rectangle col = new Rectangle(cellBound.x, cellBound.y, dw, cellBound.height);
                            for (int i = nColumn; i < (nColumn + colSpan); i++) {
                                colBoxMap.put(i, col);
                                col = new Rectangle(col);
                                col.x += colSpace;
                            }
                        } else // この列には最大幅が記憶されている
                        {
                            // この列以後の最大幅の和を求める
                            int w = 0;
                            int cw = 0;
                            for (int i = nColumn; i < (nColumn + colSpan); i++) {
                                rect = colBoxMap.get(i);
                                if (0 < w) {
                                    w += colSpace;
                                }
                                w += rect.width;
                                cw += rect.width;
                            }
                            if (w < cellBound.width) // 最大幅の和がこのセルの幅より小さい
                            {
                                w = cellBound.width - colSpace * (colSpan - 1);
                                double r = 1.0 + (double) cw / (double) w; // 拡大率
                                for (int i = nColumn; i < (nColumn + colSpan); i++) {
                                    rect = colBoxMap.get(i);
                                    rect.width = (int) Math.ceil(r * (double) rect.width);
                                }
                            }
                        }

                        // 次のセルに移動する
                        nColumn += colSpan;
                    }
                }
                break;
            case ROW_END:
                tableBound.add(rowBound);
                // 行の高さの最大値を記憶する
                rect = rowBoxMap.get(nRow);
                if (null == rect) {
                    rowBoxMap.put(nRow, (Rectangle) rowBound.clone());
                } else if (rowBound.height > rect.height) {
                    rect.height = rowBound.height;
                }
                nRow++;
                break;
            default:
            }
        }

        return index;
    }

    protected int drawTableLayouted(Graphics g, List<BrailleInfo> brailleList, List<BrailleBox> boxList, int start,
            Rectangle tableBound, boolean drawExtra) {
        start++;

        int rowSpace = this.rowSpaceCache.get(start);
        int colSpace = this.colSpaceCache.get(start);

        Map<Integer, Rectangle> colBoxMap = this.colBoxMapCashe.get(start);
        Map<Integer, Rectangle> rowBoxMap = this.rowBoxMapCache.get(start);

        Map<Integer, Map<Integer, Integer>> spanMap = Util.newHashMap();

        // 行の開始位置を再計算する
        Rectangle prevRow = null;
        for (Rectangle row : rowBoxMap.values()) {
            if (null != prevRow) {
                row.y = prevRow.y + prevRow.height + rowSpace;
            }
            prevRow = row;
        }

        // 描画する
        Rectangle rowBound = new Rectangle();
        Rectangle cellBound = new Rectangle();
        Rectangle charBound = new Rectangle();
        int nRow = 0;
        int nColumn = 0;
        int index;
        Rectangle row;
        Rectangle column;
        TABLE pos = null;
        for (index = start; index < brailleList.size(); index++) {
            BrailleInfo braille = brailleList.get(index);
            boolean drawable = true;
            for (BrailleInfo.TABLE key : BrailleInfo.TABLE.values()) {
                if (braille.haveTable(key)) {
                    pos = key;
                    switch (key) {
                    case TABLE_OPEN: // 内部の行列、表
                        index = this.drawTableLayouted(g, brailleList, boxList, index, charBound, drawExtra);
                        drawable = false;
                        break;
                    case ROW_START:
                        row = rowBoxMap.get(nRow);
                        rowBound.setBounds(tableBound.x, row.y, 0, 0);
                        charBound.setBounds(rowBound);
                        drawable = false;
                        nColumn = 0;
                        break;
                    case CELL_START:
                        // セルの結合を考慮して列番号を修正する
                        Map<Integer, Integer> spanList = spanMap.get(nRow);
                        if (null != spanList) // この行には結合したセルがある
                        {
                            Integer colSpan = spanList.get(nColumn);
                            if (null != colSpan) // このセルは上の行から結合している
                            {
                                // 結合しているセル分だけ列番号をスキップする
                                nColumn += colSpan;
                            }
                        }
                        column = colBoxMap.get(nColumn);
                        if (0 == nRow && 0 == nColumn) {
                            // 列の開始位置を再計算する
                            // カッコつきの場合もあるので、最初のセルが始まるまで位置が確定しない
                            column.x = charBound.x;
                            Rectangle prevCol = null;
                            for (Rectangle box : colBoxMap.values()) {
                                if (null != prevCol) {
                                    box.x = prevCol.x + prevCol.width + colSpace;
                                }
                                prevCol = box;
                            }
                        }
                        cellBound.setBounds(column.x, rowBound.y, 0, 0);
                        charBound.setBounds(cellBound);
                        // セルの結合を記憶する
                        int rowSpan = braille.getTableOption(BrailleInfo.TABLE_OPTION.ROWSPAN);
                        int colSpan = braille.getTableOption(BrailleInfo.TABLE_OPTION.COLUMNSPAN);
                        if (1 < rowSpan || 1 < colSpan) {
                            if (0 == rowSpan) {
                                rowSpan = 1;
                            }
                            if (0 == colSpan) {
                                colSpan = 1;
                            }
                            for (int i = nRow; i < (nRow + rowSpan); i++) {
                                spanList = spanMap.get(i);
                                if (null == spanList) {
                                    spanList = Util.newHashMap();
                                    spanMap.put(i, spanList);
                                }
                                spanList.put(nColumn, colSpan);
                            }
                        }
                        drawable = false;
                        break;
                    case TABLE_CLOSE:
                    case ROW_END:
                    case CELL_END:
                        drawable = false;
                        break;
                    default:
                    }
                    break;
                }
            }

            if (drawable) {
                List<BrailleBox> list = this.drawBraille(g, braille, index, charBound.x, charBound.y, drawExtra);

                if (0 < nRow) {
                    // 行間のカッコ部分
                    if (pos == TABLE.ROW_START) {
                        this.drawRowDots(g, charBound.x, charBound.y - rowSpace, true);
                    } else if (pos == TABLE.CELL_END) {
                        this.drawRowDots(g, charBound.x, charBound.y - rowSpace, false);
                    }
                }

                boxList.addAll(list);

                for (Rectangle rect : list) {
                    charBound.add(rect);
                }

                if (pos == TABLE.ROW_START || pos == TABLE.CELL_END) {
                    rowBound.add(charBound);
                } else {
                    cellBound.add(charBound);
                }

                if (braille.isLineBreak()) {
                    charBound.setBounds(cellBound.x, cellBound.y + this.getBoxHeight() + this.getLineSpace(), 0, 0);
                } else {
                    charBound.setBounds(charBound.x + charBound.width + this.getBoxSpace(), charBound.y, 0, 0);
                }
            }

            switch (pos) {
            case TABLE_OPEN: // 内部の行列、表をcellBoundに追加する
                cellBound.add(charBound);
                charBound.setBounds(charBound.x + charBound.width + this.getBoxSpace(), charBound.y, 0, 0);
                pos = TABLE.CELL_START;
                break;
            case TABLE_CLOSE:
                return index;
            case CELL_END:
                rowBound.add(cellBound);
                // 次のセルに列番号を変更する
                Map<Integer, Integer> spanList = spanMap.get(nRow);
                if (null == spanList) // この行には結合しているセルはない
                {
                    nColumn++;
                } else // この行に結合しているセルがある
                {
                    Integer colSpan = spanList.get(nColumn);
                    if (null == colSpan) // このセルは結合していない
                    {
                        nColumn++;
                    } else // このセルは統合している
                    {
                        nColumn += colSpan;
                    }
                }
                break;
            case ROW_END:
                tableBound.add(rowBound);
                nRow++;
                break;
            default:
            }
        }

        return index;
    }

    protected void plot(Graphics g, int x, int y) {
        if (null != g) {
            g.fillArc(x, y, this.DOT_SIZE, this.DOT_SIZE, 0, 360);
        }
    }

    private void drawDots(Graphics g, int[] dots, Rectangle rect) {
        this.drawDots(g, dots, rect, fgColor);
    }

    public void drawDots(Graphics g, int[] dots, Rectangle rect, Color color) {
        int cX = rect.x;
        int cY = rect.y;

        final int TURN_INDEX = BrailleInfo.MAX_DOT_COUNT / 2 + 1;
        if (null != g) {
            g.setColor(color);
        }
        for (int i = 0; i < BrailleInfo.MAX_DOT_COUNT; i++) {
            int code = i + 1;
            int dX = cX;
            int dY = cY;
            if (0 < code && TURN_INDEX > code) {
                dY += Math.ceil((code - 1) * this.getDotSpaceY());
            } else {
                dX = (int) Math.ceil(dX + this.getDotSpaceX());
                dY += (int) Math.ceil((code - TURN_INDEX) * this.getDotSpaceY());
            }

            boolean flag = true;
            if (null != dots) {
                for (int j = 0; j < dots.length; j++) {
                    if (code == dots[j]) {
                        this.plot(g, dX, dY);
                        flag = false;
                    }
                }
            }
            if (flag && MODE.DISPLAY == this.mode) {
                dY += this.DOT_SIZE / 2;
                if (null != g) {
                    g.drawLine(dX, dY, dX + this.DOT_SIZE, dY);
                }
            }
        }
    }

    protected void drawRowDots(Graphics g, int x, int y, boolean open) {
        int cX = x + (open ? 0 : this.SPACE_X);
        int cY = y + (this.LINE_SPACE - this.SPACE_Y) / 2;

        if (null != g) {
            g.setColor(fgColor);
        }

        this.plot(g, cX, cY);
        this.plot(g, cX, cY + this.SPACE_Y);
    }

}
