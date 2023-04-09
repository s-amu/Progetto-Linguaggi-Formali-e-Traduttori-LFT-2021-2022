public class Identificatori{
	public static boolean scan(String s){
		int state = 0;
		int i = 0;
		
		while (state >= 0 && i < s.length()){
			final char ch = s.charAt(i++);
			
			switch (state){
			case 0:
				if (ch == '_')
					state = 2;
				else if (ch <= 'Z' && ch >= 'A' || ch <= 'z' && ch >= 'a')
					state = 3;
				else 
					state = -1;
				break;
				
			case 2:
				if (ch == '_')
					state = 2;
				else if (ch <= 'Z' && ch >= 'A' || ch <= 'z' && ch >= 'a' || 
						 ch <= '9' && ch >= '0')
					state = 3;
				else
					state = -1;
				break;
				
			case 3:
				if (ch == '_' || ch <= 'Z' && ch >= 'A' || 
					ch <= 'z' && ch >= 'a' || ch <= '9' && ch >= '0')
					state = 3;
				else
					state = -1;
				break;
			}
		}
		return state == 3;
	}

    public static void main(String[] args){
	System.out.println(scan(args[0]) ? "OK" : "NOPE");
    }
}