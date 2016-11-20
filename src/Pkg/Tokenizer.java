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
    private class TokenInfo
    {
        public final Pattern regex;
        public final int token;

        public TokenInfo(Pattern regex, int token)
        {
            super();
            this.regex = regex;
            this.token = token;
        }
    }

    public class Token
    {
        public final int token;
        public final String sequence;

        public Token(int token, String sequence)
        {
            super();
            this.token = token;
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

    public void add(String regex, int token)
    {
        tokenInfos.add(new TokenInfo(Pattern.compile("^("+regex+")"), token));
    }

    public void tokenize(String str)
    {
        String input = str.trim();
        tokens.clear();
        while (!input.equals(""))
        {
            boolean match = false;
            for (TokenInfo info : tokenInfos)
            {
                Matcher matcher = info.regex.matcher(input);
                if (matcher.find())
                {
                    match = true;
                    String tok = matcher.group().trim();
                    input = matcher.replaceFirst("").trim();
                    tokens.add(new Token(info.token, tok));
                    break;
                }
            }
            if (!match) throw new ParserException("Unexpected character in input: "+ input);
        }
    }

    public LinkedList<Token> getTokens()
    {
        return tokens;
    }

}