package org.qrone.parser;

import java.util.Map;

/**
 * Wiki Library for TaggedWiki markup Parser 'QrONEParser'.<br>
 * <br>
 * This interface is used for Definitions of All Wiki markup to HTML conversion.<br>
 * Class which implement this interface should be registered for QrONEParser
 * object. Implemented method will be called by QrONEParser to use for resolving
 * all wiki markups.
 * 
 * @author J.Tabuchi
 */
public interface WikiLib {
	/**
	 * Returns output HTML tags for opening specified block.<br>
	 * possible value of type is followings:<br>
	 * 
	 * <dl compact="compact" style="margin-left:20px;">
	 * <dt>p</dt><dd>&lt;p&gt; - Paragraf</dd>
	 * <dt>*</dt><dd>&lt;ul&gt;&lt;li&gt; - Unnumbered List</dd>
	 * <dt>#</dt><dd>&lt;ol&gt;&lt;li&gt; - Numbered List</dd>
	 * <dt>;</dt><dd>&lt;dl&gt; - Definition List</dd>
	 * <dt>t</dt><dd>&lt;dt&gt; - Definition List - Name</dd>
	 * <dt>d</dt><dd>&lt;dd&gt; - Definition List - Value</dd>
	 * <dt>：　// 2byte</dt><dd>&lt;dl&gt; - Japanese Talk</dd>
	 * <dt>「　// 2byte</dt><dd>&lt;dt&gt; - Japanese Talk - Name</dd>
	 * <dt>」　// 2byte</dt><dd>&lt;dd&gt; - Japanese Talk - Comment</dd>
	 * <dt>b</dt><dd>&lt;b&gt; - Bold</dd>
	 * <dt>i</dt><dd>&lt;div&gt; - Indent Block</dd>
	 * <dt>j</dt><dd>&lt;div&gt; - Indent Block 2</dd>
	 * <dt>e</dt><dd>&lt;em&gt; - Emphasized</dd>
	 * <dt>1</dt><dd>&lt;h1&gt; - Header 1/dd>
	 * <dt>2</dt><dd>&lt;h2&gt; - Header 2</dd>
	 * <dt>3</dt><dd>&lt;h3&gt; - Header 3</dd>
	 * <dt>4</dt><dd>&lt;h4&gt; - Header 4</dd>
	 * <dt>5</dt><dd>&lt;h5&gt; - Header 5</dd>
	 * <dt>h</dt><dd>&lt;span&gt; - Inner Quote Span of Header</dd>
	 * <dt>|</dt><dd>&lt;table&gt;&lt;tr&gt; - Table</dd>
	 * <dt>c</dt><dd>&lt;td&gt; - Table cell</dd>
	 * <dt>o</dt><dd>&lt;td class="head-cell"&gt; - Table header cell</dd>
	 * <dt>:</dt><dd>&lt;div style="margin-left:3em;"&gt; - Indent Block 3</dd>
	 * <dt>></dt><dd>&lt;div style="margin-left:3em;"&gt; - Indent Block 4</dd>
	 * </dl>
	 * 
	 * @see #closeBlock(char);
	 * @param type blocktype
	 * @return Output HTML Tags.
	 */
	public String openBlock(char type);
	
	/**
	 * Returns output HTML tags for dimilitor of specified block.<br>
	 * possible value of type is followings:<br>
	 * 
	 * <dl compact="compact" style="margin-left:20px;">
	 * <dt>p</dt><dd>&lt;/p&gt;&lt;p&gt; - Paragraf</dd>
	 * <dt>#</dt><dd>&lt;/li&gt;&lt;li&gt; - Unnumbered List</dd>
	 * <dt>*</dt><dd>&lt;/li&gt;&lt;li&gt; - Numbered List</dd>
	 * <dt>p</dt><dd>&lt;br/&gt; - Break</dd>
	 * <dt>|</dt><dd>&lt;/tr&gt;&lt;tr&gt; - Table Break</dd>
	 * <dt>r</dt><dd>&lt;hr/&gt; - Line</dd>
	 * <dt>s</dt><dd>&lt;hr/&gt; - Line</dd>
	 * <dt>t</dt><dd>&lt;hr/&gt; - Line</dd>
	 * <dt>u</dt><dd>&lt;hr/&gt; - Line</dd>
	 * </dl>
	 * 
	 * @see #openBlock(char)
	 * @see #closeBlock(char);
	 * @param type blocktype
	 * @return Output HTML Tags.
	 */
	public String dimBlock(char type);
	
	/**
	 * Returns output HTML tags for closing specified block.<br>
	 * possible value of type is same as <code>openBlock(char)</code><br>
	 * </dl>
	 * 
	 * @see #openBlock(char);
	 * @param type blocktype
	 * @return Output HTML Tags.
	 */
	public String closeBlock(char type);
	
	/**
	 * Returns output HTML tags for replacing specified block.<br>
	 * possible value of type is "[" or "[["</code><br>
	 * 
	 * @param type "[" or "[["
	 * @param data CDATA section of this block.
	 */
	public String compileBlock(String type, String data);

	/**
	 * Returns Map<String, TagLib> for installing tags.
	 * 
	 * @return Tags to install.
	 */
	public Map installTags();
}
