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
public class DictTranscriber {

    public static void main(String[] args) throws Exception {
        STParser parser;
        FileWriter output = new FileWriter("revdict.html");
        {
            FileReader dictReader = new FileReader("lessvocab.html");
            String dict = "";

            //read in the dict file
            char[] cbuf = new char[200];
            for (int n = dictReader.read(cbuf); n != -1;
                    n = dictReader.read(cbuf)) {
                dict += String.copyValueOf(cbuf, 0, n);
            }

            parser = new STParser(dict);
        }

        String line;
        boolean lastWasForeignphrase = false;
        while ((line = parser.nextLine()) != null) {
            if(lastWasForeignphrase){
                line = line.substring(1);
                output.write(">");
                
                STParser p = new STParser(line);
                String latinRep = p.nextUntilToken('<');
                
                HTMLExporter h = new HTMLExporter(140);
                LTParser ltp = new LTParser(latinRep);
                ltp.setHTMLCompatible(true);
                String tengwarRep = h.tPart(ltp.parse());
                output.write(tengwarRep);
                output.write(" ["+latinRep+"]");
                
                output.write("<");
                
                output.write(p.getRemaining()+"\n");
                lastWasForeignphrase = false;
                continue;
            }
            
            
            if(line.contains("CLASS=\"foreignphrase\""))
                lastWasForeignphrase = true;
            
            output.write(line+"\n");
        }
    }
}

