import java.io.*;

public class Translator extends Lexer{
    private Lexer lex;
    private BufferedReader pbr;
    private Token look;

    SymbolTable st = new SymbolTable();
    CodeGenerator code = new CodeGenerator();
    int count = 0;
    boolean read, assign = false;  // booleano per read e assign
	
    public Translator(Lexer l, BufferedReader br) {
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
			int lnext_prog = code.newLabel();
			statlist();
			code.emitLabel(lnext_prog); // scrive etichetta al fondo L0
			match(Tag.EOF);
        try {
        	code.toJasmin();
        }
        catch(java.io.IOException e) {
        	System.out.println("IO error\n");
        };
			break;
		default:
			error("Error in <prog> with Token: " + look + "\n");
		}
	}

	private void statlist() { /// prima etichetta L1
		switch (look.tag) {
		case Tag.ASSIGN:
		case Tag.PRINT:
		case Tag.READ:
		case Tag.WHILE:
		case Tag.IF:
		case '{':
			int lnext_statlist = code.newLabel();
			stat(lnext_statlist);
			code.emitLabel(lnext_statlist);
			statlistp();
			break;
		default:
			error("Error in <statlist> with Token: " + look + "\n");
		}
	}

	private void statlistp() { /// etichette per ogni riga che finisce con ;
		switch (look.tag) {
		case ';':
			match(';');
			int lnext_statlistp = code.newLabel();
			stat(lnext_statlistp);
			code.emitLabel(lnext_statlistp);
			statlistp();
			break;
		case Tag.EOF:
		case '}':
			break;
		default:
			error("Error in <statlistp> with Token: " + look + "\n");
		}
	}	

    private void stat(int stat_next) {
        switch (look.tag) {
		case Tag.ASSIGN:
			assign = true;
			match(Tag.ASSIGN);
			expr();
			match(Tag.TO);
			idlist();
			assign = false;
			break;
		case Tag.PRINT:
			match(Tag.PRINT);
			match('(');
			exprlist(OpCode.ineg);
			match(')');
			break;
        case Tag.READ:
			read = true;
			code.emit(OpCode.invokestatic, 0);
			match(Tag.READ);
			match('(');
			idlist();
			match(')');
			read = false;
			break;
		case Tag.WHILE:
			match(Tag.WHILE);
			int whileTrue = code.newLabel(), whileReturn = code.newLabel();
			// etichetta se condizione vera  // etichetta per ritornare all'inizio del while
			code.emitLabel(whileReturn); // emetto label per fissare il ritorno prima del while
			match('(');
			bexpr(whileTrue, stat_next); // vado in bexpr ed emetto if_icmp con etichetta true e sotto un goto con etichetta falsa
			code.emitLabel(whileTrue); // stampa l'etichetta per il true e prosegue col codice di stat
			match(')');
			stat(whileReturn);
			code.emit(OpCode.GOto, whileReturn); // stampa goto per tornare all'inizio del ciclo
			break;
		case Tag.IF:
			match(Tag.IF);
			int ifTrue = code.newLabel(), ifFalse = code.newLabel();
			// etichetta se condizione vera // etichetta se condizione falsa
			match('(');
			bexpr(ifTrue, ifFalse); // vado in bexpr ed emetto if_icmp con etichetta true e sotto un goto con etichetta falsa
			code.emitLabel(ifTrue); // stampa l'etichetta per il true e prosegue col codice di stat
			match(')');
			stat(stat_next);
			code.emit(OpCode.GOto, stat_next); // stampo goto a stat_next per saltare il codice dell'else e continuo la traduzione
			code.emitLabel(ifFalse); // stampa l'etichetta per il false e prosegue col codice di statp 
			statp(stat_next); 
			break;
		case '{':
			match('{');
			statlist();
			match('}');
			break;
		default:
			error("Error in <stat> with Token: " + look + "\n");
        }
     }
	 
	private void statp(int statp_next) {
        switch (look.tag) {
		case Tag.ELSE:
			match(Tag.ELSE);
			stat(statp_next);
			match(Tag.END);
			break;
		case Tag.END:
			match(Tag.END);
			break;
		default:
			error("Error in <statp> with Token: " + look + "\n");
		}
    }

    private void idlist() {
        switch(look.tag) {
	    case Tag.ID:
			int id_addr = st.lookupAddress(((Word)look).lexeme); //assegna ad id_addr l'indirizzo dell'identificatore se è presente
                if (id_addr==-1) { // se id_addr è -1 vuol dire che non è  presente e lo aggiugne alla mappa
                    id_addr = count;
                    st.insert(((Word)look).lexeme, count++);
				}
                match(Tag.ID);
				code.emit(OpCode.istore, id_addr); 
				idlistp(id_addr);
				break;
		default:
			error("Error in <idlist> with Token: " + look + "\n");
    	}
    }

	private void idlistp(int id_addr_istore_idlist) {
        switch(look.tag) {
	    case ',':
			match(',');
			int id_addr = st.lookupAddress(((Word)look).lexeme); //assegna ad id_addr l'indirizzo dell'identificatore
                if (id_addr==-1) { // se id_addr è -1 vuol dire che non è  presente e lo aggiugne alla mappa
                    id_addr = count;
                    st.insert(((Word)look).lexeme, count++);
                }
				match(Tag.ID);
				if (read)  // se read == true stampo invokestatic read perchè vengo da <stat> read
					code.emit(OpCode.invokestatic, 0); /// serve per fare più invokestatic read in idlistp
				if (assign) // se assign == true stampo un iload con l'indirizzo passatogli da idlist
					code.emit(OpCode.iload, id_addr_istore_idlist); /// serve per fare più assign in idlistp
				code.emit(OpCode.istore, id_addr);
				idlistp(id_addr_istore_idlist);
				break;
		case Tag.EOF:
		case ';':
		case ')':
		case Tag.END:
		case Tag.ELSE:
		case '}':
				break;
		default:
			error("Error in <idlistp> with Token: " + look + "\n");
    	}
    }
	
	private void bexpr(int labelTrue, int labelFalse) {
		if (look.tag == Tag.RELOP) {
			
			String relop = ((Word)look).lexeme;
			match(Tag.RELOP);
			expr();
			expr();

			switch (relop) { // stampo if_icmp con accanto l'etichetta per il true
			case ">":
				code.emit(OpCode.if_icmpgt, labelTrue);
				break;
			case "<":
				code.emit(OpCode.if_icmplt, labelTrue);
				break;
			case ">=":
				code.emit(OpCode.if_icmpge, labelTrue);
				break;
			case "<=":
				code.emit(OpCode.if_icmple, labelTrue);
				break;
			case "<>":
				code.emit(OpCode.if_icmpne, labelTrue);
				break;
			case "==":
				code.emit(OpCode.if_icmpeq, labelTrue);
				break;
			default:
				error("Error in <bexpr> with String: " + relop + "\n");
			}
			code.emit(OpCode.GOto, labelFalse); // subito sotto l'if_icmp stampo un goto con l'etichetta per il false
		} else {
		    error("Error in <bexpr> with Token: " + look + "\n");
	    }
    }

    private void expr() {
        switch(look.tag) {
        case  '+':
			match('+');
			match('(');
			exprlist(OpCode.iadd);
			match(')');
			break;
     	case '-':
            match('-');
            expr();
            expr();
            code.emit(OpCode.isub);
            break;
		case '*':
			match('*');
			match('(');
			exprlist(OpCode.imul);
			match(')');
			break;
		case '/':
			match('/');
			expr();
			expr();
			code.emit(OpCode.idiv);
			break;
		case Tag.NUM:
			int val = NumberTok.val;
			match(Tag.NUM);
			code.emit(OpCode.ldc, val);
			break;
		case Tag.ID:
			int id_addr = st.lookupAddress(((Word)look).lexeme); //assegna ad id_addr l'indirizzo dell'identificatore
                if (id_addr==-1) { // se id_addr è -1 vuol dire che non è presente e lo aggiugne alla mappa
                    id_addr = count;
                    st.insert(((Word)look).lexeme, count++);
                }
				match(Tag.ID);
				code.emit(OpCode.iload, id_addr);
				break;
		default:
			error("Error in <expr> with Token: " + look + "\n");
		}
    }

	private void exprlist(OpCode operando) {
       switch (look.tag) {
		case Tag.ID:
		case '+':
		case '-':
		case '*':
		case '/':
		case Tag.NUM:
			expr();
			if ((operando != OpCode.iadd) && (operando != OpCode.imul)) /// serve per fare invokestatic print in exprlist
				code.emit(OpCode.invokestatic, 1); // se arrivo da stat stampo invokestatic, se arrivo da expr no
			/// non mette le prime print senza
			exprlistp(operando);
			break;
		default:
			error("Error in <exprlist> with Token: " + look + "\n");
		}
	}

	private void exprlistp(OpCode operando) {
        switch (look.tag) {
		case ',':
			match(',');
			expr();
			if ((operando != OpCode.iadd) && (operando != OpCode.imul)) /// serve per fare invokestatic print in exprlistp
				code.emit(OpCode.invokestatic, 1); // se arrivo da stat stampo invokestatic, se arrivo da expr no
			/// non mette le ultime print senza
			if (operando == OpCode.iadd) // caso di iadd e imul da expr, diverso da - e /
				code.emit(OpCode.iadd);
			else if (operando == OpCode.imul) /// senza metterebbe solamente un iadd o imul alla fine
				code.emit(OpCode.imul);
			exprlistp(operando);
			break;
		case ')':
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
            Translator translator = new Translator(lex, br);
            translator.prog();
            br.close();
        } catch (IOException e) {e.printStackTrace();}
    }
}