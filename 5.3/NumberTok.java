public class NumberTok extends Token {
	public static int val = 0;
    public NumberTok(int tag, int v) { super(tag); val = v; }
    public String toString() { return "<" + tag + ", " + val + ">"; }
}
 