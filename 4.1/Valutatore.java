import java.io.*; 

public class Valutatore extends Lexer{
    private Lexer lex;
    private BufferedReader pbr;
    private Token look;

    public Valutatore(Lexer l, BufferedReader br) { 
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
		int expr_val;
		
    	switch(look.tag) {
		case '(':
		case Tag.NUM:
			// start --> expr EOF { print(expr.val) }
			expr_val = expr();
			match(Tag.EOF);
			System.out.println(expr_val);
			break;
		default:
			error("Error in <start> with Token: " + look + "\n");
		}
    }

    private int expr() { 
		int term_val, exprp_val = 0;

		switch (look.tag) {
		case '(':
		case Tag.NUM:
			// expr --> term { exprp.i = term.val } exprp { expr.val = exprp.val }
			term_val = term();
			exprp_val = exprp(term_val);
			break;
		default:
			error("Error in <expr> with Token: " + look + "\n");
		}
	return exprp_val; // attributo sintetizzato
    }

    private int exprp(int exprp_i) { // attributo ereditato
		int term_val, exprp_val = 0;
		
		switch (look.tag) {
		case '+':
			// exprp --> + term { exprp1.i = exprp.i + term.val } exprp1 { exprp.val = exprp1.val }
			match('+');
			term_val = term();
			exprp_val = exprp(exprp_i + term_val);
			break;
		case '-':
			// exprp --> - term { exprp1.i = exprp.i - term.val } exprp1 { exprp.val = exprp1.val }
			match('-');
			term_val = term();
			exprp_val = exprp(exprp_i - term_val);
			break;
		case ')':
		case Tag.EOF:
			// exprp --> epsilon { exprp.val = exprp.i }
			exprp_val = exprp_i;
			break;
		default:
			error("Error in <exprp> with Token: " + look + "\n");
		}
	return exprp_val; // attributo sintetizzato
    }

    private int term() { 
		int fact_val, termp_val = 0;

		switch (look.tag) {
		case '(':
		case Tag.NUM:
			// term --> fact { termp.i = fact.val } termp { term.val = termp.val }
			fact_val = fact();
			termp_val = termp(fact_val);
			break;
		default:
			error("Error in <term> with Token: " + look + "\n");
		}
	return termp_val;  // attributo sintetizzato
    }
    
    private int termp(int termp_i) { // attributo ereditato
		int fact_val, termp_val = 0;
		
		switch (look.tag) {
		case '*':
			// termp --> * fact { termp1.i = termp.i * fact.val } termp1 { termp.val = termp1.val }
			match('*');
			fact_val = fact();
			termp_val = termp(termp_i * fact_val);
			break;
		case '/':
			// termp --> / fact { termp1.i = termp.i / fact.val } termp1 { termp.val = termp1.val }
			match('/');
			fact_val = fact();
			termp_val = termp(termp_i / fact_val);
			break;
		case '-':
		case '+':
		case ')':
		case Tag.EOF:
			// termp --> epsilon { termp.val = termp.i }
			termp_val = termp_i;
			break;
		default:
			error("Error in <termp> with Token: " + look + "\n");
		}
	return termp_val; // attributo sintetizzato
    }
    
    private int fact() { 
		int fact_val = 0;
		
		switch (look.tag) {
		case Tag.NUM:
			// fact --> NUM { fact.val = NUM.value }
			match(Tag.NUM);
			fact_val = NumberTok.val;
			break;
		case '(':
			// fact --> ( expr ) { fact.val = expr.val }
			match('(');
			fact_val = expr();
			match(')');
			break;
		default:
			error("Error in <fact> with Token: " + look + "\n");
		}
	return fact_val; // attributo sintetizzato
    }

    public static void main(String[] args) {
        Lexer lex = new Lexer();
        String path = "C:/..."; 
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Valutatore valutatore = new Valutatore(lex, br);
            valutatore.start();
            br.close();
        } catch (IOException e) {e.printStackTrace();}
    }
}