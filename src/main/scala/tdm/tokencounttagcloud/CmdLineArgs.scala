package tdm.tokencounttagcloud

import java.io.File
import java.net.URL

import org.rogach.scallop.exceptions._
import org.rogach.scallop.{Scallop, ScallopConf, ScallopHelpFormatter, ScallopOption, SimpleOption, throwError}

import scala.util.Try

abstract class InputArg(arguments: Seq[String]) extends ScallopConf(arguments) {
  val efInputFile: ScallopOption[File] = trailArg[File]("input",
    descr = "The TDM extracted features JSON file to process (if not provided, will read from stdin)"
  )

  validateFileExists(efInputFile)
}

/**
  * Command line argument configuration
  *
  * @param arguments The cmd line args
  */
class CmdLineArgs(arguments: Seq[String]) extends InputArg(arguments) {
  appendDefaultToDescription = true
  helpFormatter = new ScallopHelpFormatter {
    override def getOptionsHelp(s: Scallop): String = {
      super.getOptionsHelp(s.copy(opts = s.opts.map {
        case opt: SimpleOption if !opt.required =>
          opt.copy(descr = "(Optional) " + opt.descr)
        case other => other
      }))
    }
  }

  private val (appTitle, appVersion, appVendor) = {
    val p = getClass.getPackage
    val nameOpt = Option(p).flatMap(p => Option(p.getImplementationTitle))
    val versionOpt = Option(p).flatMap(p => Option(p.getImplementationVersion))
    val vendorOpt = Option(p).flatMap(p => Option(p.getImplementationVendor))
    (nameOpt, versionOpt, vendorOpt)
  }

  version(appTitle.flatMap(
    name => appVersion.flatMap(
      version => appVendor.map(
        vendor => s"$name $version\n$vendor"))).getOrElse(Main.appName))

  val numPartitions: ScallopOption[Int] = opt[Int]("num-partitions",
    descr = "The number of partitions to split the input set of features into, " +
      "for increased parallelism",
    argName = "N",
    validate = 0 <
  )

  val numCores: ScallopOption[Int] = opt[Int]("num-cores",
    descr = "The number of CPU cores to use (if not specified, uses all available cores)",
    short = 'c',
    argName = "N",
    validate = 0 <
  )

  val outputPath: ScallopOption[File] = opt[File]("output",
    descr = "The folder where the output will be written to",
    argName = "DIR",
    required = true
  )

  val correctionsUrl: ScallopOption[URL] = opt[URL]("corrections-url",
    descr = "The URL containing the correction rules to use",
    argName = "URL"
  )

  val stopWordsUrl: ScallopOption[URL] = opt[URL]("stopwords-url",
    descr = "The URL containing the stop words to remove",
    argName = "URL",
    noshort = true
  )

  val lowercaseBeforeCounting: ScallopOption[Boolean] = opt[Boolean]("lowercase",
    descr = "Lowercase all tokens before counting",
    default = Some(Defaults.LOWERCASE),
    noshort = true
  )

  val tagCloudTokenFilter: ScallopOption[String] = opt[String]("token-filter",
    descr = "Regular expression which determines which tokens will be displayed in the tag cloud",
    noshort = true,
    validate = regexp => Try(regexp.r).isSuccess
  )

  val maxDisplay: ScallopOption[Int] = opt[Int]("max-display",
    descr = "Display only this many of the most highest-occurring tokens",
    argName = "N",
    default = Some(Defaults.MAXDISPLAY)
  )

  verify()
}

class ConfigFileArg(arguments: Seq[String]) extends InputArg(arguments) {
  version("not used")

  override protected def onError(e: Throwable): Unit = e match {
    case r: ScallopResult if !throwError.value => r match {
      case Help("") => new CmdLineArgs(Seq("--help"))
      case Version => new CmdLineArgs(Seq("--version"))
      case ScallopException(message) => errorMessageHandler(message)
      case _ =>
    }
    case err => throw err
  }

  val configFile: ScallopOption[File] = opt[File]("config",
    descr = "Configuration file that can be used instead of the command line arguments",
    noshort = true,
    required = true
  )

  validateFileExists(configFile)
  verify()
}