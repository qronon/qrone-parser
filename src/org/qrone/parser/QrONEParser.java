package org.qrone.parser;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.qrone.StringTools;
import org.qrone.designpattern.Dispatcher;
import org.qrone.designpattern.block.Block;
import org.qrone.designpattern.block.ConditionBlock;
import org.qrone.parser.inner.WikiLibImpl;

public class QrONEParser {
	private String sourceStr;
	private char[] source;
	private List taglist = new ArrayList();
	
	private Map tagLib = new Hashtable();
	
	private StringBuffer htmloutput;
	private WikiParserPart wikipart;
	private WikiLib wikilib;
	
	public QrONEParser(String str){
		sourceStr = str;
		
		source = str.toCharArray();
		htmloutput = new StringBuffer();
		wikipart = new WikiParserPart();
		wikilib = new WikiLibImpl();
		
		tagLib.putAll(wikilib.installTags());
	}

	public void setWikiLib(WikiLib wikilib){
		this.wikilib = wikilib;;
	}
	
	public void addTagLib(String name, TagLib lib){
		tagLib.put(name,lib);
	}

	public void removeTagLib(String name){
		tagLib.remove(name);
	}
	
	public void write(String html){
		htmloutput.append(html);
	}

	private void escapeWrite(char c){
		switch(c){
		case '\r':
			break;
		case '\n':
			htmloutput.append("<br/>\n");
			break;
		case '\\':
			nowIndex++;
			switch(source[nowIndex]){
			case 't':
				htmloutput.append('\t');
				break;
			case 'n':
				htmloutput.append('\n');
				break;
			default:
				escapeWrite(nowIndex);
			}
			break;
		case '<':
			htmloutput.append("&lt;");
			break;
		case '>':
			htmloutput.append("&gt;");
			break;
		default:
			htmloutput.append(c);
			break;
		}
	}
	private void escapeWrite(int index){
		if(source[index]!='\r')
			wikipart.onChar(index);
	}
	private void escapeWrite(int from, int length){
		for(int i=from;i<from+length;i++){
			escapeWrite(i);
		}
	}
	
	public String output(){
		return htmloutput.toString();
	}

	private int nowIndex = 0;
	public void parse(){
		int pendingIndex = 0;
		
		boolean isEndTag = false;
		boolean isStartEndTag = false;
		boolean tagStarted = false;
		boolean tagNameStarted = false;
		boolean attributeStarted = false;
		boolean attributeValueStarted = false;
		
		boolean hasAttributeValueQuote = false;
		char attributeValueQuote = '"';
		boolean attributeValueEscaped = false;
		
		StringBuffer tagName = null;
		StringBuffer attrName = null;
		StringBuffer attrValue = null;
		Map attrMap = null;
		
		boolean compileBlockStarted = false;
		StringBuffer compileTagOutput = null;
		
		wikipart.onStart();
		for(nowIndex = 0; nowIndex < source.length; nowIndex++){
			if(tagStarted){
				if(attributeValueStarted){
					// 属性値の指定中
					 if(attributeValueEscaped){
						switch(source[nowIndex]){
						case 'n':
							attrValue.append('\n');
							break;
						case 't':
							attrValue.append('\t');
							break;
						default:
							attrValue.append("\\");
							attrValue.append(source[nowIndex]);
						 	break;
						}
						
						attributeValueEscaped = false;
							
					}else if(attrValue.length() == 0
							&& (source[nowIndex] == '"' || source[nowIndex] == '\'')){
						// 属性名の最後で、属性値の最初
						hasAttributeValueQuote = true;
						attributeValueQuote = source[nowIndex];
					}else if(source[nowIndex] == '\\'){
						attributeValueEscaped = true;
						
					}else if(hasAttributeValueQuote 
							&& attributeValueQuote == source[nowIndex]){
						// 属性値の最後
						attrMap.put(attrName.toString().toLowerCase(),
								attrValue.toString());
						attrName = new StringBuffer();
						attributeValueStarted = false;
					}else if(!isWhiteCharactor(source[nowIndex])){
						// 属性名の途中
						attrValue.append(source[nowIndex]);
					}else if(attrValue.length() > 0){
						attrMap.put(attrName.toString().toLowerCase(),
								attrValue.toString());
						attrName = new StringBuffer();
						attributeValueStarted = false;
					}
					
					if(!attributeValueEscaped && 
							!hasAttributeValueQuote &&
							attrValue.length() > 0 &&
							nowIndex < source.length &&
							source[nowIndex+1] == '>'){
						// 属性値の最後
						attrMap.put(attrName.toString().toLowerCase(),
								attrValue.toString());
						attrName = new StringBuffer();
						attributeValueStarted = false;
					}
					
				}else if(source[nowIndex] == '>'){
					// タグが終わった
					tagStarted = false;
					attributeStarted = false;
					Tag tag = null;
					
					if(tagName.toString().endsWith("/")){
						tagName.deleteCharAt(tagName.length()-1);
						isStartEndTag = true;
					}
					
					if(tagLib.containsKey(tagName.toString().toLowerCase())){
						
						if(isEndTag){
							for(int i = taglist.size()-1;i>=0;i--){
								if(((Tag)taglist.get(i)).getNodeName()
										.equals(tagName.toString().toLowerCase())){
									tag = (Tag)taglist.remove(i);
									break;
								}
							}
							if(tag == null){
								escapeWrite(pendingIndex,nowIndex-pendingIndex+1);
								isEndTag = false;
								isStartEndTag = false;
								continue;
							}
						}else{
							tag = createTag(tagName.toString().toLowerCase(),attrMap);
							taglist.add(tag);
						}

						TagLib lib = 
							(TagLib)tagLib.get(tagName.toString().toLowerCase());
						boolean linetopbuf = false;
						if(wikipart.lineStart){
							if(lib.isBlock())
								wikipart.onBlockEndcap(nowIndex,'?');
							else
								wikipart.onBlockEndcap(nowIndex,'n');
								
							linetopbuf=true;
						}
						
						if(!isEndTag){
							write(lib.startTag(tag,linetopbuf));
							if(lib instanceof CompileTagLib){
								CompileTagLib clib = (CompileTagLib)lib;
								
								if(isStartEndTag){
									write(clib.bodyTag(tag,null));
								}else{
									int compileEndIndex = sourceStr.indexOf(
											"</" +tagName.toString() + ">", nowIndex);
									
									if(compileEndIndex < 0){
										write(clib.bodyTag(tag,null));
									}else{
										write(clib.bodyTag(tag,sourceStr.substring(
												nowIndex+1,compileEndIndex)));
									}
									nowIndex = compileEndIndex + tagName.length()+2;
								}
							}
						}
						
						if(isEndTag || isStartEndTag ||
								lib instanceof CompileTagLib){
							write(lib.endTag(tag));
							if(lib.isBlock() && wikipart.thiscap == 'n'){
								if(source.length>nowIndex+1 &&
										source[nowIndex+1] == '\n'){
									nowIndex++;
								}
								if(source.length>nowIndex+1 &&
										source[nowIndex+1] == '\n'){
									nowIndex++;
								}
							}
						}
					}else{
						escapeWrite(pendingIndex,nowIndex-pendingIndex+1);
					}
					
					isEndTag = false;
					isStartEndTag = false;
					
				}else if(source[nowIndex] == '<'){
					// タグが終わっていないのに次のタグが始まった
					escapeWrite(pendingIndex,nowIndex-pendingIndex);
					pendingIndex = nowIndex;
					
					attributeStarted = false;
					tagName = new StringBuffer();
				}else if(tagNameStarted){
					// タグ名の指定中
					if(!isWhiteCharactor(source[nowIndex])){
						if(tagName.length()==0 && source[nowIndex] == '/'){
							isEndTag = true;
						}else{
							tagName.append(source[nowIndex]);
						}
						
					}else if(tagLib.containsKey(tagName.toString().toLowerCase())){
						// タグ名の最後のスペース、そこまでのタグに一致がある
						tagNameStarted = false;
						attributeStarted = true;
						attrName = new StringBuffer();
					}else{
						// タグが発見できなかった
						escapeWrite(pendingIndex,nowIndex-pendingIndex);
						tagStarted = false;
						tagNameStarted = false;
					}
				}else if(attributeStarted){
					if(source[nowIndex] == '='){
						// 属性名の最後で、属性値の最初
						attributeValueStarted = true;
						hasAttributeValueQuote = false;
						attrValue = new StringBuffer();
					}else if(!isWhiteCharactor(source[nowIndex])){
						// 属性名の途中
						attrName.append(source[nowIndex]);
					}else if(attrName.length() > 0 && 
							isWhiteCharactor(source[nowIndex])){
						attrMap.put(attrName.toString().toLowerCase(),"true");
						attrName = new StringBuffer();
					}
					
					if(attrName.length() > 0 &&
							nowIndex < source.length &&
							source[nowIndex+1] == '>'){
						// 属性名の最後
						if(attrName.equals("/")){
							// '/' のみの属性は終了済み開始タグを示す。
							isStartEndTag = true;
						}
						attrMap.put(attrName.toString().toLowerCase(),"true");
						attrName = new StringBuffer();
					}
				}
				
				
			}else if(source[nowIndex] == '<'){
				// タグの開始を発見
				tagStarted = true;
				tagNameStarted = true;
				pendingIndex = nowIndex;
				tagName = new StringBuffer();
				attrMap = new Hashtable();
			}else{
				// タグの外側
				escapeWrite(nowIndex);
			}
		}
		if(tagStarted)
			escapeWrite(pendingIndex,nowIndex-pendingIndex);

		for(int i = taglist.size()-1;i>=0;i--){
			write(((TagLib)tagLib.get(((Tag)taglist.get(i)).getNodeName()))
				.endTag(null));
		}
		wikipart.onEnd();
	}

	public Tag createTag(final String name, final Map attrmap){
		return new Tag(){
			public String getNodeName() {
				return name;
			}
			
			public boolean hasAttribute(String attr){
				return attrmap.containsKey(attr);
			}
			
			public String getAttribute(String attr) {
				if(attrmap.containsKey(attr))
					return (String)attrmap.get(attr);
				else
					return null;
			}

			public Map getAttributes() {
				return attrmap;
			}
		};
	}
	public boolean isWhiteCharactor(char c){
		if(c == ' ' || c == '\t'){
			return true;
		}
		return false;
	}
	
	private class WikiParserPart{
		int skip = 0;
		
		boolean lineStart = true;
		boolean topMatch  = true;
		StringBuffer lineStartMarker = new StringBuffer();
		StringBuffer lineStartMarkerBuf = new StringBuffer();
		
		boolean tableStart = false;
		
		boolean preStart = false;
		boolean defStart = false;
		boolean defnameStart = false;
		boolean defdescStart = false;
		
		boolean jtalkStart = false;
		boolean jtalkNameStart = false;
		boolean jtalkCommentStart = false;
		
		boolean bStarted = false;
		boolean emStarted = false;
		boolean box1Started = false;
		boolean box2Started = false;
		
		boolean hInsOpen = false;
		int hStartNum = 0;
		char lastcap = 'f';
		char thiscap = 'f';
		
		int indentNum = 0;
		
		public void onStart(){
			openBlock('f');
		}
		
		public void onEnd(){
			for(int i=lineStartMarkerBuf.length()-1;
				i>=lineStartMarker.length();
				i--){
				closeBlock(lineStartMarkerBuf.charAt(i));
			}

			if(defdescStart){
				closeBlock('d');
				defdescStart = false;
			}else if(defnameStart){
				closeBlock('t');
				defnameStart = false;
			}

			if(jtalkCommentStart){
				closeBlock('」');
				jtalkCommentStart = false;
			}else if(jtalkNameStart){
				closeBlock('「');
				jtalkNameStart = false;
			}
			
			if(defStart){
				closeBlock(';');
				defStart = false;
			}else if(jtalkStart){
				closeBlock('：');
				jtalkStart = false;
			}else if(preStart){
				closeBlock(' ');
				preStart = false;
			}else if(tableStart){
				closeBlock('c');
				closeBlock('|');
				tableStart = false;
			}
			
			if(hInsOpen){
				closeBlock('h');
				hInsOpen = false;
			}
			if(hStartNum != 0){
				closeBlock(Character.forDigit(hStartNum,10));
				hStartNum = 0;
			}

			closeBlock('f');
		}
		
		public void onBlockEndcap(int nowIndex, char nextcap){
			lineStart = false;
			lineStartMarkerBuf = lineStartMarker;
			lineStartMarker = new StringBuffer();
			
			switch(thiscap){
			case 'f':
				switch(nextcap){
				case '\n':
					lineStart = true;
					break;
				}
				break;
			case 'n':
				switch(nextcap){
				case 'n':
					dimBlock('\n');
					break;
				case '\n':
					dimBlock('\n');
					break;
				case '=':
				case ';':
				case '：': // 2byte
				case ' ':
					htmloutput.append('\n');
					break;
				}
				closeBlock('p');
				break;
			case '\n':
				switch(nextcap){
				case '\n':
				case 'n':
					switch(lastcap){
					case '\n':
					case 'n':
						dimBlock('\n');
						break;
					}
					break;
				case '=':
				case ';':
				case '：': // 2byte
				case ' ':
					htmloutput.append('\n');
					break;
				}
				break;
			case '=':
				if(hInsOpen){
					closeBlock('h');
					hInsOpen = false;
				}
				if(hStartNum != 0){
					closeBlock(Character.forDigit(hStartNum,10));
					hStartNum = 0;
				}
				htmloutput.append('\n');
				break;
			case ';':
				if(defdescStart){
					closeBlock('d');
					defdescStart = false;
				}else if(defnameStart){
					closeBlock('t');
					defnameStart = false;
				}
				if(nextcap != ';'){
					closeBlock(';');
					defStart = false;
				}
				htmloutput.append('\n');
				break;
			case '：': // 2byte
				if(jtalkCommentStart){
					closeBlock('」');
					jtalkCommentStart = false;
				}else if(jtalkNameStart){
					closeBlock('「');
					jtalkNameStart = false;
				}
				if(nextcap != '：'){
					closeBlock('：');
					jtalkStart = false;
				}
				htmloutput.append('\n');
				break;
			case ' ':
				if(nextcap != ' '){
					closeBlock(' ');
					preStart = false;
				}
				htmloutput.append('\n');
				break;
			case '|':
				if(nextcap != '|'){
					closeBlock('c');
					closeBlock('|');
					tableStart = false;
				}else{
					closeBlock('c');
					dimBlock('|');
					
					if(source.length>nowIndex+1 &&
							source[nowIndex+1] == '*'){
						nowIndex++;
						stepForword(1);
						openBlock('o');
					}else{
						openBlock('c');
					}
				}
				break;
			}
			
			if(nextcap == 'n'){
				openBlock('p');
			}else if(nextcap == '\n'){
				lineStart = true;
			}
			lastcap = thiscap;
			thiscap = nextcap;
		}

		private void stepForword(int n){
			skip += n;
		}
		
		public void onChar(int nowIndex){
			if(skip > 0){
				skip--;
				return;
			}
			if(lineStart){
				switch(source[nowIndex]){
				case '#':
					// 仕切り
					if(sourceStr.indexOf("####", nowIndex) == nowIndex){
						onBlockEndcap(nowIndex,'u');
						dimBlock('u');
						nowIndex += 3;
						stepForword(3);
						if(source.length>nowIndex+1 && source[nowIndex+1] == '\n'){
							nowIndex++;
							stepForword(1);
						}
						return;
					}
				case '*':
					// 仕切り
					if(sourceStr.indexOf("****", nowIndex) == nowIndex){
						onBlockEndcap(nowIndex,'r');
						dimBlock('r');
						nowIndex += 3;
						stepForword(3);
						if(source.length>nowIndex+1 && source[nowIndex+1] == '\n'){
							nowIndex++;
							stepForword(1);
						}
						return;
					}
				case '+':
					// 仕切り
					if(sourceStr.indexOf("++++", nowIndex) == nowIndex){
						onBlockEndcap(nowIndex,'t');
						dimBlock('t');
						nowIndex += 3;
						stepForword(3);
						if(source.length>nowIndex+1 && source[nowIndex+1] == '\n'){
							nowIndex++;
							stepForword(1);
						}
						return;
					}
				case '-':
					// 仕切り
					if(sourceStr.indexOf("----", nowIndex) == nowIndex){
						onBlockEndcap(nowIndex,'s');
						dimBlock('s');
						nowIndex += 3;
						stepForword(3);
						if(source.length>nowIndex+1 && source[nowIndex+1] == '\n'){
							nowIndex++;
							stepForword(1);
						}
						return;
					}
				case ':':
					lineStartMarker.append(source[nowIndex]);
					
					if(topMatch && 
							lineStartMarkerBuf.length() >= lineStartMarker.length()&& 
							source[nowIndex]
							== lineStartMarkerBuf.charAt(lineStartMarker.length()-1)){
						
						
					}else if(topMatch){
						topMatch = false;
						for(int i=lineStartMarkerBuf.length()-1;
							i>=lineStartMarker.length()-1;
							i--){
							closeBlock(lineStartMarkerBuf.charAt(i));
							htmloutput.append('\n');
						}
						
						htmloutput.append('\n');
						openBlock(source[nowIndex]);
					}else{
						htmloutput.append('\n');
						openBlock(source[nowIndex]);
					}
					return;
				}
				
				for(int i=lineStartMarkerBuf.length()-1;
					i>=lineStartMarker.length();
					i--){
					closeBlock(lineStartMarkerBuf.charAt(i));
					htmloutput.append('\n');
				}
				
				// <Table> ブロック
				if(lineStartMarker.length()==0 && source[nowIndex] == '|'){
					onBlockEndcap(nowIndex,'|');
					if(!tableStart){
						openBlock('|');
						if(source.length>nowIndex+1 &&
								source[nowIndex+1] == '*'){
							nowIndex++;
							stepForword(1);
							openBlock('o');
						}else{
							openBlock('c');
						}
						tableStart = true;
					}
					return;
				// ２バイト会話 ブロック
				}else if(lineStartMarker.length()==0 && source[nowIndex] == '：'){
					onBlockEndcap(nowIndex,'：'); // 2byte
					
					if(!jtalkStart){
						openBlock('：');
						jtalkStart = true;
					}
					jtalkNameStart = true;
					jtalkCommentStart = false;

					openBlock('「');
					return;
				// <PRE> ブロック
				}else if(lineStartMarker.length()==0 && source[nowIndex] == ' '){
					onBlockEndcap(nowIndex,' ');
					
					if(!preStart){
						openBlock(' ');
						preStart = true;
					}
					return;
				// 定義リストブロック
				}else if(lineStartMarker.length()==0 && source[nowIndex] == ';'){
					onBlockEndcap(nowIndex,';');
					
					if(!defStart){
						openBlock(';');
						defStart = true;
					}
					defnameStart = true;
					defdescStart = false;
					
					openBlock('t');
					return;
				// 強制インデント開始
				}else if(source[nowIndex] == '>' &&
						sourceStr.indexOf(">>", nowIndex) == nowIndex){
					onBlockEndcap(nowIndex,'n');
					openBlock('>');
					nowIndex += 1;
					stepForword(1);
					indentNum++;
					
					if(source.length>nowIndex+1 && source[nowIndex+1] == '\n'){
						nowIndex++;
						stepForword(1);
					}
					return;
				// 強制インデント終了
				}else if(source[nowIndex] == '<' &&
						sourceStr.indexOf("<<", nowIndex) == nowIndex && indentNum > 0){
					onBlockEndcap(nowIndex,'n');
					closeBlock('>');
					nowIndex += 1;
					stepForword(1);
					indentNum--;
					
					if(source.length>nowIndex+1 && source[nowIndex+1] == '\n'){
						nowIndex++;
						stepForword(1);
					}
					return;
				// ヘッダ行
				}else if(source[nowIndex] == '='){
					onBlockEndcap(nowIndex,'=');
					
					if(hStartNum == 0){
						if(sourceStr.indexOf("=====", nowIndex) == nowIndex){
							openBlock('5');
							nowIndex += 4;
							stepForword(4);
							hStartNum = 5;
						}else if(sourceStr.indexOf("====", nowIndex) == nowIndex){
							openBlock('4');
							nowIndex += 3;
							stepForword(3);
							hStartNum = 4;
						}else if(sourceStr.indexOf("===", nowIndex) == nowIndex){
							openBlock('3');
							nowIndex += 2;
							stepForword(2);
							hStartNum = 3;
						}else if(sourceStr.indexOf("==", nowIndex) == nowIndex){
							openBlock('2');
							nowIndex += 1;
							stepForword(1);
							hStartNum = 2;
						}else{
							openBlock('1');
							hStartNum = 1;
						}
					}
					return;
				// 行頭が通常文字
				}else{
					if(lineStartMarker.length()>0 && 
							lineStartMarkerBuf.length()>0 &&
							lineStartMarker.length() <= 
								lineStartMarkerBuf.length()){
						dimBlock(lineStartMarker.charAt(
								lineStartMarker.length()-1));
					}
					
					if(lineStartMarker.length()>0){
						onBlockEndcap(nowIndex,
								lineStartMarker.charAt(
										lineStartMarker.length()-1));
					}else if(source[nowIndex] == '\n'){
						onBlockEndcap(nowIndex,'\n');
						return;
					}else{
						onBlockEndcap(nowIndex,'n');
					}
				}
			// 行末
			}else if(source[nowIndex] == '\n'){
				topMatch = true;
				lineStart = true;
				return;
			}
			
			//　<Table> セル区切り
			if(tableStart && source[nowIndex] == '|' && 
				(nowIndex == 0 || source[nowIndex-1] != '\\')){
				closeBlock('c');
				if(source.length>nowIndex+1 &&
						source[nowIndex+1] == '*'){
					nowIndex++;
					stepForword(1);
					openBlock('o');
				}else{
					openBlock('c');
				}

			// 日本語会話内、会話開始発見
			}else if(jtalkStart && jtalkNameStart &&
				source[nowIndex] == '：' && // 2byte 
				(nowIndex == 0 || source[nowIndex-1] != '\\')){
				jtalkNameStart = false;
				jtalkCommentStart = true;
				closeBlock('「');
				openBlock('」');
			// 定義ブロック定義内容発見
			}else if(defStart && defnameStart &&
				source[nowIndex] == ':' && 
				(nowIndex == 0 || source[nowIndex-1] != '\\')){
				defnameStart = false;
				defdescStart = true;
				closeBlock('t');
				openBlock('d');
			// 行の通常文字
			}else{
				switch(source[nowIndex]){
				case '\'':
					if(sourceStr.indexOf("'''", nowIndex) == nowIndex){
						if(bStarted){
							closeBlock('b');
							nowIndex += 2;
							stepForword(2);
							bStarted = false;
						}else{
							openBlock('b');
							nowIndex += 2;
							stepForword(2);
							bStarted = true;
						}
					}else{
						escapeWrite(source[nowIndex]);
					}
					break;
				case '{':
					if(sourceStr.indexOf("{{{:", nowIndex) == nowIndex){
						if(!box1Started){
							openBlock('i');
							nowIndex += 3;
							stepForword(3);
							box1Started = true;
							
							if(source.length>nowIndex+1 && source[nowIndex+1] == '\n'){
								nowIndex++;
								stepForword(1);
							}
						}else{
							escapeWrite(source[nowIndex]);
						}
					}else if(sourceStr.indexOf("{{{\n", nowIndex) == nowIndex){
						if(!box2Started){
							openBlock('j');
							nowIndex += 3;
							stepForword(3);
							box2Started = true;
							
							if(source.length>nowIndex+1 && source[nowIndex+1] == '\n'){
								nowIndex++;
								stepForword(1);
							}
						}else{
							escapeWrite(source[nowIndex]);
						}
					}else if(sourceStr.indexOf("{{{", nowIndex) == nowIndex){
						if(!emStarted){
							openBlock('e');
							nowIndex += 2;
							stepForword(2);
							emStarted = true;
						}else{
							escapeWrite(source[nowIndex]);
						}
					}else{
						escapeWrite(source[nowIndex]);
					}
					break;
				case '}':
					if(sourceStr.indexOf("}}}", nowIndex) == nowIndex){
						if(box1Started){
							closeBlock('i');
							nowIndex += 2;
							stepForword(2);
							box1Started = false;
							
							if(source.length>nowIndex+1 && source[nowIndex+1] == '\n'){
								nowIndex++;
								stepForword(1);
							}
						}else if(box2Started){
							closeBlock('j');
							nowIndex += 2;
							stepForword(2);
							box2Started = false;
							
							if(source.length>nowIndex+1 && source[nowIndex+1] == '\n'){
								nowIndex++;
								stepForword(1);
							}
						}else if(emStarted){
							closeBlock('e');
							nowIndex += 2;
							stepForword(2);
							emStarted = false;
						}else{
							escapeWrite(source[nowIndex]);
						}
					}else{
						escapeWrite(source[nowIndex]);
					}
					break;
				case '[':
					if(sourceStr.indexOf("[[", nowIndex) == nowIndex
							&& sourceStr.indexOf("]]",nowIndex) > 0){
						compileBlock("[[",
								sourceStr.substring(
										sourceStr.indexOf("[[", nowIndex)+2,
										sourceStr.indexOf("]]", nowIndex))
							);
						stepForword(sourceStr.indexOf("]]", nowIndex)+1-nowIndex);
						nowIndex = sourceStr.indexOf("]]", nowIndex)+1;
					}else if(sourceStr.indexOf("]",nowIndex) > 0){
						compileBlock("[",
								sourceStr.substring(
										sourceStr.indexOf("[", nowIndex)+1,
										sourceStr.indexOf("]", nowIndex))
							);
						stepForword(sourceStr.indexOf("]", nowIndex)-nowIndex);
						nowIndex = sourceStr.indexOf("]", nowIndex);
					}else{
						escapeWrite(source[nowIndex]);
					}
					break;
				case '=':
					Dispatcher d = new Dispatcher(new Block(){
						public void process(Object... o) {
							escapeWrite(source[((Integer)o[1])]);
						}});
					for(int i=1; i<=5; i++){
						final int hn = i;
						d.addCondition(new ConditionBlock(){
							public boolean condition(Object... o) {
								int now = ((Integer)o[1]);
								if(hStartNum == hn &&
									sourceStr.indexOf(StringTools.xStr('=',hn), now) == now){
									return true;
								}
								return false;
							}
							public void process(Object... o) {
								int now = ((Integer)o[1]);
								now += hn-1;
								stepForword(hn-1);
								if(source.length > now+1 && source[now+1] != '\n'){
									openBlock('h');
									hInsOpen = true;
								}
							}
						});
					}
					d.dispatch('=',nowIndex);
					break;
				default:
					escapeWrite(source[nowIndex]);
				}
			}
		}
		

		public void compileBlock(String type, String data){
			htmloutput.append(wikilib.compileBlock(type,data));
		}

		public void openBlock(char c){
			htmloutput.append(wikilib.openBlock(c));
		}
		
		public void dimBlock(char c){
			htmloutput.append(wikilib.dimBlock(c));
		}
		
		public void closeBlock(char c){
			htmloutput.append(wikilib.closeBlock(c));
		}
	}
}
