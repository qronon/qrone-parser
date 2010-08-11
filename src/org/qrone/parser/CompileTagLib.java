package org.qrone.parser;

public interface CompileTagLib extends TagLib{
	/**
	 * Returns output HTML tags for replacement of tag conent.
	 * 
	 * @param t Tag object.
	 * @param content CDATA of inside of the tag.
	 * @return Ouput HTML contents.
	 */
	public String bodyTag(Tag t, String content);
}
