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

import java.awt.Font;
import java.io.FileInputStream;

/**
 *
 * @author namor
 */
public class LTParser {

    /**
     * lojban text to parse
     */
    private String lttp;
    private Token prevToken;
    /**
     * token before the previous token
     */
    private Token prev2Token;
    private Token currToken;
    /**
     * if set, high ascii signs are replaced by html codes
     */
    private boolean htmlCompatible = gHtmlCompatible;
    /**
     * optional ending s-curls for names
     */
    private boolean enableSCurls = true;

    /**
     * use with caution!
     */
    public static void setGlobalHTMLCompatible(boolean htmlComp){
        gHtmlCompatible = htmlComp;
    }
    
    public static boolean gHtmlCompatible = false;
    
    /**
     * see enableSCurls:boolean
     * @param enable_s_curls
     */
    public void setSCurls(boolean enable_s_curls) {
        enableSCurls = enable_s_curls;
    //System.out.println("changing s-curl state to: " + enable_s_curls);
    }

    public static Font getFontUsed() throws Exception{
        return Font.createFont(
                Font.TRUETYPE_FONT, new FileInputStream("TengwarSindarin.ttf"));
    }
    
    /**
     * see htmlCompatible:boolean
     * @param htmlCompatible
     */
    public void setHTMLCompatible(boolean htmlCompatible) {
        this.htmlCompatible = htmlCompatible;
    }
    
    /**
     * a recursive function parsing lojban to tengwar,
     * you should use parse() from the outside
     * @param txSoFar the result so far
     * @return tengwar representation of the lojban input
     * @throws java.lang.Exception
     */
    String parse(String txSoFar) throws Exception {
        Token tk = getNextToken();
        if (tk == null) {
            return txSoFar;
        }

        if (tk.type == Token.TYPE.CONSONANT) {
            Token p2tk = read2PrevToken();
            Token ptk = readPrevToken();
            Token ntk = readNextToken();
            //end 's' curl?
            //System.out.println(enableSCurls);
            if (enableSCurls && tk.getChar() == 's' && ptk != null && p2tk != null &&
                    ptk.type == Token.TYPE.VOWEL &&
                    (ntk == null || ntk.getChar() == '.' || ntk.getChar() == ' ' || ntk.getChar() == '\n')) {
                //System.out.println("Curling mode!");
                switch (p2tk.getChar()) {
                    case 'l':
                        if (!htmlCompatible) {
                            txSoFar += '¥';
                        } else {
                            txSoFar += "&#0165;";
                        }
                        break;
                    case 'k':
                    case 'g':
                    case 'x':
                        if (!htmlCompatible) {
                            txSoFar += '¢';
                        } else {
                            txSoFar += "&#0162;";
                        }
                        break;

                    default:
                        txSoFar += '+';
                    //txSoFar += tengwarConsonant(tk.getChar());
                }
            } else //or not
            {
                txSoFar += tengwarConsonant(tk.getChar());
            }
        } else if (tk.type == Token.TYPE.VOWEL) {
            Token ptk = readPrevToken();
            //vowel above consonant
            if (ptk != null && ptk.type == Token.TYPE.CONSONANT) {
                if (tk.getChar() != 'y') {
                    txSoFar += tengwarVowel(tk.getChar(), ptk.tPosAbove);
                } else {
                    txSoFar += tengwarVowel('y', ptk.tPosBelow);
                }
            } else {
                //double vowel
                if (readNextToken() != null && readNextToken().getChar() == tk.getChar()) {
                    getNextToken();
                    txSoFar += "~" + tengwarVowel(tk.getChar(), Token.TPOS.RIGHT);

                } //single vowel
                else {
                    txSoFar += "`" + tengwarVowel(tk.getChar(), Token.TPOS.RIGHT);
                }
            }
        } else {
            txSoFar += tengwarMisc(tk.getChar());
        }

        return parse(txSoFar);
    }

    public String parse() throws Exception {
        return parse("");
    }

    char tengwarConsonant(char lojban_rep) throws Exception {
        switch (lojban_rep) {
            //first row
            case 't':
                return '1';
            case 'p':
                return 'q';
            case 'k':
                return 'z';

            //second row
            case 'd':
                return '2';
            case 'b':
                return 'w';
            case 'g':
                return 'x';

            //third row
            case 'f':
                return 'e';
            case 'c':
                return 'd';
            case 'x':
                return 'c';

            //fourth row
            case 'v':
                return 'r';
            case 'j':
                return 'f';

            //fifth row
            case 'n':
                return '5';
            case 'm':
                return 't';

            //sixth row
            case 'r':
                return '6';

            //additional twngwar
            case 'l':
                return 'j';
            case 's':
                return '8';
            case 'z':
                return 'k';
        }
        throw new Exception("invalid control flow pos");
    }

    String tengwarMisc(char lojban_rep) throws Exception {
        switch (lojban_rep) {
            case ' ': //space remains space
                return " ";
            case '\'': // a'e for instance
                if (htmlCompatible) {
                    return "&#0156;";
                } else {
                    return "œ";
                }
            case '.':
                if (htmlCompatible) {
                    return "&#0186;";
                } else {
                    return "º";
                }
            case '\n':
                return "\n";
            case ',':
                if (!htmlCompatible) {
                    return "¹";
                } else {
                    return "&#0185;";
                }
            default:
                return " ";
        }
    }

    String tengwarVowel(char lojban_rep, Token.TPOS pos_mod) throws Exception {
        //System.out.println("Vowel, got: " + lojban_rep + "  POS: " + pos_mod);

        if (lojban_rep == 'a') {
            if (pos_mod == Token.TPOS.LEFT) {
                return "#";
            }
            if (pos_mod == Token.TPOS.MID1) {
                return "E";
            }
            if (pos_mod == Token.TPOS.MID2) {
                return "D";
            }
            if (pos_mod == Token.TPOS.RIGHT) {
                return "C";
            }
        }

        if (lojban_rep == 'e') {
            if (pos_mod == Token.TPOS.LEFT) {
                return "$";
            }
            if (pos_mod == Token.TPOS.MID1) {
                return "R";
            }
            if (pos_mod == Token.TPOS.MID2) {
                return "F";
            }
            if (pos_mod == Token.TPOS.RIGHT) {
                return "V";
            }
        }

        if (lojban_rep == 'i') {
            if (pos_mod == Token.TPOS.LEFT) {
                return "%";
            }
            if (pos_mod == Token.TPOS.MID1) {
                return "T";
            }
            if (pos_mod == Token.TPOS.MID2) {
                return "G";
            }
            if (pos_mod == Token.TPOS.RIGHT) {
                return "B";
            }
        }

        if (lojban_rep == 'o') {
            if (pos_mod == Token.TPOS.LEFT) {
                return "^";
            }
            if (pos_mod == Token.TPOS.MID1) {
                return "Y";
            }
            if (pos_mod == Token.TPOS.MID2) {
                return "H";
            }
            if (pos_mod == Token.TPOS.RIGHT) {
                return "N";
            }
        }

        if (lojban_rep == 'u') {
            if (pos_mod == Token.TPOS.LEFT) {
                return "&";
            }
            if (pos_mod == Token.TPOS.MID1) {
                return "U";
            }
            if (pos_mod == Token.TPOS.MID2) {
                return "J";
            }
            if (pos_mod == Token.TPOS.RIGHT) {
                return "M";
            }
        }

        if (lojban_rep == 'y') {

            if (pos_mod == Token.TPOS.LEFT) {
                //return "Ë";
                return "(";
            }
            if (pos_mod == Token.TPOS.MID1) {
                //return "É";
                return "O";
            }
            if (pos_mod == Token.TPOS.MID2) {
                //return "Ê";
                return "L";
            }
            if (pos_mod == Token.TPOS.RIGHT) {
                if (!htmlCompatible) {
                    return "Ë";
                } else {
                    return "&#0203;";
                }
            }
        }
        throw new Exception(
                "invalid control flow position position");
    }
    
    public LTParser(String lojbanTextToParse) {
        lttp = lojbanTextToParse;
    }

    Token getNextToken() {
        Token tk = readNextToken();
        prev2Token =
                prevToken;
        prevToken =
                currToken;
        currToken =
                tk;
        if (!lttp.isEmpty()) {
            lttp = lttp.substring(1);
        } //remove first character

        return tk;
    }

    Token readCurrToken() {
        return currToken;
    }

    Token readNextToken() {
        if (lttp.length() != 0) {
            Token tk = new Token(lttp.charAt(0));
            return tk;
        } else {
            return null;
        }

    }

    Token readPrevToken() {
        return prevToken;
    }

    Token read2PrevToken() {
        return prev2Token;
    }
    }

class Token {

    public enum TYPE {

        CONSONANT, VOWEL, MISC
    }

    public enum TPOS {

        LEFT, MID1, MID2 /*=> mid-high when below!*/, RIGHT
    }
    public final TPOS tPosAbove;
    public final TPOS tPosBelow;
    public final TYPE type;

    public char getChar() {
        return c;
    }
    char c;

    public Token(char c) {
        this.c = c;

        //consonant/vowel/misc
        switch (c) {
            case 'a':
            case 'e':
            case 'i':
            case 'o':
            case 'u':
            case 'y':
                //System.out.println("tk: vowel");
                type = TYPE.VOWEL;
                break;
            case 't':
            case 'p':
            case 'k':
            case 'd':
            case 'b':
            case 'g':
            case 'f':
            case 'c':
            case 'x':
            case 'v':
            case 'j':
            case 'n':
            case 'm':
            case 'r':
            case 'l':
            case 's':
            case 'z':
                //System.out.println("tk: consonant");
                type = TYPE.CONSONANT;
                break;
            default:
                //System.out.println("tk: misc");
                type = TYPE.MISC;
        }

        //VowelPositionOverConsonant
        switch (c) {
            case 'd':
            case 'b':
            case 'g':
            case 'v':
            case 'j':
            case 'n':
            case 'm':
            case 's':
            case 'z':
            // the following two could use 'mid1' too, but are more easily
            // recognizable with 'left' imo
            case 'c':
            case 'x':
                tPosAbove = TPOS.LEFT;
                break;
            case 'f':
                tPosAbove = TPOS.MID2;
                break;

            default:
                tPosAbove = TPOS.MID1;
        }

        //VowelPositionUnderConsonant
        switch (c) {
            case 'd':
            case 'b':
            case 'f':
            case 's':
            case 'z':
            case 'c':
            case 'x':
                tPosBelow = TPOS.MID1;
                break;
            case 'l':
                tPosBelow = Token.TPOS.MID2;
                break;
            default:
                tPosBelow = this.tPosAbove;
        }

    }
}
