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
import java.util.LinkedList;
import java.util.List;
import romUtils.STParser;

/**
 *
 * @author namor
 */
public class HTMLExporter {

    public static class AlignedText {

        public AlignedText(boolean isTengwar, String text, String scale) {
            this.isTengwar = isTengwar;
            this.text = text;
            this.scale = scale;
        }
        /*public AlignedText(boolean isTengwar, String text, int scale) {
        AlignedText(isTengwar,text,Integer.toString(scale));
        }*/
        public boolean isTengwar;
        public String text;
        public String scale;
    }

    public static void main(String[] args) throws Exception {
        HTMLExporter h = new HTMLExporter();

        FileReader dictReader = new FileReader("dict");
        String dict = "";

        //read in the dict file
        char[] cbuf = new char[200];
        for (int n = dictReader.read(cbuf); n != -1;
                n = dictReader.read(cbuf)) {
            dict += String.copyValueOf(cbuf, 0, n);
        }

        STParser parser = new STParser(dict);

        //parse line by line
        String line = parser.nextLine().trim();
        while (line != null) {
            //transcribe first word
            STParser lineParser = new STParser(line);
            String firstWord = lineParser.nextWord();
            String tengwarFirstWord = new LTParser(firstWord.trim()).parse();
            h.parts.add(new AlignedText(true, tengwarFirstWord, "120"));
            h.parts.add(new AlignedText(false,"  " + lineParser.getRemaining() + "<br><br>", "120"));

            line = parser.nextLine();
            if (line != null) {
                line = line.trim();
            }
        }

        /*
        h.parts.add(new AlignedText(false, "kiris: ", "400"));
        h.parts.add(new AlignedText(true , "zT6T+", "400"));
        h.parts.add(new AlignedText(false, "\nroman: ", "400"));
        h.parts.add(new AlignedText(true , "6Yt#5", "400"));*/
        h.exportComposition("testfile");
    }
    
    public HTMLExporter() throws Exception {
        scale = "120";//JOptionPane.showInputDialog("Enter scale of output:", 1000);
    }
    public HTMLExporter(int scale){
        this.scale = Integer.toString(scale);
    }
    

    public String getHTMLComposition() {
        String html = "";

        html += html_head;
        for (AlignedText at : parts) {
            if (at.isTengwar) {
                html += tPart(at.text, at.scale);
            } else {
                html += nPart(at.text, at.scale);
            }
        }
        html += html_end;
        return html;
    }

    public void exportComposition(String filename) throws Exception {
        FileWriter fw = new FileWriter(filename);
        fw.write(getHTMLComposition());
        fw.close();
    }

    public String getHTMLTengwar(String tengwar_text) {
        return html_head + tPart(tengwar_text) + html_end;
    }

    public void exportTengwar(String tengwar_text, String filename) throws Exception {
        FileWriter fw = new FileWriter(filename);
        fw.write(getHTMLTengwar(tengwar_text));
        fw.close();
    }
    public List<AlignedText> parts = new LinkedList<AlignedText>();

    public String tPart(String tengwar_part) {
        return tPart(tengwar_part, scale);
    }

    public String tPart(String tengwar_part, String scale) {
        return tPartBPreScale + scale + xPartMPostScale + tengwar_part + xPartE;
    }

    private String nPart(String normal_part) {
        return nPart(normal_part, scale);
    }

    private String nPart(String normal_part, String scale) {
        return nPartBPreScale + scale + xPartMPostScale + normal_part + xPartE;
    }
    private final String scale;
    private final String html_head = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\">\n" +
            "<html><head><title>Tengwar-Lojban</title></head>\n" +
            "<body>\n\n";
    private final String html_end = "</body></html>";
    private final String tPartBPreScale = "<span style=\"font-family:'Tengwar Sindarin'; font-size:";
    private final String xPartMPostScale = "%\">";
    private final String xPartE = "</span>\n";
    private final String nPartBPreScale = "<span style=\"font-family:'Monospace'; font-size:";
}
