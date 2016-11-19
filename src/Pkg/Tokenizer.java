package Pkg;

/**
 * Created by gimy on 11/18/2016.
 */



import jdk.nashorn.internal.runtime.ParserException;

import java.util.LinkedList;
import java.util.regex.Matcher;//to check
import java.util.regex.Pattern;//to check


public class Tokenizer {

    private LinkedList<TokenInfo> tokenInfos; //hold the information about all the tokens
    private LinkedList<Token> tokens; //to store the code of the token with it's real string value

    public Tokenizer() {
        tokenInfos = new LinkedList<TokenInfo>();
        tokens = new LinkedList<Token>();
    }

    public class TokenInfo{

        public final Pattern regex; //stores the regular expression in compiled form (improve performance)
        public final int token;// code of the token each token has his own code value

        public TokenInfo(Pattern regex, int token) {
            super();
            this.regex = regex;
            this.token = token;
        }
    }

    public class Token { //this class a container that will contain the code of a token and it's real string value

        public final int token;
        public final String sequence;

        public Token(int token, String sequence) {

            super();
            this.token = token;
            this.sequence = sequence;
        }
    }
    public void add(String regex, int token) {//we can pass a regular expression string and a token code to the method.
        tokenInfos.add( new TokenInfo( Pattern.compile("^(" + regex + ")"), token)); //adding a token with it's compiled regex and it's token code
    }

    public void tokenize(String str) {

        String input = new String(str);
        tokens.clear();

        while (!input.equals("")) {

            boolean match = false;

            for (TokenInfo element : tokenInfos) {

                Matcher matcher = element.regex.matcher(input);

                if (matcher.find()) {
                    match = true;

                    String tok = matcher.group().trim();
                    tokens.add(new Token(element.token, tok));

                    input = matcher.replaceFirst("");
                    break;
                }

                if (!match) throw new ParserException("Unexpected character in input: " + input );


            }
        }
    }

    public LinkedList<Token> getTokens() {
        return tokens;
    }

}
