package tdm.tokencounttagcloud

import java.io.{File, FileOutputStream, OutputStreamWriter, PrintWriter}

import com.gilt.gfc.time.Timer
import com.typesafe.config.ConfigFactory
import html.TagCloud
import kantan.csv._
import kantan.csv.ops._
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import org.hathitrust.htrc.tools.scala.io.IOUtils.using
import play.api.libs.json.Json
import tdm.tokencounttagcloud.Helper._
import tdm.tokencounttagcloud.TokenFormat._
import tdm.tokencounttagcloud.features.EF

import scala.collection.mutable
import scala.io.{Codec, Source}

/**
  * This tool aggregates token counts from TDM extracted features and produces
  * a tag cloud visualization and a token count CSV from the input.
  *
  * @author Boris Capitanu
  */
object Main {
  val appName: String = "tdm-token-count-tag-cloud"
  val supportedLanguages: Set[String] = Set("ar", "zh", "en", "fr", "de", "es")

  def stopSparkAndExit(sc: SparkContext, exitCode: Int = 0): Unit = {
    try {
      sc.stop()
    }
    finally {
      System.exit(exitCode)
    }
  }

  def main(args: Array[String]): Unit = {
    val (conf, input) =
      if (args.contains("--config")) {
        val configArgs = new ConfigFileArg(args)
        val input = configArgs.efInputFile.toOption
        Conf.fromConfig(ConfigFactory.parseFile(configArgs.configFile()).getConfig(appName)) -> input
      } else {
        val cmdLineArgs = new CmdLineArgs(args)
        val input = cmdLineArgs.efInputFile.toOption
        Conf.fromCmdLine(cmdLineArgs) -> input
      }

    val sparkConf = new SparkConf()
    sparkConf.setAppName(appName)
    sparkConf.setIfMissing("spark.master", s"local[${conf.numCores}]")

    val spark = SparkSession.builder()
      .config(sparkConf)
      .getOrCreate()

    val sc = spark.sparkContext

    try {
      logger.info("Starting...")
      logger.debug(s"Using ${conf.numCores} cores")

      val t0 = Timer.nanoClock()
      conf.outputPath.mkdirs()

      type Token = String
      type Replacement = String
      type Correction = (Token, Replacement)

      val stopwordsBcast = {
        val stopwords = conf.stopWordsUrl match {
          case Some(url) =>
            logger.info("Loading stop words from {}", url)
            Source.fromURL(url)(Codec.UTF8).getLines().toSet

          case None => Set.empty[Token]
        }

        sc.broadcast(stopwords)
      }

      val correctionsBcast = {
        val corrections = conf.correctionsUrl match {
          case Some(url) =>
            logger.info("Loading correction data from {}", url)
            url.asCsvReader[Correction](rfc.withHeader)
              .foldLeft(Map.empty[Token, Replacement]) { (map, res) =>
                res match {
                  case Right(row) => map + row
                  case Left(error) =>
                    throw new RuntimeException(s"Error parsing correction data", error)
                }
              }

          case None => Map.empty[Token, Replacement]
        }

        sc.broadcast(corrections)
      }

      val efRDD = sc.textFile(input.get.toString).map(Json.parse(_).as[EF])
      val pagesRDD = efRDD.flatMap(_.features.pages)
      val tokenCounts = pagesRDD
        .flatMap(_.body)
        .flatMap(_.tokenPosCount)
        .filter { case (token, _) =>
          val stopwords = stopwordsBcast.value
          !stopwords.contains(token.toLowerCase())
        }
        .map { case tm@(token, map) =>
          val corrections = correctionsBcast.value
          val tokenLowerCase = token.toLowerCase()
          corrections.get(tokenLowerCase) match {
            case Some(correction) if conf.lowercaseBeforeCounting => correction -> map
            case Some(correction) =>
              checkTokenFormat(token) match {
                case UpperCase => correction.toUpperCase() -> map
                case LowerCase => correction -> map
                case SentenceCase if correction.length >= 2 => (correction.head.toUpper + correction.tail.toLowerCase()) -> map
                case _ => correction -> map
              }
            case None if conf.lowercaseBeforeCounting => tokenLowerCase -> map
            case None => tm
          }
        }
        .mapValues(_.values.sum)
        .reduceByKey(_ + _)
        .sortBy(_._2, ascending = false)
        .collect()

      val tokenCountsCsvFile = new File(conf.outputPath, "token_counts.csv")
      val tagCloudHtmlFile = new File(conf.outputPath, "tag_cloud.html")

      logger.info("Saving token counts...")
      val writer = new OutputStreamWriter(new FileOutputStream(tokenCountsCsvFile), Codec.UTF8.charSet)
      val csvConfig = rfc.withHeader("token", "count")
      using(writer.asCsvWriter[(Token, Int)](csvConfig)) { out =>
        out.write(tokenCounts)
      }

      logger.info("Saving token counts tag cloud...")
      using(new PrintWriter(tagCloudHtmlFile, Codec.UTF8.name)) { out =>
        val it = tokenCounts.iterator
        val filteredTokenCounts = conf.tagCloudTokenRegex match {
          case Some(regex) => it.filter { case (token, _) => regex.findFirstMatchIn(token).isDefined }
          case None => it
        }
        val tokens = mutable.MutableList.empty[Token]
        val counts = mutable.MutableList.empty[Int]
        for ((token, count) <- filteredTokenCounts.take(conf.maxTokensToDisplay)) {
          tokens += token
          counts += count
        }
        out.write(TagCloud(tokens, counts)(TagCloudConfig(title = "Tag Cloud")).toString)
      }

      val t1 = Timer.nanoClock()
      val elapsed = t1 - t0

      logger.info(f"All done in ${Timer.pretty(elapsed)}")
    }
    catch {
      case e: Throwable =>
        logger.error(s"Uncaught exception", e)
        stopSparkAndExit(sc, exitCode = 500)
    }

    stopSparkAndExit(sc)
  }
}
