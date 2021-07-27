import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 *@author pmrachkovskaya
 */
public class TestRegex {
  public static void main(String[] args) {
    String text="[6398C613619E4DCA88220ACA49603D87] [HELLO THOMAS 2] [PRIVATE]";
    String patternString = "\\[(.*?)\\]";
    Pattern pattern = Pattern.compile(patternString);

    Matcher matcher = pattern.matcher(text);
    List<String> allMatches = new ArrayList<String>(3);

    while(matcher.find()){
      allMatches.add(matcher.group(1));
      System.out.println(matcher.group(1));
    }
  }
}
