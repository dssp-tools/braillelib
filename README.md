# braillelib

braillelibは点図作成ソフト「橋立」で使用するライブラリです。

## ビルド手順
必要なソフトウェア
* maven

ビルド

    mvn install

* targetにbraileLib.jarが生成されます
* braileLib.jarはローカルのmavenリポジトリに登録されます

## Eclipseを使う場合

#### プロジェクトのインポート
1. ワークスペースにGitクローンする。
1. 「ファイル」メニュー→「インポート」→「Maven既存プロジェクト」を選択する。
1. 「ルートディレクトリ」にbraillelibフォルダのパスを入力して、「完了」をクリックする。


#### ビルド
1. パッケージエクスプローラーでbraillelibを選択して右クリックする。
1. 「実行」→「Mavenビルド」で、「ゴール」に「install」を入力する。
<br/>2回目以降は「実行」→「braillelib(Mevenビルド）」
1. 「実行」ボタンをクリックする。

## ライセンス
[ライセンス](Lisense.txt)


## リンク
[障害学生支援プロジェクト Disabled-student Study Support Project(DSSP)](http://dssp.sakura.ne.jp/)

[hashidate](https://github.com/dssp-tools/hashidate)
