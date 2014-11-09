package mapReduce.parse;

import static org.junit.Assert.fail;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

public class WikiRedirectParser {
	public static Logger log = Logger.getLogger(WikiRedirectParser.class);
	public static final String ELEMENT   = "<.*?>";
	public static final String PAGE      = "";
	public static final String TITLE     = "<title>.*?</title>";
	public static final String TEXT      = "<text xml:space=\"preserve\">.*</text>";

	public static final String TABLE     = "\\{\\|[^\\{]*?\\|\\}";

	public static final String LINK      = "\\[\\[.*?\\]\\]";   					// [[*]]
	public static final String NORECLINK  = "\\[\\[[^\\]\\[]*?\\]\\]";              // [[  -[] +  ]]    nerekurzivna



	public static final String SIMPLELINK  = "\\[[^\\[]*?\\]";


	public static final String DEL      = "YXXXXXXY";
	public static final String SUBDEL   = "BAAAAB";
	public static final String DOCDEL   = ">>>>>>>>>>>>>>>>>>>>>>>";

	public static Pattern tittlePattern	    = Pattern.compile( TITLE,     Pattern.DOTALL | Pattern.MULTILINE);
	public static Pattern textPattern       = Pattern.compile( TEXT,      Pattern.DOTALL | Pattern.MULTILINE);
	public static Pattern linkPattern       = Pattern.compile( LINK );
	public static Pattern noRecPattern   	= Pattern.compile( NORECLINK );

	public static Pattern simpleLinkPattern = Pattern.compile( SIMPLELINK );
	public static Pattern tablePattern      = Pattern.compile( TABLE,     Pattern.DOTALL | Pattern.MULTILINE);

	private String title = null; 
	private Boolean shoudPrintl; 

	public String getTitle() {
		return title;
	}

	public String parse(String page) {
		return parse(page, Boolean.FALSE);
	}

	public String parse(String page, Boolean print) {
		title = null;
		page = StringEscapeUtils.unescapeHtml(page.replaceAll("nbsp;", " "));
		try {
			return parsePage(page, print);

		}catch (Exception e) {	
			if ( !print){
				log.error( "PARSE ERROR \n:",e);
				parse(page, Boolean.TRUE);
			}
			int a = 5/0;
		}
		return null;
	}


	public String parsePage(String page, Boolean print) {
		shoudPrintl = print;
		String redirect = " ";
		String subredirect = " ";
		String text = " ";
		page = page.replaceAll( "\\$", "Dolar" ).replaceAll( "\\\\", "\\\\\\\\" );
		info("*******************************************************");
		//info(page); info( "-------------------------");


		Matcher tittleMatcher = tittlePattern.matcher(page);
		if (tittleMatcher.find()) {
			title = tittleMatcher.group().replaceAll(ELEMENT, "").trim();
			info("TITLE: " + title);


			Matcher textMatcher = textPattern.matcher(page);
			if (textMatcher.find()) {
				text = textMatcher.group().replaceAll(ELEMENT, "").trim();
				//info("TEXT:  " + text);

				// REDIRECT

				if (text.toLowerCase().startsWith ("#redirect")  ) {
					Matcher redirectMatcher = linkPattern.matcher(text);
					if(redirectMatcher.find()) {
						String[] redirects = redirectMatcher.group().replaceAll("[\\[\\]]","").trim().split("#");
						if (1 == redirects.length) {
							redirect = title;
							title = redirects[0];
							text = " ";
							info("REDIRECT: " + redirect);

						} else if (2 == redirects.length) {
							title = redirects[0];
							subredirect = redirects[1];
							text = " ";
							info( "REDIRECT: " + redirect);								
							info( "REDIRECT title:    " + redirect);
							info( "REDIRECT subtitle: " + subredirect);

						} else {
							fail("Parse FAIL: more than 2 fields in redirect");
						}
					}
				}  
				else {   // not REDIRECT
					text = text.replaceAll("'''","").replaceAll("''","");

					text = parseTables( text);

					text = parseLinks( text);


					text = parseSimpleLinks(text);
					text = text.replaceAll("[\n]+", "\n");
					text = text.replaceAll("[\n][ ]*==", "\n\n==");
					text = text.replaceAll("\t", " ");
					info( "RESULT TEXT:" + text);
				}

			} else {
				if( !page.toLowerCase().contains("<text xml:space=\"preserve\" />")) { 
					log.error( page);

					fail("Parse FAIL: Tittle was found but not text");}
			}	
		}	
		StringBuffer resBuf = new StringBuffer();		
		resBuf.append(redirect);
		resBuf.append( WikiRedirectParser.DEL);
		resBuf.append(subredirect);
		resBuf.append( WikiRedirectParser.DEL);
		resBuf.append(text);
		resBuf.append( WikiRedirectParser.DOCDEL);
		info( "---------------------");
		info( resBuf.toString());
		return resBuf.toString();
	}



	protected String parseLinks(String text) {
		info("LINKS INPUT: " + text);
		Boolean match = Boolean.TRUE; 
		while (match) {
			match = Boolean.FALSE;
			info("LINK RECURSIVE CYCLE IN\n:");
			StringBuffer sb = new StringBuffer();
			Matcher linkMatcher = noRecPattern.matcher(text);

			while (linkMatcher.find()) {
				match = Boolean.TRUE;
				if(match) info("NOREC: (" + text.length() + ")" + text);
				String replacement = processLink( linkMatcher.group());
				info(linkMatcher.group() + " --->> " + replacement);
				info(sb.toString());
				linkMatcher.appendReplacement(sb, replacement);
			}
			if(match ){
				linkMatcher.appendTail(sb);
				text = sb.toString();	
			}
			info("LINK RECURSIVE CYCLE OUT:\n");

		}
		return text;
	}

	protected String processLink(String input) {
		info(" >>>>1 " + input);
		input = input.replaceAll("[\\[\\]]","");
		if( 1 < input.length()) {

			// :aaa -> aaa
			if (input.startsWith(":")){     
				input = input.substring(1);
			}

			// aaa:bbb| -> bbb
			if ( input.endsWith("|")) {		
				input = input.substring(input.lastIndexOf(':')+1, input.length()-1);
			}

			// aaa|bbb -> bbb
			input = input.substring(input.lastIndexOf('|')+1);    
		}
		info(" >>>>2 " + input);
		return input;
	}


	protected String parseTables(String text) {
		Boolean match = Boolean.TRUE; 
		while (match) {
			match = Boolean.FALSE;
			info("TABLE RECURSIVE CYCLE IN\n:");
			StringBuffer sb = new StringBuffer();
			Matcher tableMatcher = tablePattern.matcher(text);

			while (tableMatcher.find()) {
				//if(match) info("NOREC: " + text);
				match = Boolean.TRUE;
				String replacement = processTable( tableMatcher.group());
				tableMatcher.appendReplacement(sb, replacement);
				//info(tableMatcher.group() + " --->> " + replacement);
			}
			if(match ){
				tableMatcher.appendTail(sb);
				text = sb.toString();	
			}
			info("TABLE RECURSIVE CYCLE OUT:\n");

		}
		return text;
	}

	protected String processTable( String table) {
		table = table.substring(2, table.length()-2);
		info("TABLE1 : " + table);
		table = table.replaceAll("\\|\\|", "     ").replaceAll("!!", "     ").replaceAll("!", "");;
		String[] lines = table.split("\n");
		StringBuffer sb = new StringBuffer();
		for (String line : lines ) {
			line = line.substring(line.lastIndexOf('|')+1).trim();
			if( 1 != line.length() || !line.startsWith("-")){
				sb.append( line);
				sb.append("\n");
			}
		}

		table = sb.toString();
		info("TABLE2 : " + table);
		return "TABLE";
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
		subtitle = subtitle.replaceAll("\\(", "\\\\\\(").replaceAll("\\)", "\\\\\\)").replaceAll("–", "-");
		text = text.replaceAll("–", "-");

		String subpattern = "[^=\\p{L}]*?"+subtitle.trim()+ "[^=\\p{L}]*?";
		Pattern subtitlePattern   = Pattern.compile( "(?<CAPS>==+)" +subpattern+ "(\\k<CAPS>).+?(([^=]\\k<CAPS>[^=])|\\z)",   Pattern.DOTALL | Pattern.MULTILINE );

		Matcher subtitleMatcher = subtitlePattern.matcher(text);

		if (subtitleMatcher.find()) {
			//log.info("SUBUB: " + subtitle + ">>>>>" + subtitleMatcher.group().replaceAll("==", "") + "<<<<");		
			return subtitleMatcher.group().replaceAll("==", "");
		}else {
			log.info("\nREDUCER FAIL: Subtext for: " + subtitle + " NOT FOUND IN \n" + text);
		}
		return "";
	}



	protected void info(String msg) {
		if (shoudPrintl)
			log.info(msg);
	}

	protected void error(String msg, Throwable t) {
		if (shoudPrintl)
			log.error(msg, t);
	}

	protected void error(String msg) {
		if (shoudPrintl)
			log.error(msg);
	}
}
