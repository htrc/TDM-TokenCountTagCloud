package tdm.tokencounttagcloud

case class TagCloudConfig(title: String,                // the title for the page
                          width: Int = 1000,            // tag cloud width
                          height: Int = 1000,           // tag cloud height
                          fontMin: Int = 20,            // minimum font size
                          fontMax: Int = 150,           // maximum font size
                          scale: String = "linear",     // one of linear, log, sqrt
                          rotation: Int = 0,            // rotate text by degrees (0 = all horizontal, 90 = horizontal or vertical)
                          colorPalette: String = "category20",  // one of category10, category20, category20b, category20c
                          showCounts: Boolean = false,  // should counts be included in the tokens from the tag cloud?
                          showToolTip: Boolean = true,  // should a tooltip containing the word and the count be shown upon hovering a token in the tag cloud?
                          overflow: Boolean = false,    // enable the overflow logic that draws partial words in case they don't fully fit?
                          fontName: Option[String] = None, // name of the font to use for the words in the tag cloud
                          d3ApiUrl: String = "https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.17/d3.min.js",
                          d3LayoutCloudApiUrl: String = "https://cdnjs.cloudflare.com/ajax/libs/d3-cloud/1.2.5/d3.layout.cloud.min.js")