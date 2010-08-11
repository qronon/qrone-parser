package org.qrone.main;

import org.qrone.Base64;
import org.qrone.StringTools;
import org.qrone.ZipTools;
import org.qrone.parser.QrONEParser;

public class QrTaggedWikiParser {
	public static void main(String[] args){
		try {
			String str = StringTools.readStream(System.in);
			str = StringTools.xCRLFtoLF(str);
			
			QrONEParser p = new QrONEParser(str);			
			p.parse();
			System.out.write(p.output().getBytes("UTF-8"));
			System.out.write("\n\n<!-- QrONEParserSource // ZIP compressed data of the source text.\nBase64:".getBytes("UTF-8"));
			System.out.write(
					Base64.encode(ZipTools.encode(
							str.getBytes("UTF-8"))).getBytes("UTF-8"));
			System.out.write(" -->\n".getBytes("UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
