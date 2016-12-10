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
                EOF = 0,AND=1, ASC=2, CREATE=3, DELETE=4, DESC=5, DISTINCT=6, FALSE=7, FROM=8,
                INTO=9, IS=10, LEFT=11, NOT=12, NULL=13, OR=14, ORDER=15, RIGHT=16, SELECT=17,
                SET=18, TABLE=19, TRUE=20, UPDATE=21, WHERE=22, AVG=23, BY=24, CHARACTER=25,
                COUNT=26, DEC=27, DROP=28, FIRST=29, INSERT=30, LAST=31, MAX=32, MIN=33,
                SUM=34, UNKNOWN=35, VALUES=36, BOOLEAN=37, BOOL=38, INT=39, INTEGER=40,
                REAL=41, FLOAT=42, DOUBLE=43, NUMERIC=44, DECIMAL=45, CHAR=46, VARCHAR=47,
                DATE=48, TIME=49, TIMESTAMP=50, TEXT=51, ASSIGN=52, EQUAL=53, COLON=54,
                SEMI_COLON=55, COMMA=56, NOT_EQUAL=58, LTH=59,
                LEQ=60, GTH=61, GEQ=62, LEFT_PAREN=63, RIGHT_PAREN=64, PLUS=65, MINUS=66,
                MULTIPLY=67, DIVIDE=68, MODULAR=69, DOT=70, UNDERLINE=71, VERTICAL_BAR=72,
                QUOTE=73, NUMBER=74, REAL_NUMBER=75, Identifier=76, Character_String_Literal=77;

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

    public Tokenizer()
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