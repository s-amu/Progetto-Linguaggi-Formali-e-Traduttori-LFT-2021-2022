public class MatricolaCognome{
    public static boolean scan(String s){
		int state = 0;
		int i = 0;

		while (state >= 0 && i < s.length()){
			final char ch = s.charAt(i++);

			switch (state){
			case 0:
				if (ch == '1' || ch == '3' || ch == '5' || 
				    ch == '7' || ch == '9')
					state = 1;
				else if (ch == '0' || ch == '2' || ch == '4' ||
				         ch == '6' || ch == '8')
				    state = 2;
				else 
					state = -1;
				break;
				
			case 1:
				if (ch == '1' || ch == '3' || ch == '5' || 
				    ch == '7' || ch == '9')
					state = 1;
				else if (ch == '0' || ch == '2' || ch == '4' ||
				         ch == '6' || ch == '8')
					state = 2;
				else if (ch <= 'Z' && ch >= 'L')
					state = 4;
				else
					state = -1;
				break;
				
			case 2:
				if (ch == '0' || ch == '2' || ch == '4' ||
				    ch == '6' || ch == '8')
					state = 2;
				else if (ch == '1' || ch == '3' || ch == '5' || 
				         ch == '7' || ch == '9')
				    state = 1;
				else if (ch <= 'K' && ch >= 'A')
					state = 4;
				else
					state = -1;
				break;
				
			case 4:
				if (ch <= 'z' && ch >= 'a')
					state = 4;
				else
					state = -1;
				break;
			}
		}
		return state == 4;
	}
	
    public static void main(String[] args){
	System.out.println(scan(args[0]) ? "OK" : "NOPE");
    }
}