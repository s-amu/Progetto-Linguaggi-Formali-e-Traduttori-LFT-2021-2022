import java.util.*;

public class SymbolTable {

     Map <String, Integer> OffsetMap = new HashMap <String,Integer>(); //mappa di String(nome variabile) e Integer(valore associato)

	public void insert( String s, int address ) { // inserisce una variabile(x,y,z...) con il suo indirizzo(0,1,2...) nella mappa
            if( !OffsetMap.containsValue(address) )  // se la mappa non contiene l'indirizzo lo aggiunge 
                OffsetMap.put(s,address);
            else // altrimenti se c'è già eccezione
                throw new IllegalArgumentException("Reference to a memory location already occupied by another variable");
	}

	public int lookupAddress ( String s ) { // controlla se c'è una variabile nella mappa
            if( OffsetMap.containsKey(s) ) 
                return OffsetMap.get(s); // se è presente nella mappa la restituisce
            else
                return -1;  // altrimenti ritorna -1 (non è un errore)
	}
}
