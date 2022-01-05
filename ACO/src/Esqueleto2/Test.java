package Esqueleto2;

public class Test {
    public static void main(String[] args) {
    	int traffic[][] = new int[][] { 
    		{ 0, 10, 10, 10, 0},
            { 10, 0, 10, 0, 10},
            { 10, 10, 0, 10, 10},
            { 10, 0, 10, 0, 10},
            { 0, 10, 10, 10, 0},
             };
             int adjacency[][] = new int[][] { 
         		{ 0, 1, 1, 1, 0},
                { 1, 0, 1, 0, 1},
                { 1, 1, 0, 1, 1},
                { 1, 0, 1, 0, 1},
                { 0, 1, 1, 1, 0},
                  };
             int capacity[][] = new int[][] { 
               		  { 0, 100, 100, 100, 0},
                      { 100, 0, 100, 0, 100},
                      { 100, 100, 0, 100, 100},
                      { 100, 0, 100, 0, 100},
                      { 0, 100, 100, 100, 0},
                        };  
                        
                  
        //Las tres matrices se utilizaran para el calculo de la matriz de carga, junto con la de distancias.          
        ACO aco = new ACO(traffic,adjacency,capacity,0,1,6,adjacency.length, 10, 1.d, 5.d, 0.5d);
        aco.init("cities2.txt");
        
        
        aco.solve();//origen 1 destino 5
    }
}
