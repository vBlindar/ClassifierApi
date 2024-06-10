package kg.edu.krsu.vblindar.classifierapi.ngram;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PorterStemmer {
  private static final Pattern RUSSIAN_LETTERS = Pattern.compile("[а-яА-Я]+");
  private static final Pattern PERFECT_GERUND = Pattern.compile("((?<group1>ив|ивши|ившись|ыв|ывши|ывшись)|(([ая])(?<group2>в|вши|вшись)))$");
  private static final Pattern ADJECTIVE = Pattern.compile("(ее|ие|ые|ое|ими|ыми|ей|ий|ый|ой|ем|им|ым|ом|его|ого|ему|ому|их|ых|ую|юю|ая|яя|ою|ею)$");
  private static final Pattern PARTICIPLE = Pattern.compile("((ивш|ывш|ующ)|((?<=[ая])(ем|нн|вш|ющ|щ)))$");
  private static final Pattern REFLEXIVE = Pattern.compile("(с[яь])$");
  private static final Pattern VERB = Pattern.compile(
          "((ила|ыла|ена|ейте|уйте|ите|или|ыли|ей|уй|ил|ыл|им|ым|ен|ило|ыло|ено|ят|ует|уют|ит|ыт|ены|ить|ыть|ишь|ую|ю)" +
                  "|((?<=[ая])(ла|на|ете|йте|ли|й|л|ем|н|ло|но|ет|ют|ны|ть|ешь|нно)))$");
  private static final Pattern NOUN = Pattern.compile("(а|ев|ов|ие|ье|е|иями|ями|ами|еи|ии|и|ией|ей|ой|ий|й|иям|ям|ием|ем|ам|ом|о|у|ах|иях|ях|ы|ь|ию|ью|ю|ия|ья|я)$");
  private static final Pattern VOWELS = Pattern.compile("^(.*?[аеиоуыэюя])(.*)$");
  private static final Pattern DERIVATIONAL_SUFFIX = Pattern.compile("ость?$");
  private static final Pattern DERIVATIONAL = Pattern.compile(".*[^аеиоуыэюя]+[аеиоуыэюя].*" + DERIVATIONAL_SUFFIX);
  private static final Pattern SUPERLATIVE = Pattern.compile("(ейше|ейш)$");
  private static final Pattern AND = Pattern.compile("и$");
  private static final Pattern SOFT_SIGN = Pattern.compile("ь$");
  private static final Pattern NN = Pattern.compile("нн$");

  public String stem(String word) {
    if (word == null) {
      return null;
    }

    word = clear(word);

    if (!RUSSIAN_LETTERS.matcher(word).matches()) {
      return null;
    }

    Matcher vowelsMatcher = VOWELS.matcher(word);
    if (!vowelsMatcher.matches()) {
      return word;
    }


    String beforeRV = vowelsMatcher.group(1);
    String rv = vowelsMatcher.group(2);

    rv = step1(rv);
    rv = step2(rv);
    rv = step3(rv);
    rv = step4(rv);

    return beforeRV + rv;
  }

  private String clear(String word) {
    return word.toLowerCase().replace("ё", "е");
  }

  private String step1(String rv) {
    String temp = replacePerfectGerundEnd(rv);
    if (!rv.equals(temp)) {
      return temp;
    }

    rv = REFLEXIVE.matcher(rv).replaceFirst("");
    temp = ADJECTIVE.matcher(rv).replaceFirst("");

    if (!rv.equals(temp)) {
      temp = PARTICIPLE.matcher(temp).replaceFirst("");
      return temp;
    }
    rv = temp;
    temp = VERB.matcher(rv).replaceFirst("");

    if (!rv.equals(temp)) {
      return temp;
    }
    return NOUN.matcher(rv).replaceFirst("");
  }

  private String replacePerfectGerundEnd(String rv) {
    Matcher matcher = PERFECT_GERUND.matcher(rv);
    if (!matcher.find()) {
      return rv;
    }

    String group1 = matcher.group("group1");
    if (group1 != null) {
      return rv.substring(0, rv.lastIndexOf(group1));
    }

    String group2 = matcher.group("group2");
    if (group2 != null) {
      return rv.substring(0, rv.lastIndexOf(group2));
    }

    return rv;
  }

  private String step2(String rv) {
    return AND.matcher(rv).replaceFirst("");
  }

  private String step3(String rv) {
    if (DERIVATIONAL.matcher(rv).find()) {
      rv = DERIVATIONAL_SUFFIX.matcher(rv).replaceFirst("");
    }
    return rv;
  }

  private String step4(String rv) {
    String temp = SOFT_SIGN.matcher(rv).replaceFirst("");
    if (rv.equals(temp)) {
      rv = SUPERLATIVE.matcher(rv).replaceFirst("");
      rv = NN.matcher(rv).replaceFirst("н");
    } else {
      rv = temp;
    }

    return rv;
  }
}