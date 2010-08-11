package org.qrone.main;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.qrone.Base64;
import org.qrone.StringTools;
import org.qrone.ZipTools;

public class QrInflater {
	public static void main(String[] args){
		try {
			String str = StringTools.readStream(System.in);
			Pattern p = Pattern.compile(
					".*<!-- QrONEParserSource.*?Base64:(.*?) -->.*",Pattern.DOTALL);
			Matcher m = p.matcher(str);
			m.matches();
			String code = m.group(1);
			System.out.write(ZipTools.decode(Base64.decode(code.getBytes())));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
