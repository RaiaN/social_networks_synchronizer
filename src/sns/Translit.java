package sns;

import java.util.HashMap;
import java.util.Map;

public class Translit {
    private static final Map<String, String> letters = new HashMap<String, String>();
    static {
        letters.put("А", "A");
        letters.put("Б", "B");
        letters.put("В", "V");
        letters.put("Г", "G");
        letters.put("Д", "D");
        letters.put("Е", "E");
        letters.put("Ё", "E");
        letters.put("Ж", "ZH");
        letters.put("З", "Z");
        letters.put("И", "I");
        letters.put("Й", "I");
        letters.put("К", "K");
        letters.put("Л", "L");
        letters.put("М", "M");
        letters.put("Н", "N");
        letters.put("О", "O");
        letters.put("П", "P");
        letters.put("Р", "R");
        letters.put("С", "S");
        letters.put("Т", "T");
        letters.put("У", "U");
        letters.put("Ф", "F");
        letters.put("Х", "H");
        letters.put("Ц", "C");
        letters.put("Ч", "CH");
        letters.put("Ш", "SH");
        letters.put("Щ", "SH");
        letters.put("Ъ", "'");
        letters.put("Ы", "Y");
        letters.put("Ъ", "'");
        letters.put("Э", "E");
        letters.put("Ю", "U");
        letters.put("Я", "YA");
        letters.put("а", "a");
        letters.put("б", "b");
        letters.put("в", "v");
        letters.put("г", "g");
        letters.put("д", "d");
        letters.put("е", "e");
        letters.put("ё", "e");
        letters.put("ж", "zh");
        letters.put("з", "z");
        letters.put("и", "i");
        letters.put("й", "i");
        letters.put("к", "k");
        letters.put("л", "l");
        letters.put("м", "m");
        letters.put("н", "n");
        letters.put("о", "o");
        letters.put("п", "p");
        letters.put("р", "r");
        letters.put("с", "s");
        letters.put("т", "t");
        letters.put("у", "u");
        letters.put("ф", "f");
        letters.put("х", "h");
        letters.put("ц", "c");
        letters.put("ч", "ch");
        letters.put("ш", "sh");
        letters.put("щ", "sh");
        letters.put("ъ", "'");
        letters.put("ы", "y");
        letters.put("ъ", "'");
        letters.put("э", "e");
        letters.put("ю", "u");
        letters.put("я", "ya");
        letters.put("кс", "x");

        letters.put("A"  ,"А");
        letters.put("B"  ,"Б");
        letters.put("V"  ,"В");
        letters.put("G"  ,"Г");
        letters.put("D"  ,"Д");
        letters.put("E"  ,"Е");
        letters.put("E"  ,"Ё");
        letters.put("ZH" ,"Ж");
        letters.put("Z"  ,"З");
        letters.put("I"  ,"И");
        letters.put("I"  ,"Й");
        letters.put("K"  ,"К");
        letters.put("L"  ,"Л");
        letters.put("M"  ,"М");
        letters.put("N"  ,"Н");
        letters.put("O"  ,"О");
        letters.put("P"  ,"П");
        letters.put("R"  ,"Р");
        letters.put("S"  ,"С");
        letters.put("T"  ,"Т");
        letters.put("U"  ,"У");
        letters.put("F"  ,"Ф");
        letters.put("H"  ,"Х");
        letters.put("C"  ,"Ц");
        letters.put("CH" ,"Ч");
        letters.put("SH" ,"Ш");
        letters.put("SH" ,"Щ");
        letters.put("'"  ,"Ъ");
        letters.put("Y"  ,"Ы");
        letters.put("'"  ,"Ъ");
        letters.put("E"  ,"Э");
        letters.put("U"  ,"Ю");
        letters.put("YA" ,"Я");
        letters.put("a"  ,"а");
        letters.put("b"  ,"б");
        letters.put("v"  ,"в");
        letters.put("g"  ,"г");
        letters.put("d"  ,"д");
        letters.put("e"  ,"е");
        letters.put("e"  ,"ё");
        letters.put("zh" ,"ж");
        letters.put("z"  ,"з");
        letters.put("i"  ,"и");
        letters.put("k"  ,"к");
        letters.put("l"  ,"л");
        letters.put("m"  ,"м");
        letters.put("n"  ,"н");
        letters.put("o"  ,"о");
        letters.put("p"  ,"п");
        letters.put("r"  ,"р");
        letters.put("s"  ,"с");
        letters.put("t"  ,"т");
        letters.put("u"  ,"у");
        letters.put("f"  ,"ф");
        letters.put("h"  ,"х");
        letters.put("c"  ,"ц");
        letters.put("ch" ,"ч");
        letters.put("sh" ,"ш");
        letters.put("sh" ,"щ");
        letters.put("'"  ,"ъ");
        letters.put("y"  ,"ы");
        letters.put("'"  ,"ъ");
        letters.put("e"  ,"э");
        letters.put("u"  ,"ю");
        letters.put("x"  ,"кс");
        letters.put("ya" ,"я");
    }



    public static String toTranslit(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            String l = text.substring(i, i+1);
            if (letters.containsKey(l)) {
                sb.append(letters.get(l));
            }
            else {
                sb.append(l);
            }
        }
        return sb.toString();
    }

}
