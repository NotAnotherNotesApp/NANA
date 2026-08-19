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
