package tdm.tokencounttagcloud

import play.api.libs.json.{Json, Reads}
import tdm.featureextractor.features.SectionFeatures

package object features {

  implicit val sectionFeaturesReads: Reads[SectionFeatures] = Json.reads[SectionFeatures]
  implicit val pageFeaturesReads: Reads[TdmPageFeatures] = Json.reads[TdmPageFeatures]
  implicit val volumeFeaturesReads: Reads[TdmVolumeFeatures] = Json.reads[TdmVolumeFeatures]
  implicit val efReads: Reads[EF] = Json.reads[EF]

}
