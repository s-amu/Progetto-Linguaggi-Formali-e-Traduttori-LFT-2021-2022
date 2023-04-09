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

    public void prog() {
		switch(look.tag) {
		case Tag.ASSIGN:
		case Tag.PRINT:
		case Tag.READ:
		case Tag.WHILE:
		case Tag.IF:
		case '{':
			// prog --> statlist EOF
			statlist();
			match(Tag.EOF);
			break;
		default:
			error("Error in <prog> with Token: " + look + "\n");
		}
	}
	
    private void statlist() {	
		switch (look.tag) {
		case Tag.ASSIGN:
		case Tag.PRINT:
		case Tag.READ:
		case Tag.WHILE:
		case Tag.IF:
		case '{':
			// statlist --> stat statlistp
			stat();
			statlistp();
			break;
		default:
			error("Error in <statlist> with Token: " + look + "\n");
		}
    }

    private void statlistp() {
		switch (look.tag) {
		case ';':
			// statlistp --> ; stat statlistp
			match(';');
			stat();
			statlistp();
			break;
		case Tag.EOF:
		case '}':
			// statlistp --> epsilon
			break;
		default:
			error("Error in <statlistp> with Token: " + look + "\n");
		}
    }
	
    private void stat() {
        switch (look.tag) {
		case Tag.ASSIGN:
			// stat --> assign expr to idlist
			match(Tag.ASSIGN);
			expr();
			match(Tag.TO);
			idlist();
			break;
		case Tag.PRINT:
			// stat --> print ( exprlist )
			match(Tag.PRINT);
			match('(');
			exprlist();
			match(')');
			break;
		case Tag.READ:
			// stat --> read ( idlist )
			match(Tag.READ);
			match('(');
			idlist();
			match(')');
			break;
		case Tag.WHILE:
			// stat --> while ( bexpr ) stat
			match(Tag.WHILE);
			match('(');
			bexpr();
			match(')');
			stat();
			break;
		case Tag.IF:
			// if ( bexpr ) stat statp
			match(Tag.IF);
			match('(');
			bexpr();
			match(')');
			stat();
			statp();
			break;
		case '{':
			// { statlist }
			match('{');
			statlist();
			match('}');
			break;
		default:
			error("Error in <stat> with Token: " + look + "\n");
		}
    }

	 private void statp() {
        switch (look.tag) {
		case Tag.ELSE:
			// statp --> else stat end
			match(Tag.ELSE);
			stat();
			match(Tag.END);
			break;
		case Tag.END:
			// statp --> end
			match(Tag.END);
			break;
		default:
			error("Error in <statp> with Token: " + look + "\n");
		}
    }

    private void idlist() {
        switch (look.tag) {
		case Tag.ID:
			// idlist --> ID idlistp
			match(Tag.ID);
			idlistp();
			break;
		default:
			error("Error in <idlist> with Token: " + look + "\n");
		}
    }

    private void idlistp() {
        switch (look.tag) {
		case ',':
			// idlistp --> , ID idlistp
			match(',');
			match(Tag.ID);
			idlistp();
			break;
		case Tag.EOF:
		case ';':
		case ')':
		case Tag.END:
		case Tag.ELSE:
		case '}':
			// idlistp --> epsilon
			break;
		default:
			error("Error in <idlistp> with Token: " + look + "\n");
		}
    }
	
	private void bexpr() {
        switch (look.tag) {
		case Tag.RELOP:
			// bexpr --> RELOP expr expr
			match(Tag.RELOP);
			expr();
			expr();
			break;
		default:
			error("Error in <bexpr> with Token: " + look + "\n");
		}
    }
	
	private void expr() {
        switch (look.tag) {
		case  '+':
			// expr --> + ( exprlist )
			match('+');
			match('(');
			exprlist();
			match(')');
			break;
		case '-':
			// expr --> - expr expr
			match('-');
			expr();
			expr();
			break;
		case '*':
			// expr --> * ( exprlist )
			match('*');
			match('(');
			exprlist();
			match(')');
			break;
		case '/':
			// expr --> / expr expr
			match('/');
			expr();
			expr();
			break;
		case Tag.NUM:
			// expr --> NUM
			match(Tag.NUM);
			break;
		case Tag.ID:
			// expr --> ID
			match(Tag.ID);
			break;
		default:
			error("Error in <expr> with Token: " + look + "\n");
		}
    }
	
	private void exprlist() {
        switch (look.tag) {
		case Tag.ID:
		case '+':
		case '-':
		case '*':
		case '/':
		case Tag.NUM:
			// exprlist --> expr exprlistp
			expr();
			exprlistp();
			break;
		default:
			error("Error in <exprlist> with Token: " + look + "\n");
		}
    }
		
	private void exprlistp() {
        switch (look.tag) {
		case ',':
			// exprlistp --> , expr exprlistp
			match(',');
			expr();
			exprlistp();
			break;
		case ')':
			// exprlistp --> epsilon
			break;
		default:
			error("Error in <exprlistp> with Token: " + look + "\n");
		}
    }

    public static void main(String[] args) {
        Lexer lex = new Lexer();
        String path = "C:/...";
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Parser parser = new Parser(lex, br);
            parser.prog();
            System.out.println("Input OK");
            br.close();
        } catch (IOException e) {e.printStackTrace();}
    }
}