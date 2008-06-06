/*
 * 
 * Copyright (C) 2008 Roman Naumann
 * 
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * See COPYING.TXT for details.
 */
package lojbantengwartranslator;

import java.io.FileReader;
import java.io.FileWriter;
import romUtils.STParser;

/**
 *
 * @author namor
 */
public class TextTranscriber {

    public static void main(String[] args) throws Exception {
        String lojban = "";
        STParser stp;
        FileWriter output;
        {
            output = new FileWriter("short_story.html");
            FileReader dictReader = new FileReader("short_story");
            String dict = "";

            //read in the dict file
            char[] cbuf = new char[200];
            for (int n = dictReader.read(cbuf); n != -1;
                    n = dictReader.read(cbuf)) {
                dict += String.copyValueOf(cbuf, 0, n);
            }
            stp = new STParser(dict);
        }

        String line;
        while ((line = stp.nextLine()) != null) {
            LTParser ltp = new LTParser(line);
            ltp.setHTMLCompatible(true);
            lojban += ltp.parse() + "<br>\n";
        }
        String html = new HTMLExporter(130).getHTMLTengwar(lojban);

        output.write(html);
        output.close();
    }
}
