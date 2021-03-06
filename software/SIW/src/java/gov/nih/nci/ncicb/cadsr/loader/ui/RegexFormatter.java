/*L
 * Copyright Oracle Inc, SAIC, SAIC-F
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-semantic-tools/LICENSE.txt for details.
 */

package gov.nih.nci.ncicb.cadsr.loader.ui;

import java.text.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.text.*;

/**
 * A regular expression based implementation of <code>AbstractFormatter</code>.
 */
public class RegexFormatter extends DefaultFormatter {
  private Pattern pattern;
  private Matcher matcher;
  private int maxLength = 0;

    public RegexFormatter() {
        super();
    }

  /**
   * Creates a regular expression based <code>AbstractFormatter</code>.
   * <code>pattern</code> specifies the regular expression that will
   * be used to determine if a value is legal.
   */
  public RegexFormatter(String pattern, int maxLength) throws PatternSyntaxException {
    this();
    this.maxLength = maxLength;
    setPattern(Pattern.compile(pattern));
  }

  public RegexFormatter(String pattern) throws PatternSyntaxException {
    this(pattern, 0);
  }
  
  /**
   * Creates a regular expression based <code>AbstractFormatter</code>.
   * <code>pattern</code> specifies the regular expression that will
   * be used to determine if a value is legal.
   */
  public RegexFormatter(Pattern pattern) {
    this();
    setPattern(pattern);
  }
  
  /**
   * Sets the pattern that will be used to determine if a value is
   * legal.
   */
  public void setPattern(Pattern pattern) {
    this.pattern = pattern;
  }

    /**
     * Returns the <code>Pattern</code> used to determine if a value is
     * legal.
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * Sets the <code>Matcher</code> used in the most recent test
     * if a value is legal.
     */
    protected void setMatcher(Matcher matcher) {
        this.matcher = matcher;
    }

    /**
     * Returns the <code>Matcher</code> from the most test.
     */
    protected Matcher getMatcher() {
        return matcher;
    }

  /**
   * Parses <code>text</code> returning an arbitrary Object. Some
   * formatters may return null.
   * <p>
   * If a <code>Pattern</code> has been specified and the text
   * completely matches the regular expression this will invoke
   * <code>setMatcher</code>.
   *
   * @throws ParseException if there is an error in the conversion
   * @param text String to convert
   * @return Object representation of text
   */
  public Object stringToValue(String text) throws ParseException {
    Pattern pattern = getPattern();
    
    if(text == null)
      return "";
    
    if (pattern != null) {
      Matcher matcher = pattern.matcher(text);
  
      if(maxLength > 0 && text.length() > maxLength)
        throw new ParseException("Pattern did not match", 0);
      
      if (matcher.matches()) {
        setMatcher(matcher);
        return super.stringToValue(text);
      }
      throw new ParseException("Pattern did not match", 0);
    }
    return text;
  }
}
