import java.io.*;

public class Parser extends Lexer{
    private Lexer lex;
    private BufferedReader pbr;
    private Token look;

    public Parser(Lexer l, BufferedReader br) {
        lex = l;
        pbr = br;
        move();
    }

    void move() {
        look = lex.lexical_scan(pbr);
        System.out.println("token = " + look);
    }

    void error(String s) {
		throw new Error("near line " + lex.line + ": " + s);
    }

    void match(int t) {
		if (look.tag == t) {
			if (look.tag != Tag.EOF) move();
		} else error("syntax error");
    }

    public void start() {
		switch(look.tag) {
		case '(':
		case Tag.NUM:
			// start --> expr EOF
			expr();
			match(Tag.EOF);
			break;
		default:
			error("Error in <start> with Token: " + look + "\n");
		}
	}
	
    private void expr() {	
		switch (look.tag) {
		case '(':
		case Tag.NUM:
			// expr --> term exprp
			term();
			exprp();
			break;
		default:
			error("Error in <expr> with Token: " + look + "\n");
		}
    }

    private void exprp() {
		switch (look.tag) {
		case '+':
			// exprp --> + term exprp
			match('+');
			term();
			exprp();
			break;
		case '-':
			// exprp --> - term exprp
			match('-');
			term();
			exprp();
			break;
		case ')':
		case Tag.EOF:
			// exprp --> epsilon
			break;
		default:
			error("Error in <exprp> with Token: " + look + "\n");
		}
    }
	
    private void term() {
        switch (look.tag) {
		case '(':
	    case Tag.NUM:
			// term --> fact termp
			fact();
			termp();
			break;
		default:
			error("Error in <term> with Token: " + look + "\n");
		}
    }

    private void termp() {
        switch (look.tag) {
		case '*':
			// termp --> * fact termp
			match('*');
			fact();
			termp();
			break;
		case '/':
			// termp --> / fact termp
			match('/');
			fact();
			termp();
			break;
		case '-':
		case '+':
		case ')':
		case Tag.EOF:
			// termp --> epsilon
			break;
		default:
			error("Error in <termp> with Token: " + look + "\n");
		}
    }

    private void fact() {
        switch (look.tag) {
		case Tag.NUM:
			// fact --> NUM
			match(Tag.NUM);
			break;
		case '(':
			// fact --> (expr)
			match('(');
			expr();
			match(')');
			break;
		default:
			error("Error in <fact> with Token: " + look + "\n");
		}
    }
		
    public static void main(String[] args) {
        Lexer lex = new Lexer();
        String path = "C:/...";
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Parser parser = new Parser(lex, br);
            parser.start();
            System.out.println("Input OK");
            br.close();
        } catch (IOException e) {e.printStackTrace();}
    }
}