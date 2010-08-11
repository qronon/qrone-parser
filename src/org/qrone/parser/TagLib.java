package org.qrone.parser;

/**
 * Tag Library for TaggedWiki markup Parser 'QrONEParser'.<br>
 * <br>
 * Implemented class of this interface is used for Definition 
 * single Tag markup to HTML Convertion.<br>
 * A instance used for resolving all Tags which has specified NodeName.
 * If you need all CDATA contents in specified tag, 
 * use <code>CompilegTagLib</code>.
 * 
 * @author J.Tabuchi
 */
public interface TagLib {
	/**
	 * Returns output HTML tags for replacement of opening tag.
	 * 
	 * @param t Tag object.
	 * @param started If true, This tag is placed for start of a line.
	 * @return Ouput HTML contents.
	 */
	public String startTag(Tag t, boolean started);
	
	/**
	 * Returns output HTML tags for replacement of closing tag.
	 * 
	 * @param t Tag object.
	 * @return Ouput HTML contents.
	 */
	public String endTag(Tag t);
	
	
	/**
	 * If true, this tag is a block element. Else, inline elment.<br>
	 * <br>
	 * Normally this method should return "false", but if you want to ignore
	 * break code which is placed just after and just before the tag, make it
	 * return "true".<br>
	 * If tag is set as a block element, parser will ignore upto 1 break codes
	 * just before the tag and upto 2 break codes just after the tag.
	 * 
	 * @return
	 */
	public boolean isBlock();
}
