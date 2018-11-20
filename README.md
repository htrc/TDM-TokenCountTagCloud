# TDM-TokenCountTagCloud

Token count and tag cloud generator for a set of TDM extracted features.

This tool takes as input a set of TDM JSON extracted features records and computes an aggregate token count and generates a tag cloud and CSV file reflecting these token counts.  The tag cloud can be "massaged" to display at most N tokens that match a given regular expression (to allow, for example, to only display words made up exclusively of alphabetic characters).  The tool also optionally applies a pre-processing step consisting of stopword removal and/or text correction (given appropriate rules).

# Build

`sbt clean dist`

then find the resulting `tdm-token-count-tag-cloud-<version>.zip` in the `target/universal/` folder.

# Run

```
unzip tdm-token-count-tag-cloud-<version>.zip
cd tdm-token-count-tag-cloud-<version>
./bin/tdm-token-count-tag-cloud <ARGS> <INPUT>
```

## Help

```
tdm-token-count-tag-cloud
Text and Data Mining (TDM) initiative involving HathiTrust/HTRC, JSTOR, and Portico
      --corrections-url  <URL>   (Optional) The URL containing the correction
                                 rules to use
      --lowercase                (Optional) Lowercase all tokens before counting
  -m, --max-display  <N>         (Optional) Display only this many of the most
                                 highest-occurring tokens (default = 200)
  -c, --num-cores  <N>           (Optional) The number of CPU cores to use (if
                                 not specified, uses all available cores)
  -n, --num-partitions  <N>      (Optional) The number of partitions to split
                                 the input set of features into, for increased
                                 parallelism
  -o, --output  <DIR>            The folder where the output will be written to
      --stopwords-url  <URL>     (Optional) The URL containing the stop words to
                                 remove
      --token-filter  <arg>      (Optional) Regular expression which determines
                                 which tokens will be displayed in the tag cloud
  -h, --help                     Show help message
  -v, --version                  Show version of this program

 trailing arguments:
  input (required)   The TDM extracted features JSON file to process (if not
                     provided, will read from stdin)
```

### Correction rules (optional)

The correction rules can be used to correct common mispellings or OCR errors. An example set of correction rules (generated based on a process that identifies likely OCR errors) can be found here: http://data.analytics.hathitrust.org/data/text/default_corrections.txt

### Stopword removal (optional)

A stopword list can be supplied for further text cleaning by removing common stop words from the resulting CSV and tag cloud output. A sample list can be found here: http://data.analytics.hathitrust.org/data/text/default_stopwords_en.txt

### Token filtering (optional)

Tokens extracted from a text don't necessarily all represent real words. Things like numbers, dates, or other punctuation are also represented as "tokens", so the resulting tag cloud will also feature them.  If it's desired to only display tokens matching a particular format, then a regular expression can be supplied which matches only tokens desired to be displayed in the tag cloud. The regular expression does not affect the CSV output.
