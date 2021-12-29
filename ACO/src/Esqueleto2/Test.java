package Esqueleto2;

public class Test {
    public static void main(String[] args) {
    	int traffic[][] = new int[][] { 
    		{ 0, 10, 10, 10, 10},
            { 10, 0, 10, 10, 10},
            { 10, 10, 0, 10, 10},
            { 10, 10, 10, 0, 10},
            { 10, 10, 10, 10, 0},
             };
             int adjacency[][] = new int[][] { 
         		{ 0, 1, 1, 1, 0},
                { 1, 0, 1, 0, 1},
                { 1, 1, 0, 1, 1},
                { 1, 0, 1, 0, 1},
                { 0, 1, 1, 1, 0},
                  };
                  
        ACO aco = new ACO(traffic,adjacency,6, 5, 2000, 1.d, 5.d, 0.5d);
        aco.init("cities2.txt");
        
        
        aco.solve(1,5);//origen 1 destino 5
    }
}
