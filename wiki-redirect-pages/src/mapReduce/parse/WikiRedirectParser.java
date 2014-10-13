package mapReduce.parse;

import static org.junit.Assert.fail;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class WikiRedirectParser {
	public static Logger log = Logger.getLogger(WikiRedirectParser.class);
	public static final String ELEMENT   = "<.*?>";
	public static final String PAGE      = "";
	public static final String TITLE     = "<title>.*?</title>";
	public static final String TEXT      = "<text xml:space=\"preserve\">.*</text>";
	public static final String LINK      = "\\[\\[.*?\\]\\]";
	public static final String INPUTLINK = "\\[\\[[^\\]\\[]*?:[^\\]\\[]*?\\]\\]";
	public static final String NAMELINK  = "\\[\\[[^:\\]\\[]*?\\]\\]"; 
	public static final String TABLE     = "\\{[^\\{]*?\\}";
	public static final String SIMPLELINK  = "\\[[^\\[]*?\\]";
	public static final String DEL      = "XXXXXX";
	public static final String SUBDEL   = "AAAA";
	public static final String DOCDEL   = ">>>>>>>>>>>>>>>>>>>>>>>";
	
	public static Pattern tittlePattern	    = Pattern.compile( TITLE,     Pattern.DOTALL | Pattern.MULTILINE);
	public static Pattern textPattern       = Pattern.compile( TEXT,      Pattern.DOTALL | Pattern.MULTILINE);
	public static Pattern linkPattern       = Pattern.compile( LINK );
	public static Pattern inputLinkPattern  = Pattern.compile( INPUTLINK );
	public static Pattern nameLinkPattern   = Pattern.compile( NAMELINK );
	public static Pattern simpleLinkPattern = Pattern.compile( SIMPLELINK );
	public static Pattern tablePattern      = Pattern.compile( TABLE,     Pattern.DOTALL | Pattern.MULTILINE);

	private String title = null; 

	public String getTitle() {
		return title;
	}

	public String parsePage(String page) {
			String redirect = " ";
			String subredirect = " ";
			String text = " ";
			
		
			//log.info( "*******************************************************");
			//log.info( page); log.info( "-------------------------");
			Matcher tittleMatcher = tittlePattern.matcher(page);
			if (tittleMatcher.find()) {
				title = tittleMatcher.group().replaceAll(ELEMENT, "").trim();
				//log.info("TITLE: " + title);
				

				Matcher textMatcher = textPattern.matcher(page);
				if (textMatcher.find()) {
					text = textMatcher.group().replaceAll(ELEMENT, "").trim();
					//log.info("TEXT:  " + text);

					// REDIRECT

					if (text.startsWith("#REDIRECT")) {
						Matcher redirectMatcher = linkPattern.matcher(text);
						if(redirectMatcher.find()) {
							String[] redirects = redirectMatcher.group().replaceAll("[\\[\\]]","").trim().split("#");
							if (1 == redirects.length) {
								redirect = title;
								title = redirects[0];
								text = " ";
								//log.info("REDIRECT: " + redirect);
								
							} else if (2 == redirects.length) {
								title = redirects[0];
								subredirect = redirects[1];
								text = " ";
								//log.info("REDIRECT: " + redirect);								
								//log.info("REDIRECT title:    " + redirect);
								//log.info("REDIRECT subtitle: " + subredirect);

							} else {
								fail("Parse FAIL: more than 2 fields in redirect");
							}
						}
					}  
					else {   // not REDIRECT
						text = text.replaceAll("'''","").replaceAll("''","");
						text = parseLinks( text);
						text = parseTable( text); 
						text = parseSimpleLinks(text);
						text = text.replaceAll("[\n]+", "\n");
						text = text.replaceAll("[\n][ ]*==", "\n\n==");
						text = text.replaceAll("\t", " ");
						//log.info("TEXT:" + text);
					}

				} else {
					log.error( page);
					fail("Parse FAIL: Tittle was found but not text");
				}	
			}	
			StringBuffer resBuf = new StringBuffer();		
			resBuf.append(redirect);
			resBuf.append( WikiRedirectParser.DEL);
			resBuf.append(subredirect);
			resBuf.append( WikiRedirectParser.DEL);
			resBuf.append(text);
			resBuf.append( WikiRedirectParser.DOCDEL);
			//log.info(resBuf.toString());
			return resBuf.toString();
	}


	protected String parseLinks(String text) {
		Boolean match = Boolean.TRUE; 
		while (match) {
			Matcher inputMatcher = inputLinkPattern.matcher(text);
			match = inputMatcher.find() ? Boolean.TRUE : Boolean.FALSE;
			text = inputMatcher.replaceAll("");

			Matcher nameMatcher = nameLinkPattern.matcher(text);
			StringBuffer sb = new StringBuffer();
			while (nameMatcher.find()) {
				match = Boolean.TRUE;
				String replacement = nameMatcher.group();
				replacement = replacement.substring( replacement.lastIndexOf('|')+1).replaceAll("[\\[\\]]","");
				nameMatcher.appendReplacement(sb, replacement);
				//log.info(nameMatcher.group() + " --->> " + replacement);
			}
			nameMatcher.appendTail(sb);
			text = sb.toString();	
		}
		return text;
	}


	protected String parseTable(String text) {
		Boolean match = Boolean.TRUE; 
		while (match) {
			Matcher tableMatcher = tablePattern.matcher(text);
			match = tableMatcher.find() ? Boolean.TRUE : Boolean.FALSE;
			text = tableMatcher.replaceAll("");		
		}
		return text;
	}

	protected String parseSimpleLinks(String text) {
		Matcher simpleLinkMatcher = simpleLinkPattern.matcher(text);
		StringBuffer sb = new StringBuffer();
		while (simpleLinkMatcher.find()) {
			String replacement = simpleLinkMatcher.group();
			replacement = replacement.replaceAll("[\\[\\]]", "");
			replacement = replacement.replaceFirst(".*? ", "").trim();	
			simpleLinkMatcher.appendReplacement(sb, replacement);
			//log.info(simpleLinkMatcher.group() + " --->> " + replacement);
		}
		simpleLinkMatcher.appendTail(sb);
		text = sb.toString();
		return text;
	}
	
	public String findSubtext(String text, String subtitle) {
		Pattern subtitlePattern   = Pattern.compile( "==+[^=\\p{L}]*?"+subtitle.trim()+ "[^=\\p{L}]*?==+.+?==+",   Pattern.DOTALL | Pattern.MULTILINE);
		Matcher subtitleMatcher = subtitlePattern.matcher(text);
		
		if (subtitleMatcher.find()) {
			//log.info("SUBUB: " + subtitle + ">>>>>" + subtitleMatcher.group().replaceAll("==", "") + "<<<<");		
			return subtitleMatcher.group().replaceAll("==", "");
		}else {
			fail("Parse FAIL: Subtitle was found: " + subtitle);
		}
		return "";
	}
	
}
