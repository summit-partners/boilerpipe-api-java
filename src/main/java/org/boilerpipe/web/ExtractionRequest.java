package org.boilerpipe.web;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExtractionRequest {
    public String html;
    public String url;

    @JsonProperty("extract_html")
    boolean extractHtml;

    @JsonProperty("extract_images")
    boolean extractImages;

    @JsonProperty("extract_text")
    boolean extractText;
}
