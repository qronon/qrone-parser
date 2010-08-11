package org.qrone.parser.inner;

import java.util.Hashtable;
import java.util.Map;

import org.qrone.StringTools;
import org.qrone.main.QrGNUSourceHighlight;
import org.qrone.main.QrLatexToPNG;
import org.qrone.parser.CompileTagLib;
import org.qrone.parser.Tag;
import org.qrone.parser.TagLib;
import org.qrone.parser.WikiLib;

public class WikiLibImpl implements WikiLib{
	public static int id = 0;

	public void addTag(Map map ,String name){
		addTag(map, name, new String[0], false);
	}

	public void addTag(Map map ,String name, boolean bool){
		addTag(map, name, new String[0], bool);
	}

	public void addTag(Map map ,String name ,String[] allowAttributes){
		addTag(map, name, allowAttributes, false);
	}
	
	public void addTag(final Map map, final String name,
			final String[] allowAttributes, final boolean bool){
		map.put(name, new TagLib(){
			public String startTag(Tag t, boolean started) {
				StringBuffer buf = new StringBuffer();
				buf.append("<" + name);
				for(int i=0; i<allowAttributes.length; i++){
					if(t.hasAttribute(allowAttributes[i])){
						buf.append(" " + allowAttributes[i] +
								"=\"" + t.getAttribute(allowAttributes[i]) + '"');
					}
				}
				buf.append(">");
				return buf.toString();
			}
			public String endTag(Tag t) {
				return "</" + name + ">";
			}
			public boolean isBlock(){
				return bool;
			}
		});
	}
	
	public Map installTags(){
		Map map = new Hashtable();
		
		String[] attr;
		attr= new String[2];
		attr[0] = "href";
		attr[1] = "name";
		addTag(map, "a", attr);
		addTag(map, "b");
		addTag(map, "u");
		addTag(map, "i");
		addTag(map, "s");
		addTag(map, "strike");
		addTag(map, "sup");
		addTag(map, "sup");
		addTag(map, "rb");
		addTag(map, "rp");
		addTag(map, "ruby");
		addTag(map, "marquee");
		map.put("br",new TagLib(){
			public String startTag(Tag t, boolean started) {
				return "<br/>";
			}
			public String endTag(Tag t) { return ""; }
			public boolean isBlock(){
				return false;
			}});
		attr = new String[2];
		attr[0] = "size";
		attr[1] = "color";
		addTag(map, "font", attr);
		addTag(map, "center");

		map.put("math",new CompileTagLib(){
			public String bodyTag(Tag t, String content) {
				try {
					return "<img src=\"http://www.qrone.org/qreditor/" 
						+ QrLatexToPNG.latex2png(content) + "\" style=\"margin:3px;\" border=\"0\" align=\"absmiddle\"/>";
				} catch (Exception e) {
					return "ERROR!!";
				}
			}
			
			public String startTag(Tag t, boolean started) { return ""; }
			public String endTag(Tag t) { return ""; }
			public boolean isBlock(){
				return false;
			}});

		map.put("d",new CompileTagLib(){
			public String bodyTag(Tag t, String content) {
				StringBuffer buf = new StringBuffer();
				buf.append("<ruby><rb>");
				buf.append(content);
				buf.append("</rb><rp>(</rp><rt>");
				buf.append(StringTools.xStr('・',content.length()));
				buf.append("</rt><rp>)</rp></ruby>");
				return buf.toString();
			}
			
			public String startTag(Tag t, boolean started) { return ""; }
			public String endTag(Tag t) { return ""; }
			public boolean isBlock(){
				return false;
			}});
		
		map.put("code",new CompileTagLib(){
			public String bodyTag(Tag t, String content) {
				try {
					if(t.hasAttribute("lang"))
						return QrGNUSourceHighlight.sourceHighlight(
								t.getAttribute("lang").toLowerCase(),content);
					else
						return QrGNUSourceHighlight.sourceHighlight(
								"html",content);
				} catch (Exception e) {
					return "ERROR!!";
				}
			}
			
			public String startTag(Tag t, boolean started) { return ""; }
			public String endTag(Tag t) { return ""; }
			public boolean isBlock(){
				return true;
			}});
		TagLib raw = new CompileTagLib(){
			public String bodyTag(Tag t, String content) {
				return content;
			}

			public String startTag(Tag t, boolean started) { return ""; }
			public String endTag(Tag t) { return ""; }
			public boolean isBlock() {
				return false;
			}};
		map.put("raw",raw);
		map.put("nowiki",raw);
		
		map.put("hidden",new TagLib(){
			public String startTag(Tag t, boolean started) {
				StringBuffer buf = new StringBuffer();
				buf.append("<a href=\"javascript:");
				buf.append("var t = document.getElementById('");
				buf.append("QrONEWiki#switch" + ++id + "').style;");
				buf.append("if(t.display == 'none'){");
				buf.append("t.display = '';");
				buf.append("}else{");
				buf.append("t.display = 'none';");
				buf.append("}");
				buf.append(";void(0);\">");
				if(t.hasAttribute("as")){
					buf.append(t.getAttribute("as"));
				}else{
					buf.append("more...");
				}
				buf.append("</a>\n<div style=\"display:none\" id=\"");
				buf.append("QrONEWiki#switch" + id);
				buf.append("\">");
				return buf.toString();
			}

			public String endTag(Tag t) {
				return "</div>";
			}

			public boolean isBlock(){
				return false;
			}});

		
		map.put("blockquote",new TagLib(){
			public String startTag(Tag t, boolean started) {
				StringBuffer buf = new StringBuffer();
				buf.append("<div class=\"blockquote\">");
				return buf.toString();
			}

			public String endTag(Tag t) {
				StringBuffer buf = new StringBuffer();
				
				if(t.hasAttribute("cite") && t.hasAttribute("title")){
					buf.append("<br>\n<cite><a href=\"");
					buf.append(t.getAttribute("cite"));
					buf.append("\">");
					buf.append(t.getAttribute("title"));
					buf.append("</a></cite>");
				}else if(t.hasAttribute("cite")){
					buf.append("<br>\n<cite><a href=\"");
					buf.append(t.getAttribute("cite"));
					buf.append("\">");
					buf.append(t.getAttribute("cite"));
					buf.append("</a></cite>");
				}else if(t.hasAttribute("title")){
					buf.append("<br>\n<cite>");
					buf.append(t.getAttribute("title"));
					buf.append("</cite>");
				}
				
				buf.append("</div>");
				return buf.toString();
			}

			public boolean isBlock(){
				return false;
			}});
		

		map.put("hidden",new TagLib(){
			public String startTag(Tag t, boolean started) {
				StringBuffer buf = new StringBuffer();
				buf.append("<a href=\"javascript:");
				buf.append("var t = document.getElementById('");
				buf.append("QrONEWiki#switch" + ++id + "').style;");
				buf.append("if(t.display == 'none'){");
				buf.append("t.display = '';");
				buf.append("}else{");
				buf.append("t.display = 'none';");
				buf.append("}");
				buf.append(";void(0);\">");
				if(t.hasAttribute("as")){
					buf.append(t.getAttribute("as"));
				}else{
					buf.append("more...");
				}
				buf.append("</a>\n<div style=\"display:none\" id=\"");
				buf.append("QrONEWiki#switch" + id);
				buf.append("\">");
				return buf.toString();
			}

			public String endTag(Tag t) {
				return "</div>";
			}

			public boolean isBlock(){
				return false;
			}});

		map.put("design",new TagLib(){
			public String startTag(Tag t, boolean started) {
				StringBuffer buf = new StringBuffer();
				String skinClass = "normal";
				if(t.hasAttribute("skin")){
					skinClass = t.getAttribute("skin");
				}
				
				if(t.hasAttribute("align") && 
						t.getAttribute("align").equals("center")){
					buf.append("\n<div class=\"QrDesignSkin\" align=\"center\">\n");
				}else{
					buf.append("\n<div class=\"QrDesignSkin\">\n");
				}
				
				String style = "margin:10px;";
				if(t.hasAttribute("width")){
					style += "width:" + t.getAttribute("width") + ";";
				}else{
					style += "width:100%;";
				}
				if(t.hasAttribute("align")){
					if(t.getAttribute("align").equals("right")){
						style += "float:right;";
					}else if(t.getAttribute("align").equals("left")){
						style += "float:left;";
					}
				}
				
				buf.append("<div class=\"" + skinClass + "\"");
				if(!style.equals("")){
					buf.append(" style=\""+style+"\"");
				}
				buf.append(">");
				buf.append("<div class=\"layerA\">");
				buf.append("<div class=\"layerB\">");
				buf.append("<div class=\"layerC\">");
				buf.append("<div class=\"layerD\">");
				buf.append("<div class=\"layerE\">");
				buf.append("<div class=\"layerF\">");
				buf.append("<div class=\"layerG\">");
				buf.append("<div class=\"layerH\">");
				buf.append("<div class=\"layerI\">");
				buf.append("<div class=\"layerJ\" align=\"left\">\n");
				return buf.toString();
			}

			public String endTag(Tag t) {
				return	"\n</div></div></div></div></div></div>" +
						"</div></div></div></div></div></div>\n";
			}

			public boolean isBlock(){
				return true;
			}});
		
		return map;
	}
	
	public String compileBlock(String type, String data){
		StringBuffer buf = new StringBuffer();
		if(type.equals("[[")){
			if(data.toLowerCase().startsWith("google:image:")){
				buf.append("<a href=\"http://images.google.com/images?hl=ja&lr=lang_ja&ie=UTF-8&oe=UTF-8&q=");
				buf.append(data.substring(13));
				buf.append("&num=50&sa=N&tab=wi\">Google:image: ");
				buf.append(data.substring(13));
				buf.append("</a>");
			}else if(data.toLowerCase().startsWith("google:news:")){
				buf.append("<a href=\"http://news.google.com/news?hl=ja&lr=lang_ja&ie=UTF-8&oe=UTF-8&q=");
				buf.append(data.substring(12));
				buf.append("&num=50&sa=N&tab=in\">Google:News: ");
				buf.append(data.substring(12));
				buf.append("</a>");
			}else if(data.toLowerCase().startsWith("google:")){
				buf.append("<a href=\"http://www.google.com/search?hl=ja&lr=lang_ja&ie=UTF-8&oe=UTF-8&q=");
				buf.append(data.substring(7));
				buf.append("&num=50\">Google: ");
				buf.append(data.substring(7));
				buf.append("</a>");
			}else if(data.toLowerCase().indexOf("img(")>=0){
				buf.append("<img src=\"");
				if(data.lastIndexOf(")")>=0){
					buf.append(data.substring(
							data.indexOf("img(")+4,
							data.lastIndexOf(")")
						));
				}else{
					buf.append(data.substring(
							data.indexOf("img(")+3
						));
					
				}
				buf.append("\" style=\"margin:3px;\" border=\"0\" align=\"center\"");
			}else{
				buf.append("<a href=\"");
				buf.append(data);
				buf.append("\">");
				buf.append(data);
				buf.append("</a>");
			}
		}else if(type.equals("[")){
			if(data.indexOf(" ")>=0){
				buf.append("<a href=\"");
				buf.append(data.substring(0,data.indexOf(" ")));
				buf.append("\">");
				buf.append(data.substring(data.indexOf(" ")+1));
				buf.append("</a>");
			}else{
				buf.append("<a href=\"");
				buf.append(data);
				buf.append("\">");
				buf.append(data);
				buf.append("</a>");
			}
		}
		return buf.toString();
	}

	public String openBlock(char c){
		StringBuffer htmloutput = new StringBuffer();
		switch(c){
		case 'f':
			htmloutput.append("<div class=\"QrONEWiki\">\n");
			break;
		case 'p':
			//htmloutput.append("<p>");
			break;
		case ' ':
			htmloutput.append("<pre><tt>");
			break;
		case '*':
			htmloutput.append("<ul type=\"square\">\n<li>");
			break;
		case '#':
			htmloutput.append("<ol><li>");
			break;
		case '+':
			htmloutput.append("<ol><li>");
			break;
		case '-':
			htmloutput.append("<ul><li>");
			break;
		case '>':
		case ':':
			htmloutput.append("<div style=\"margin-left:3em;\">");
			break;
		case '：':
			htmloutput.append("<div>");
			break;
		case '「':
			break;
		case '」':
			htmloutput.append("<div style=\"padding:0px; padding-left:3em; text-indent:-1em;\">");
			break;
		case ';':
			htmloutput.append("<dl>");
			break;
		case 't':
			htmloutput.append("<dt>");
			break;
		case 'd':
			htmloutput.append("<dd>");
			break;
		case 'b':
			htmloutput.append("<b>");
			break;
		case 'i':
			htmloutput.append("<div class=\"indent-box1\">");
			break;
		case 'j':
			htmloutput.append("<div class=\"indent-box2\">");
			break;
		case 'e':
			htmloutput.append("<em>");
			break;
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
			htmloutput.append("<h"+c+">");
			break;
		case 'h':
			htmloutput.append("<ins>");
			break;
		case '|':
			htmloutput.append("<table align=\"center\">\n<tr>");
			break;
		case 'c':
			htmloutput.append("<td>");
			break;
		case 'o':
			htmloutput.append("<td class=\"head-cell\">");
			break;
		}
		return htmloutput.toString();
	}
	
	public String dimBlock(char c){
		StringBuffer htmloutput = new StringBuffer();
		switch(c){
		case '*':
		case '#':
		case '+':
		case '-':
			htmloutput.append("</li>\n<li>");
			break;
		case ':':
			htmloutput.append("<br/>\n");
			break;
		case '\n':
			htmloutput.append("<br/>\n");
			break;
		case '|':
			htmloutput.append("</tr>\n<tr>");
			break;
		case 'r':
			htmloutput.append("<hr>\n");
			break;
		case 's':
			htmloutput.append("<hr size=\"10\" width=\"10\" style=\"margin:20px;\">\n");
			break;
		case 't':
			htmloutput.append("<hr>\n");
			break;
		case 'u':
			htmloutput.append("<hr size=\"10\" width=\"10\" style=\"margin:20px;\">\n");
			break;
		}
		return htmloutput.toString();
	}
	
	public String closeBlock(char c){
		StringBuffer htmloutput = new StringBuffer();
		switch(c){
		case 'f':
			htmloutput.append("</div>\n");
			break;
		case 'p':
			//htmloutput.append("</p>");
			break;
		case ' ':
			htmloutput.append("</tt></pre>");
			break;
		case '*':
			htmloutput.append("</li>\n</ul>");
			break;
		case '#':
			htmloutput.append("</li>\n</ol>");
			break;
		case '+':
			htmloutput.append("</li>\n</ol>");
			break;
		case '-':
			htmloutput.append("</li>\n</ul>");
			break;
		case '>':
		case ':':
			htmloutput.append("</div>");
			break;
		case '：': // 2byte
			htmloutput.append("</div>");
			break;
		case '「': // 2byte
			break;
		case '」': // 2byte
			htmloutput.append("</div>");
			break;
		case ';':
			htmloutput.append("</dl>");
			break;
		case 't':
			htmloutput.append("</dt>");
			break;
		case 'd':
			htmloutput.append("</dd>");
			break;
		case 'b':
			htmloutput.append("</b>");
			break;
		case 'i':
			htmloutput.append("</div>");
			break;
		case 'j':
			htmloutput.append("</div>");
			break;
		case 'e':
			htmloutput.append("</em>");
			break;
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
			htmloutput.append("</h"+c+">");
			break;
		case 'h':
			htmloutput.append("</ins>");
			break;
		case '|':
			htmloutput.append("</tr>\n</table>");
			break;
		case 'c':
			htmloutput.append("</td>");
			break;
		case 'o':
			htmloutput.append("</td>");
			break;
		}
		return htmloutput.toString();
	}
}
