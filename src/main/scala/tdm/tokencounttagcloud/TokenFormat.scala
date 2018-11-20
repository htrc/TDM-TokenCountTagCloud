package tdm.tokencounttagcloud

object TokenFormat extends Enumeration {
  type TokenFormat = Value
  val UpperCase, LowerCase, SentenceCase, MixedCase, NoCase = Value
}
