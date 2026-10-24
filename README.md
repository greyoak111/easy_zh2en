# easy_zh2en

**打中文，发英文。** 一个带「译」键的安卓输入法：在任意输入框里用拼音打好中文，点工具栏上的「译」，原地换成地道的英文；下方提示一行中文回译，确认意思没跑偏再发。

**Type Chinese, send English.** An Android keyboard with a translate key: type in Chinese (pinyin / wubi) in any text box, tap **译** on the toolbar, and the text is replaced in place with natural English. A back-translation pops up so you can check the meaning before sending.

> easy_zh2en 是 [fcitx5-android](https://github.com/fcitx5-android/fcitx5-android)（小企鹅输入法安卓版）的分支。中文输入引擎、键盘、主题、剪贴板等全部来自上游；本分支只加了翻译功能。与 fcitx5 官方无关。
>
> easy_zh2en is a fork of [fcitx5-android](https://github.com/fcitx5-android/fcitx5-android). The input engine, keyboard, themes and clipboard all come from upstream; this fork only adds translation. It is not affiliated with the fcitx5 project.

## 用法

1. 装好后在系统设置里启用 easy_zh2en，并切换为当前输入法。
2. 打开 easy_zh2en 的设置 → **翻译**，选服务商、填上 API Key。下拉里有 DeepSeek（默认）、Kimi、智谱、通义千问、硅基流动、OpenAI，选好会自动填上接口地址和默认型号；型号也能从下拉里换，或选「自定义…」手输。别的 OpenAI 兼容接口选「自定义」，自己填地址。
3. 在任意输入框打中文，点工具栏的「译」：
   - 有选中文字时，只翻译选中的部分；
   - 没选中时，翻译光标前的文字（最多 2000 字）。
4. 翻完会提示中文回译。不满意就**长按「译」**，恢复原文。

设置里还能选**语气**（日常闲聊 / 技术讨论 / 正式）和**译成**哪种语言（英、日、韩、法、德、西、俄、繁中、简中，默认英语）。代码、命令、链接、@人、项目名会原样保留。

## 隐私

- 上游 fcitx5-android 不申请联网权限；**本分支加了联网权限，只用于翻译**：只在你点「译」时，把要翻译的那段文字发到你自己填写的接口。平时打字不联网、不上传。
- API Key 只存在输入法自己的私有设置里，只发给你填写的接口地址。

## 下载与构建

APK 由本仓库的 GitHub Actions 构建（`.github/workflows/build.yml`），见 [Releases](https://github.com/greyoak111/easy_zh2en/releases) 或 Actions 的构建产物。只出 arm64 版。

和官方 fcitx5 包名不同（`io.github.greyoak111.easyzh2en`），可以同时安装；因此官方的 Rime 等插件装不上，自带的拼音、五笔不受影响。

## 许可

与上游相同：LGPL-2.1-or-later，见 [LICENSE](LICENSE)。本分支的改动集中在 `app/src/main/java/org/fcitx/fcitx5/android/translate/`，其余改动处标有 `easy_zh2en` 注释。

上游原 README 见 [UPSTREAM-README.md](UPSTREAM-README.md)。感谢 fcitx5-android 及 fcitx5 的全体贡献者。
