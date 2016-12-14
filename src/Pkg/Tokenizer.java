package Pkg;

/**
 * Created by gimy on 11/18/2016.
 */



import jdk.nashorn.internal.runtime.ParserException;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer
{
    public class TokenInfo //this class will help store regex to match sql syntax
    {


        private final Pattern regex;
        private final int tokenCode;

        public TokenInfo(Pattern regex, int tokenCode)
        {
            super();
            this.regex = regex;
            this.tokenCode = tokenCode;
        }

        public Pattern getRegex() {
            return regex;
        }

        public int getTokenCode() {
            return tokenCode;
        }
    }

    public static class Token { //this class will help store token sequence that where matched in the input and will help identifying their code

        public static final int
                EOF = 0,AND=1,ASC=2,CREATE=3,DELETE=4,DESC=5,DISTINCT=6,FALSE=7,FROM=8,INTO=9,IS=10,NOT=11
                ,NULL=12,OR=13,ORDER=14,RIGHT=15,SELECT=16,SET=17,TABLE=18,TRUE=19,UPDATE=20,WHERE=21
                ,BY=22,CHARACTER=23,DEC=24,DROP=25,FIRST=26,INSERT=27,LAST=28,UNKNOWN=29,VALUES=30
                ,BOOLEAN=31,BOOL=32,INT=33,INTEGER=34,REAL=35,FLOAT=36,DOUBLE=37,NUMERIC=38,DECIMAL=39,CHAR=40
                ,VARCHAR=41,TEXT=42,ASSIGN=43,EQUAL=44,COLON=45,SEMI_COLON=46,COMMA=47,NOT_EQUAL=48,LTH=49
                ,LEQ=50,GTH=51,GEQ=52,LEFT_PAREN=53,RIGHT_PAREN=54,PLUS=55,MINUS=56,MULTIPLY=57,DIVIDE=58,MODULAR=59
                ,DOT=60,UNDERLINE=61,VERTICAL_BAR=62,QUOTE=63,NUMBER=64,REAL_NUMBER=65
                ,Identifier=66,Character_String_Literal=67;

        public final int tokenCode;
        public final String sequence;

        public Token(int tokenCode, String sequence)
        {
            super();
            this.tokenCode = tokenCode;
            this.sequence = sequence;
        }

    }



    private LinkedList<TokenInfo> tokenInfos;
    private LinkedList<Token> tokens;

    public Tokenizer()  //constructor
    {
        tokenInfos = new LinkedList<TokenInfo>();
        tokens = new LinkedList<Token>();
    }



    public void add(String regex, int tokenCode)//this will add tokens info to a linked list
    {
        tokenInfos.add(new TokenInfo(Pattern.compile("^("+regex+")", Pattern.CASE_INSENSITIVE), tokenCode)); //notice that the regex is compiled for performance and we match only the beginning of the input
    }



    public void tokenize(String str) //this will break the input into small tokens and add them to a linked list
    {
        String input = str.trim();
        tokens.clear();
        while (!input.equals(""))
        {
            boolean match = false;
            for (int i = 0; i < tokenInfos.size(); i++) {

                Matcher matcher = tokenInfos.get(i).regex.matcher(input);
                if (matcher.find())
                {
                    match = true;
                    String sequence = matcher.group().trim();
                    input = matcher.replaceFirst("").trim();
                    tokens.add(new Token(tokenInfos.get(i).tokenCode, sequence));
                    break;
                }
            }

            if (!match) throw new ParserException("Parsing unresolved : "+ input);
        }
    }



    public LinkedList<Token> getTokens()
    {
        return tokens;
    }

    public LinkedList<TokenInfo> getTokenInfos() {
        return tokenInfos;
    }

}