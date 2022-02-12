package Esqueleto2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;



public class Test {
    public static void main(String[] args) throws IOException {
    	
    	String cadena; //String para guardar lo que se introduzca por teclado
    	int opcion = 0; //Guardar la opción en formato entero
    	String network= "";
    	int traffic[][] = null;
    	int adjacency[][] = null;
    	int capacity[][] = null;
    	do {
    	System.out.println("Elegir topología:");
    	System.out.println("1- Topología 1");
    	System.out.println("2- Nobel");
    	System.out.println("3- Geant");
    	System.out.println("4- Germany");
    	System.out.println("0- Salir");
    	System.out.println("Topología-");
    	
    	
    	Scanner sc = new Scanner(System.in); //Scanner para leer la entrada por teclado
    	cadena = sc.nextLine(); //Leer la opción
    	
    	opcion = Integer.parseInt(cadena); //True si es un número false si no lo es

    	}while(opcion<0||opcion>4);
    	
    	
		switch(opcion) {
		case 1:
		//  --------------------------------------------------TOPOLOGIA 1-------------------------------------------  
//	                        
			capacity = loadMatrixCSV("capacities1.csv");
			traffic = loadMatrixCSV("traffic1.csv");
			adjacency = loadMatrixCSV("adjacency1.csv");
		
			break;
		case 2:
		   	// -------------------------------------------------Nobel------------------------------------------
	    	network= "Nobel";
	    	capacity = loadMatrixCSV("nobelCapacities.csv");
			traffic = loadMatrixCSV("nobelTM1.csv");
			adjacency = loadMatrixCSV("nobelAdjacency.csv");
			break;
        case 3:
        	//-----------------------------------------------Geant----------------------------------------------------------
        	network= "Geant";      
        	capacity = loadMatrixCSV("geantCapacities.csv");
			traffic = loadMatrixCSV("geantTM1.csv");
			adjacency = loadMatrixCSV("geantAdjacency.csv");
			break;
			
		case 4:
		    //----------------------------------------------Germany---------------------------------------------------  
	    	network= "Germany";                                        
	    	capacity = loadMatrixCSV("germanyCapacities.csv");
			traffic = loadMatrixCSV("germanyTM1.csv");
			adjacency = loadMatrixCSV("germanyAdjacency.csv");                                    
	         
			break;
	
		case 0:
			return;	
		
		}

    	//---------------------------------------MENU----------------------------------------------
             
		
        Scanner reader = new Scanner(System.in);
   		System.out.println("Ejecutando: "+network);

   		
   		System.out.println("Matriz de adyacencia: ");
   		pintarMatriz(adjacency);
   		System.out.println("Matriz de capacidades: ");
   		pintarMatriz(capacity);
   		System.out.println("Matriz de tráfico: ");
   		pintarMatriz(traffic);
   		
               
        //Las tres matrices se utilizaran para el calculo de la matriz de carga, junto con la de distancias.          
        ACO aco = new ACO(traffic,adjacency,capacity,5,adjacency.length, 5, 1.d, 5.d, 0.5d);
        aco.init("cities.txt");
        
        
        try {
			aco.solve();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	   private static void pintarMatriz(int matriz[][]) {
			for (int x=0; x < matriz.length; x++) {
				  System.out.print("|");
				  for (int y=0; y < matriz[x].length; y++) {
				    System.out.print (matriz[x][y]);
				    if (y!=matriz[x].length-1) System.out.print("\t");
				  }
				  System.out.println("|");
				}
			
		}
	
	   static int[][] loadMatrixCSV(String pathToCsv) throws IOException {
		   BufferedReader csvReader = new BufferedReader(new FileReader(pathToCsv));
		   String row;
		   
		   row=csvReader.readLine();
		   String[] data = row.split(",");
		   int tamanio=data.length;
		   int matrix[][]=new int[tamanio][tamanio];
		   int i=0;
		   csvReader= new  BufferedReader(new FileReader(pathToCsv));
		   while ((row = csvReader.readLine()) != null) {
		       data = row.split(",");
		       // do something with the data
		       for (int j=0;j< tamanio;j++) {
		   
		    	   matrix[i][j]= Integer.parseInt(data[j]);
			     
			   }
		       i++;  
			}
		   
		   
		   csvReader.close();
		return matrix;
	    }

	
}
