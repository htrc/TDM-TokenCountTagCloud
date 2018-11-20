package tdm.tokencounttagcloud

import java.io.File
import java.net.URL

import com.typesafe.config.Config

import scala.util.matching.Regex

case class Conf(numPartitions: Option[Int],
                numCores: String,
                outputPath: File,
                correctionsUrl: Option[URL],
                stopWordsUrl: Option[URL],
                maxTokensToDisplay: Int,
                lowercaseBeforeCounting: Boolean,
                tagCloudTokenRegex: Option[Regex])

object Conf {
  def fromConfig(config: Config): Conf = {
    val numPartitions = if (config.hasPath("num-partitions")) Some(config.getInt("num-partitions")) else None
    val numCores = if (config.hasPath("num-cores")) config.getString("num-cores") else Defaults.NUMCORES
    val outputPath = new File(config.getString("output"))
    val correctionsUrl = if (config.hasPath("corrections-url")) Some(new URL(config.getString("corrections-url"))) else None
    val stopWordsUrl = if (config.hasPath("stopwords-url")) Some(new URL(config.getString("stopwords-url"))) else None
    val maxTokensToDisplay = if (config.hasPath("max-display")) config.getInt("max-display") else Defaults.MAXDISPLAY
    val lowercaseBeforeCounting = if (config.hasPath("lowercase")) config.getBoolean("lowercase") else Defaults.LOWERCASE
    val tagCloudTokenRegex = if (config.hasPath("token-filter")) Some(config.getString("token-filter").r) else None

    Conf(
      numPartitions = numPartitions,
      numCores = numCores,
      outputPath = outputPath,
      correctionsUrl = correctionsUrl,
      stopWordsUrl = stopWordsUrl,
      maxTokensToDisplay = maxTokensToDisplay,
      lowercaseBeforeCounting = lowercaseBeforeCounting,
      tagCloudTokenRegex = tagCloudTokenRegex
    )
  }

  def fromCmdLine(cmdLineArgs: CmdLineArgs): Conf = {
    val numPartitions = cmdLineArgs.numPartitions.toOption
    val numCores = cmdLineArgs.numCores.map(_.toString).getOrElse(Defaults.NUMCORES)
    val outputPath = cmdLineArgs.outputPath()
    val correctionsUrl = cmdLineArgs.correctionsUrl.toOption
    val stopWordsUrl = cmdLineArgs.stopWordsUrl.toOption
    val maxTokensToDisplay = cmdLineArgs.maxDisplay()
    val lowercaseBeforeCounting = cmdLineArgs.lowercaseBeforeCounting()
    val tagCloudTokenRegex = cmdLineArgs.tagCloudTokenFilter.toOption.map(_.r)

    Conf(
      numPartitions = numPartitions,
      numCores = numCores,
      outputPath = outputPath,
      correctionsUrl = correctionsUrl,
      stopWordsUrl = stopWordsUrl,
      maxTokensToDisplay = maxTokensToDisplay,
      lowercaseBeforeCounting = lowercaseBeforeCounting,
      tagCloudTokenRegex = tagCloudTokenRegex
    )
  }
}