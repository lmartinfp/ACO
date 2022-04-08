package Esqueleto2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;



public class Test {
    public static void main(String[] args) throws IOException {
    	
    	String cadena; //String para guardar lo que se introduzca por teclado
    	int opcion = 0; //Guardar la opción en formato entero
    	String network= "";
    	float traffic[][] = null;
    	int adjacency[][] = null;
    	float capacity[][] = null;
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
	        network= "Topologia 1";               
			capacity = floatMatrixCSV("capacities1.csv");
			traffic = floatMatrixCSV("traffic1.csv");
			adjacency = intMatrixCSV("adjacency1.csv");
		
			break;
		case 2:
		   	// -------------------------------------------------Nobel------------------------------------------
	    	network= "Nobel";
	    	capacity = floatMatrixCSV("nobelCapacities.csv");
			traffic = floatMatrixCSV("nobelTM1.csv");
			adjacency = intMatrixCSV("nobelAdjacency.csv");
			break;
        case 3:
        	//-----------------------------------------------Geant----------------------------------------------------------
        	network= "Geant";      
        	capacity = floatMatrixCSV("geantCapacities.csv");
			traffic = floatMatrixCSV("geantTM1.csv");
			adjacency = intMatrixCSV("geantAdjacency.csv");
			break;
			
		case 4:
		    //----------------------------------------------Germany---------------------------------------------------  
	    	network= "Germany";                                        
	    	capacity = floatMatrixCSV("germanyCapacities.csv");
			traffic = floatMatrixCSV("germanyTM1.csv");
			adjacency = intMatrixCSV("germanyAdjacency.csv");                                    
	         
			break;
	
		case 0:
			return;	
		
		}
		
		//----------------------------------Numero hormigas--------------------------------------
		
		Scanner reader = new Scanner(System.in);
		int nHormigas = 0;

		System.out.println("Introduce el número de hormigas:");

		do {			
			nHormigas = reader.nextInt();
		   
		} while (nHormigas==0);
		//----------------------------------Numero generaciones--------------------------------------
		
		
				int nGen = 0;

				System.out.println("Introduce el número de generaciones:");

				do {			
					nGen = reader.nextInt();
				   
				} while (nGen==0);
    	//---------------------------------------MENU----------------------------------------------
             
		
   		System.out.println("Ejecutando: "+network);

   		
   		System.out.println("Matriz de adyacencia: ");
   		pintarMatriz(adjacency);
   		System.out.println("Matriz de capacidades: ");
   		pintarMatriz(capacity);
   		System.out.println("Matriz de tráfico: ");
   		pintarMatriz(traffic);
   		
   		FileWriter  fresumen =  new FileWriter("./output/resumen.csv");
   		fresumen.append("Topologia");
   		fresumen.append(",");
   		fresumen.append("Hormigas");
   		fresumen.append(",");
   		fresumen.append("Generaciones");
    	fresumen.append(",");
    	fresumen.append("MLU");
    	fresumen.append(",");
    	fresumen.append("MAXhops");
    	fresumen.append("\n");
    	FileWriter  fresumenDijkstra =  new FileWriter("./output/resumenDijkstra.csv");
    	fresumenDijkstra.append("Topologia");
    	fresumenDijkstra.append(",");
    	fresumenDijkstra.append("Hormigas");
    	fresumenDijkstra.append(",");
    	fresumenDijkstra.append("Generaciones");
    	fresumenDijkstra.append(",");
    	fresumenDijkstra.append("MLU");
    	fresumenDijkstra.append(",");
    	fresumenDijkstra.append("MAXhops");
    	fresumenDijkstra.append("\n");
    	
    	
   
    	fresumen.flush();
    	fresumen.close();
    	
    	fresumenDijkstra.flush();
    	fresumenDijkstra.close();
          
        //Las tres matrices se utilizaran para el calculo de la matriz de carga, junto con la de distancias. 
        ACO aco = new ACO(traffic,adjacency,capacity,nHormigas,adjacency.length, nGen, 1.d, 5.d,0 ,0.5d);
        
        
        for (int i = 0; i < 25; i++) {
        	aco.init("cities.txt");
            
        	fresumen =  new FileWriter("./output/resumen.csv",true);
        	fresumenDijkstra =  new FileWriter("./output/resumenDijkstra.csv",true);
        	
        	fresumen.append(network);
        	fresumen.append(",");
        	fresumen.append(String.valueOf(nHormigas));
        	fresumen.append(",");
        	fresumen.append(String.valueOf(nGen));
        	fresumen.append(",");
        	
        	fresumenDijkstra.append(network);
        	fresumenDijkstra.append(",");
        	fresumenDijkstra.append(String.valueOf(nHormigas));
        	fresumenDijkstra.append(",");
        	fresumenDijkstra.append(String.valueOf(nGen));
        	fresumenDijkstra.append(",");
        	
        	fresumen.flush();
        	fresumen.close();
        	
        	fresumenDijkstra.flush();
        	fresumenDijkstra.close();
            try {
    			aco.solve(i);
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
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
	   private static void pintarMatriz(float matriz[][]) {
				for (int x=0; x < matriz.length; x++) {
					  System.out.print("|");
					  for (int y=0; y < matriz[x].length; y++) {
					    System.out.print (matriz[x][y]);
					    if (y!=matriz[x].length-1) System.out.print("\t");
					  }
					  System.out.println("|");
					}
				
			}
	
	   static int[][] intMatrixCSV(String pathToCsv) throws IOException {
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

	   static float[][] floatMatrixCSV(String pathToCsv) throws IOException {
		   BufferedReader csvReader = new BufferedReader(new FileReader(pathToCsv));
		   String row;
		   
		   row=csvReader.readLine();
		   String[] data = row.split(",");
		   int tamanio=data.length;
		   float matrix[][]=new float[tamanio][tamanio];
		   int i=0;
		   csvReader= new  BufferedReader(new FileReader(pathToCsv));
		   while ((row = csvReader.readLine()) != null) {
		       data = row.split(",");
		       // do something with the data
		       for (int j=0;j< tamanio;j++) {
		   
		    	   matrix[i][j]= Float.valueOf(data[j]);
			     
			   }
		       i++;  
			}
		   
		   
		   csvReader.close();
		return matrix;
	    }
}
