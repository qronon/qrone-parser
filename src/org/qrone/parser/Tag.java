package org.qrone.parser;

import java.util.Map;

public interface Tag {
	public String getNodeName();
	
	public boolean hasAttribute(String attr);
	public String getAttribute(String attr);
	public Map getAttributes();
}
