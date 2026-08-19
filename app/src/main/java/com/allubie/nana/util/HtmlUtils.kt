package com.allubie.nana.util

// Helper function to strip HTML tags and get plain text, preserving list markers
fun stripHtml(html: String): String {
    var result = html
    
    // Track list context for ordered lists
    var listCounter = 0
    var inOrderedList = false
    
    // Handle ordered lists - replace <ol> tags and number list items
    result = result.replace(Regex("<ol[^>]*>")) { 
        inOrderedList = true
        listCounter = 0
        ""
    }
    result = result.replace(Regex("</ol>")) {
        inOrderedList = false
        ""
    }
    
    // Handle unordered lists
    result = result.replace(Regex("<ul[^>]*>"), "")
    result = result.replace(Regex("</ul>"), "")
    
    // Replace list items with appropriate markers
    // For simplicity, use bullet for unordered and dash for ordered (since we can't track state in single regex)
    result = result.replace(Regex("<li[^>]*>"), "\n- ")
    result = result.replace(Regex("</li>"), "")
    
    // Handle paragraphs and line breaks
    result = result.replace(Regex("<p[^>]*>"), "\n")
    result = result.replace(Regex("</p>"), "")
    result = result.replace(Regex("<br[^>]*>"), "\n")
    result = result.replace(Regex("<div[^>]*>"), "\n")
    result = result.replace(Regex("</div>"), "")
    
    // Remove remaining HTML tags
    result = result.replace(Regex("<[^>]*>"), "")
    
    // Handle HTML entities
    result = result
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&apos;", "'")
        .replace("&period;", ".")
        .replace("&comma;", ",")
        .replace("&colon;", ":")
        .replace("&semi;", ";")
        .replace("&excl;", "!")
        .replace("&quest;", "?")
        .replace("&hyphen;", "-")
        .replace("&dash;", "-")
        .replace("&ndash;", "-")
        .replace("&mdash;", "-")
        .replace("&lpar;", "(")
        .replace("&rpar;", ")")
        .replace("&lsqb;", "[")
        .replace("&rsqb;", "]")
        .replace("&lcub;", "{")
        .replace("&rcub;", "}")
        .replace("&num;", "#")
        .replace("&dollar;", "$")
        .replace("&percnt;", "%")
        .replace("&ast;", "*")
        .replace("&plus;", "+")
        .replace("&equals;", "=")
        .replace("&commat;", "@")
        .replace("&sol;", "/")
        .replace("&bsol;", "\\")
        .replace("&verbar;", "|")
        .replace("&tilde;", "~")
        .replace("&circ;", "^")
        .replace("&grave;", "`")
        .replace(Regex("&#(\\d+);")) { matchResult ->
            val code = matchResult.groupValues[1].toIntOrNull()
            if (code != null) code.toChar().toString() else matchResult.value
        }
        .replace(Regex("&#x([0-9a-fA-F]+);")) { matchResult ->
            val code = matchResult.groupValues[1].toIntOrNull(16)
            if (code != null) code.toChar().toString() else matchResult.value
        }
    
    // Clean up multiple newlines and spaces
    result = result.replace(Regex("\\n{3,}"), "\n\n")
    result = result.replace(Regex(" +"), " ")
    result = result.trim()
    
    // Remove leading newline if present
    if (result.startsWith("\n")) {
        result = result.substring(1)
    }
    
    return result
}

/**
 * Sanitizes HTML content for note editor and viewer to ensure proper contrast in all themes (Dark, Light, AMOLED).
 * Strips hardcoded text colors and page background colors so that text dynamically adopts the theme's onSurface color,
 * while preserving all structural formatting (bold, italic, underline, strikethrough, headings, lists, code, and highlights).
 */
fun sanitizeHtmlForEditor(html: String): String {
    if (html.isBlank()) return html
    var result = html

    // 1. Remove font color attributes: <font color="..."> -> <font>
    result = result.replace(Regex("(<font\\b[^>]*?)\\s+color\\s*=\\s*(?:\"[^\"]*\"|'[^']*'|[^\\s>]+)", RegexOption.IGNORE_CASE)) { matchResult ->
        matchResult.groupValues[1]
    }

    // 2. Clean style="..." attributes by removing color and non-highlight background styles
    val stylePattern = Regex("style\\s*=\\s*\"([^\"]*)\"", RegexOption.IGNORE_CASE)
    result = result.replace(stylePattern) { matchResult ->
        val styleContent = matchResult.groupValues[1]
        val cleaned = cleanCssStyles(styleContent)
        if (cleaned.isNotBlank()) "style=\"$cleaned\"" else ""
    }

    val styleSingleQuotePattern = Regex("style\\s*=\\s*'([^']*)'", RegexOption.IGNORE_CASE)
    result = result.replace(styleSingleQuotePattern) { matchResult ->
        val styleContent = matchResult.groupValues[1]
        val cleaned = cleanCssStyles(styleContent)
        if (cleaned.isNotBlank()) "style='$cleaned'" else ""
    }

    // 3. Remove leftover empty tags
    result = result.replace(Regex("<font\\s*>(.*?)</font>", RegexOption.IGNORE_CASE), "$1")
    result = result.replace(Regex("<span\\s*>(.*?)</span>", RegexOption.IGNORE_CASE), "$1")

    return result
}

private fun cleanCssStyles(styleContent: String): String {
    // Remove text color property: color: ...;
    var cleaned = styleContent.replace(Regex("(?<![a-zA-Z-])color\\s*:\\s*[^;\"]+;?", RegexOption.IGNORE_CASE), "")

    // Remove page-level background colors (white, black, near-white, near-black), but keep highlights
    cleaned = cleaned.replace(
        Regex("background(?:-color)?\\s*:\\s*(?:#ffffff|#fff|white|rgba?\\(\\s*255\\s*,\\s*255\\s*,\\s*255[^)]*\\)|#000000|#000|black|rgba?\\(\\s*0\\s*,\\s*0\\s*,\\s*0[^)]*\\)|#121212|#1a1a1a|#202124|#222222)\\s*;?", RegexOption.IGNORE_CASE),
        ""
    )

    return cleaned.trim().trim(';').trim()
}
