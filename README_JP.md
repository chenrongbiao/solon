<h1 align="center" style="text-align:center;">
<img src="solon_icon.png" width="128" />
<br />
Solon v2.6.2-SNAPSHOT
</h1>
<p align="center">
	<strong>Javaの新しいアプリケーション開発フレームワーク、より小さく、より速く、より簡単です!</strong>
</p>
<p align="center">
	<a href="https://solon.noear.org/">https://solon.noear.org</a>
</p>

<p align="center">
    <a target="_blank" href="https://central.sonatype.com/search?q=org.noear%3Asolon-parent">
        <img src="https://img.shields.io/maven-central/v/org.noear/solon.svg?label=Maven%20Central" alt="Maven" />
    </a>
    <a target="_blank" href="LICENSE">
		<img src="https://img.shields.io/:License-Apache2-blue.svg" alt="Apache 2" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html">
		<img src="https://img.shields.io/badge/JDK-8-green.svg" alt="jdk-8" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html">
		<img src="https://img.shields.io/badge/JDK-11-green.svg" alt="jdk-11" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html">
		<img src="https://img.shields.io/badge/JDK-17-green.svg" alt="jdk-17" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html">
		<img src="https://img.shields.io/badge/JDK-21-green.svg" alt="jdk-21" />
	</a>
    <br />
    <a target="_blank" href='https://gitee.com/noear/solon/stargazers'>
		<img src='https://gitee.com/noear/solon/badge/star.svg' alt='gitee star'/>
	</a>
    <a target="_blank" href='https://github.com/noear/solon/stargazers'>
		<img src="https://img.shields.io/github/stars/noear/solon.svg?logo=github" alt="github star"/>
	</a>
</p>

<br/>
<p align="center">
	<a href="https://jq.qq.com/?_wv=1027&k=kjB5JNiC">
	<img src="https://img.shields.io/badge/QQ交流群-22200020-orange"/></a>
</p>

##### 言語： 日本語 | [中文](README_CN.md) | [English](README_EN.md) | [Русский](README_RU.md)

<hr />

起動が5～10倍速く、qpsは2～3倍高く、ランタイムメモリの使用量1/3〜1/2少なく、パッケージは1/2～1/10小さくなります。

<hr />

## 紹介：

ゼロから構築され。独自の標準規範と開放的なエコシステムを備えており、異なるエコプラグインを組み合わせて様々なニーズに対応し、カスタマイズが可能、快速的に開発可能。

* **理性的・簡易的・効率的・開放的・エコシステム的**
* JDK8、JDK11、JDK17、JDK21に対応可能
* Http、WebSocket、Socket の3つの信号を統合した開発体験 (通称: 3ソース統合)
* 「注釈」と「手動」2種類のモードをサポートし、必要に応じて自由に操作可能
* Not Servlet、あらゆる通信インフラストラクチャに適合（最小0.3 mでrpcアーキテクチャが実行可能）
* [ユニークなIOC/AOP容器のデザインです](https://solon.noear.org/article/241)。プラグインが多くなったからといって起動が遅くなることはありません
* Web、Data、Job、Remoting、Cloudなどの開発シナリオをサポート
* Handler+ContextとListener+Messageの2つのイベントモデルを両立
* プラグイン式の拡張を強調し、異なるアプリケーションシーンに対応可能
* GraalVm Native Imageパッケージをサポート
* サービスプラグインには「ホットプラグ」「ホットプラグ」「ホットマネジメント」ができます。


## エコシステム：

* solon

<img src="solon_schema.png" width="700" />

* solon cloud

<img src="solon_cloud_schema.png" width="700" />

## 公式サイトと関するデモ・ケース：

* 公式サイト：[https://solon.noear.org](https://solon.noear.org)
* 公式サイトのデモ：[https://gitee.com/noear/solon-examples](https://gitee.com/noear/solon-examples)
* プロジェクトのシングルテスト：[__test](./__test/) 
* プロジェクトの詳細機能のデモ：[solon_api_demo](https://gitee.com/noear/solon_api_demo)  、 [solon_rpc_demo](https://gitee.com/noear/solon_rpc_demo) 、 [solon_socketd_demo](https://gitee.com/noear/solon_socketd_demo) 、 [solon_cloud_demo](https://gitee.com/noear/solon_cloud_demo) 、 [solon_auth_demo](https://gitee.com/noear/solon_auth_demo)
* ユーザーケース：[オープンソースプロジェクトです](https://solon.noear.org/article/555)、[ユーザービジネスです](https://solon.noear.org/article/cases)


## オープンソースプロジェクトへのサポートしてくれたJetBrainsに特別感謝致します

<a href="https://jb.gg/OpenSourceSupport">
  <img src="https://user-images.githubusercontent.com/8643542/160519107-199319dc-e1cf-4079-94b7-01b6b8d23aa6.png" align="left" height="100" width="100"  alt="JetBrains">
</a>

