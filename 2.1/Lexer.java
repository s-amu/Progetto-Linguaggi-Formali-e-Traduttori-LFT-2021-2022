import java.io.*; 
import java.util.*;

public class Lexer {
    public static int line = 1;
    private char peek = ' ';
    
    private void readch(BufferedReader br) {
        try {
            peek = (char) br.read();
        } catch (IOException exc) {
            peek = (char) -1; // ERROR
        }
    }

    public Token lexical_scan(BufferedReader br) {
        while (peek == ' ' || peek == '\t' || peek == '\n'  || peek == '\r') {
            if (peek == '\n') line++;
            readch(br);
        }
        
        switch (peek) {
			// gestione di ! ( ) { } + - * / ; , (Token)
            case '!':
                peek = ' ';
                return Token.not;
			case '(':
                peek = ' ';
                return Token.lpt;
			case ')':
                peek = ' ';
                return Token.rpt;
			case '{':
                peek = ' ';
                return Token.lpg;
			case '}':
                peek = ' ';
                return Token.rpg;
			case '+':
                peek = ' ';
                return Token.plus;
			case '-':
                peek = ' ';
                return Token.minus;
			case '*':
                peek = ' ';
                return Token.mult;
			case '/':
                peek = ' ';
                return Token.div;
			case ';':
                peek = ' ';
                return Token.semicolon;
			case ',':
                peek = ' ';
                return Token.comma;

			// gestione di operatori relazionali e logici && || < > <= >= <> == (Word)
            case '&': // leggo &
                readch(br);
                if (peek == '&') { // leggo un'altra & e restituisco And
                    peek = ' ';
                    return Word.and;
                } else {
                    System.err.println("Erroneous character"
                            + " after & : "  + peek );
                    return null;
                }
			case '|': // leggo |
                readch(br);
                if (peek == '|') { // leggo un'altra | e restituisco Or
                    peek = ' ';
                    return Word.or;
                } else {
                    System.err.println("Erroneous character"
                            + " after | : "  + peek );
                    return null;
                }
			case '<': // leggo <
                readch(br);
                if (peek == '=') { // leggo un = e restituisco <=
                    peek = ' ';
                    return Word.le;
                } else if (peek == '>'){ // leggo > e restituisco <>
					peek = ' ';
                    return Word.ne;
				} else if (peek == ' ' || Character.isLetter(peek) || Character.isDigit(peek)) { 
					peek = ' '; // leggo uno spazio, una lettera o un numero e restituisco <
                    return Word.lt;
				} else {
                    System.err.println("Erroneous character"
                            + " after < : "  + peek );
                    return null;
                }
			case '>': // leggo >
                readch(br);
                if (peek == '=') { // leggo un = e restituisco >=
                    peek = ' ';
                    return Word.ge;
                } else if (peek == ' ' || Character.isLetter(peek) || Character.isDigit(peek)){
					peek = ' '; // leggo uno spazio, una lettera o un numero e restituisco >
                    return Word.gt;
				} else {
                    System.err.println("Erroneous character"
                            + " after > : "  + peek );
                    return null;
                }
			case '=': // leggo =
                readch(br);
                if (peek == '=') { // leggo un = e restituisco ==
                    peek = ' ';
                    return Word.eq;
                } else {
                    System.err.println("Erroneous character"
                            + " after = : "  + peek );
                    return null;
                }
			// gestione End Of File (EOF)
            case (char)-1:
                return new Token(Tag.EOF);

            default:
				// gestione degli identificatori e parole chiavi (Word)
                if (Character.isLetter(peek)) { 
					String test = "";
					
					// cicla fino a quando leggo una lettera o un numero
					while (Character.isLetter(peek) || Character.isDigit(peek)){
						test += peek; // unisco la stringa test al carattere peek
						readch(br); 
					}
					
						// controllo se la stringa test è una parola chiave
						if (test.equals("assign")){
							return Word.assign;
						} else if (test.equals("to")){
						    return Word.to;
						} else if (test.equals("if")){
						    return Word.iftok;
						} else if (test.equals("else")){
						    return Word.elsetok;
						} else if (test.equals("while")){
						    return Word.whiletok;
						} else if (test.equals("begin")){
						    return Word.begin;
						} else if (test.equals("end")){
						    return Word.end;
						} else if (test.equals("print")){
						    return Word.print;
						} else if (test.equals("read")){
						    return Word.read;
					    } else { // se non è una parola chiave, test è un identificatore
							return new Word(Tag.ID, test);
						}
				
				// gestione delle costanti numeriche (NumberTok)
                } else if (Character.isDigit(peek)) {
					String num = "";
					int i = 1;
					int ris = 0, lunghezza = 0;
					
					// cicla fino a quando leggo un numero
					while (Character.isDigit(peek)){
						num += peek; // unisco la stringa num al carattere peek
						readch(br);
					}
					
					lunghezza = num.length();
					while ((lunghezza - 1) >= 0) { // ciclo fino a quando lunghezza è positiva
						lunghezza--;
						ris += (num.charAt(lunghezza)-48) * i;// assegno a ris il valore in posizione lunghezza e sottraggo 48, per via dello standard ASCII 
						i *= 10;  // moltiplico la i per 10 ad ogni ciclo per avere la potenza di 10
					}
					return new NumberTok(Tag.NUM, ris);

                } else { // carattere non riconosciuto
                        System.err.println("Erroneous character: " 
                                + peek );
                        return null;
                }
            }
    }
		
    public static void main(String[] args) {
        Lexer lex = new Lexer();
        String path = "C:/...";
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Token tok;
            do {
                tok = lex.lexical_scan(br);
                System.out.println("Scan: " + tok);
            } while (tok.tag != Tag.EOF);
            br.close();
        } catch (IOException e) {e.printStackTrace();}    
    }
}