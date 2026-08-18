package com.allubie.nana.util

// Strips HTML formatting into plain text while preserving paragraph breaks and list items
fun stripHtml(html: String): String {
    var result = html
    
    // Lists and line breaks
    result = result.replace(Regex("</?[ou]l[^>]*>"), "")
    result = result.replace(Regex("<li[^>]*>"), "\n- ")
    result = result.replace(Regex("</li>"), "")
    result = result.replace(Regex("<(p|div|br)[^>]*>"), "\n")
    result = result.replace(Regex("</(p|div)>"), "")
    
    // Strip remaining tags
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
