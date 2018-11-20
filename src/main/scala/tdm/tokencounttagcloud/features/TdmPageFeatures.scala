package tdm.tokencounttagcloud.features

import tdm.featureextractor.features.{BasicFeatures, SectionFeatures}

/**
  * Object recording aggregate features at the page level
  *
  * @param seq            The page sequence id
  * @param version        The MD5 hash of the page content
  * @param language       The identified page language (if any)
  * @param tokenCount     The total token count for the page
  * @param lineCount      The total line count for the page
  * @param emptyLineCount The empty line count for the page
  * @param sentenceCount  The sentence count for the page
  * @param header         The page header features
  * @param body           The page body features
  * @param footer         The page footer features
  */
case class TdmPageFeatures(seq: String,
                           version: String,
                           language: Option[String],
                           tokenCount: Int,
                           lineCount: Int,
                           emptyLineCount: Int,
                           sentenceCount: Option[Int],
                           header: Option[SectionFeatures],
                           body: Option[SectionFeatures],
                           footer: Option[SectionFeatures]) extends BasicFeatures
