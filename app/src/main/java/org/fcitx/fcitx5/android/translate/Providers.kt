/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 easy_zh2en contributors
 */
package org.fcitx.fcitx5.android.translate

import androidx.annotation.StringRes
import org.fcitx.fcitx5.android.R

/** OpenAI-compatible providers offered in the dropdown; the first model of each is its default. "custom" leaves URL and model to the user. */
object Providers {

    data class Provider(val id: String, @StringRes val label: Int, val baseUrl: String, val models: List<String>)

    const val CUSTOM = "custom"

    val all = listOf(
        Provider("deepseek", R.string.translate_provider_deepseek, "https://api.deepseek.com", listOf("deepseek-chat")),
        Provider("moonshot", R.string.translate_provider_moonshot, "https://api.moonshot.cn/v1", listOf("kimi-k2-turbo-preview", "moonshot-v1-8k")),
        Provider("zhipu", R.string.translate_provider_zhipu, "https://open.bigmodel.cn/api/paas/v4", listOf("glm-4-flash", "glm-4-air", "glm-4-plus")),
        Provider("qwen", R.string.translate_provider_qwen, "https://dashscope.aliyuncs.com/compatible-mode/v1", listOf("qwen-plus", "qwen-turbo", "qwen-max")),
        Provider("siliconflow", R.string.translate_provider_siliconflow, "https://api.siliconflow.cn/v1", listOf("deepseek-ai/DeepSeek-V3", "Qwen/Qwen2.5-7B-Instruct")),
        Provider("openai", R.string.translate_provider_openai, "https://api.openai.com/v1", listOf("gpt-4o-mini", "gpt-4.1-mini", "gpt-4o")),
        Provider(CUSTOM, R.string.translate_provider_custom, "", emptyList())
    )

    fun get(id: String) = all.firstOrNull { it.id == id } ?: all.last()

    /** value goes into the prompt as-is, label is what the user sees */
    val targets = listOf(
        "English" to R.string.translate_lang_en,
        "Japanese" to R.string.translate_lang_ja,
        "Korean" to R.string.translate_lang_ko,
        "French" to R.string.translate_lang_fr,
        "German" to R.string.translate_lang_de,
        "Spanish" to R.string.translate_lang_es,
        "Russian" to R.string.translate_lang_ru,
        "Traditional Chinese" to R.string.translate_lang_zh_hant,
        "Simplified Chinese" to R.string.translate_lang_zh_hans
    )
}
