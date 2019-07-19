package org.boilerpipe.web;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExtractionResponse {

  @JsonProperty("highlighted")
  String highlighted;

  @JsonProperty("plain")
  String plain;
}
