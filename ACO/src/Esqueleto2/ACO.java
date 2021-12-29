package Esqueleto2;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.YenKShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import com.opencsv.CSVWriter;

/**
   * Algoritmo de colonia de hormigas
 * @author gbs
 *
 */
public class ACO {

    private Ant[] ants; //hormigas
    private int antNum; // Número de hormigas
    private int cityNum; // Número de ciudades
    private int MAX_GEN; // Ejecutar álgebra (número de veces que se ejecuta el algoritmo)
    private double[][] pheromone; // Matriz de feromonas
    private int[][] distance; // Matriz de distancias (coste)
    private int[][] load; // carga de trafico que tiene un nodo en un momento dado
    private int[][] traffic; 
    private int bestLength; // Longitud óptima
    private int[] bestTour; // Mejor camino
    // Tres parámetros
    private double alpha; 
    private double beta;
    private double rho;
	private int[][] adjacency;

    /*
     * Constructor por defecto de la clase ACO
     */
    public ACO() {

    }

    /*
     * Constructor parametrizado
     */
    public ACO(int [][] traffic,int[][]adjacency ,int antNum, int cityNum, int mAX_GEN, double alpha, double beta, double rho) {
        this.traffic=traffic;
        this.adjacency =adjacency;
    	this.antNum = antNum;
        this.cityNum = cityNum;
        this.MAX_GEN = mAX_GEN;
        this.alpha = alpha;
        this.beta = beta;
        this.rho = rho;
        this.ants = new Ant[this.antNum];
    }

    /*
     * Método para inicializar las variable. Lee un fichero con las ciudades a partir del cual calcula la matriz de distancias.
     */
    public void init(String path) {
        int []x;
        int []y;
        String buffer;
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
            this.distance = new int[this.cityNum][this.cityNum];
            this.load = new int[this.cityNum][this.cityNum];
            x = new int[cityNum];  
            y = new int[cityNum];  
            // Leer las coordenadas de cada nodo
            for (int i = 0; i < cityNum; i++) {  
                buffer = br.readLine();
                String[] str = buffer.split(" +");  
                x[i] = Integer.valueOf(str[1]);  
                y[i] = Integer.valueOf(str[2]);  
            } 
            /**
             * Calcular la matriz de distancias. Para problemas específicos, el método de cálculo de distancias es diferente. Aquí se utiliza att48 como caso.
             * Tiene 48 ciudades, el método de cálculo de distancia es la distancia pseudoeuclidiana y el valor óptimo es 10628
             */
            for(int i = 0;i < this.cityNum - 1;i++) {
                for(int j = i + 1;j < this.cityNum;j++) {
                    double rij = Math.sqrt(((x[i]-x[j])*(x[i]-x[j]) + (y[i]-y[j])*(y[i]-y[j]))/10.0);//sqrt calcula la raiz cuadrada
                    int tij = (int)Math.round(rij);
                    if(tij < rij)
                        tij++;
                    this.distance[i][j] = tij;
                    this.distance[j][i] = tij;
                	//PONEMOS LA MATRIZ DE CARGA A 0 INICIALMENTE
                	 this.load[i][j] = 0;
                     this.load[j][i] = 0;
                }
            }
            this.distance[this.cityNum-1][this.cityNum-1] = 0;
            
            //Las aristas que no están conectadas las ponemos a 0
            finalizarMatrizCostes();          
            
     
            
            // Inicializar la matriz de feromonas
            this.pheromone=new double[this.cityNum][this.cityNum];
            for(int i = 0;i < this.cityNum;i++) {
                for(int j = 0;j < this.cityNum;j++) {
                    this.pheromone[i][j] = 0.1d; //valor por defecto de las feromonas
                }
            }
            // Inicializa la longitud de la ruta óptima
            this.bestLength=Integer.MAX_VALUE;
            // Inicializar la ruta óptima
            this.bestTour=new int[this.cityNum+1];  
            // Coloca las hormigas al azar  
            for(int i = 0;i < this.antNum;i++){  //Inicializamos el vector de hormigas= creamos las hormigas
                this.ants[i]=new Ant(this.cityNum);  
                this.ants[i].init(this.distance, this.alpha, this.beta);  
            }  
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void finalizarMatrizCostes() {
		for (int i = 0; i < this.adjacency.length; i++) {
			for (int j = 0; j < this.adjacency.length; j++) {
				if (this.adjacency[i][j]==0) {//No tenemos arista entre estos dos nodos
					this.distance[i][j]=9999;
				}
			}
		}
	}

	/**
     * Actualizar feromonas
     * Seguir formula wuham
     */
    private void updatePheromone() {
        // Volatilización de feromonas  
        for(int i = 0;i < this.cityNum;i++)  
            for(int j = 0;j < this.cityNum;j++)  
                this.pheromone[i][j] = this.pheromone[i][j] * (1 - this.rho);//RHO es el valor de volatilizacion de las feromonas 
        // Actualización de feromonas (alta complejidad)
        for(int i = 0;i < this.cityNum;i++) {
            for(int j = 0;j < this.cityNum;j++) {
                for(int k = 0;k < this.antNum;k++) {
                    this.pheromone[i][j] += this.ants[k].getDelta()[i][j];//Utilizamos una formula para actualizar feromonas que probablemente haya que cambiar
                }
            }
        }
    }

    /*
     * Método que implementa el movimiento de las hormigas
     */
    public void solve(int origen, int destino) {
    	
    	calculateKshortestPath(origen,destino);
   
  /*  	
    	
     //   for (int g = 0; g < this.MAX_GEN; g++) {
        	
            // El proceso de movimiento de cada hormiga
 
    		
    	//bucle mientras no se encuentre solucion? Creo que no nos haría falta porque siempre se lanzarán el numero de hormigas
        //que indiquemos por parámetros
            for (int i = 0; i < this.antNum; i++) {
            	
                    this.ants[i].selectNextCity(this.pheromone,origen,destino,topologia,i);//Construye solucion
                
                this.ants[i].getTabu().add(this.ants[i].getFirstCity());
//              if(this.ants[i].getTabu().size() < 49) {
//                  System.out.println(this.ants[i].toString());
//              }
                // Calcula la longitud del camino obtenido por la hormiga  
                this.ants[i].setTourLength(this.ants[i].calculateTourLength());  
                if(this.ants[i].getTourLength() < this.bestLength){  
                    // Reserva el camino óptimo  
                    this.bestLength = this.ants[i].getTourLength();  
                   // System.out.println(" "+g+"Generación, descubre nuevas soluciones"+this.bestLength);  
                    //System.out.println("size:"+this.ants[i].getTabu().size());
                    for(int k = 0;k < this.ants[i].getTabu().size();k++)  
                        this.bestTour[k] = this.ants[i].getTabu().get(k).intValue();;//Distancia del camino que ha seguido cada hormiga
                }
                // Actualiza la matriz de cambio de feromonas
                for (int j = 0; j < this.ants[i].getTabu().size()-1; j++) {
                    this.ants[i].getDelta()[this.ants[i].getTabu().get(j).intValue()][this.ants[i].getTabu().get(j+1).intValue()] = (double) (1.0/this.ants[i].getTourLength());
                    this.ants[i].getDelta()[this.ants[i].getTabu().get(j+1).intValue()][this.ants[i].getTabu().get(j).intValue()] = (double) (1.0/this.ants[i].getTourLength());
                }
            }
            
            
            
            // Actualizar feromonas
            this.updatePheromone();// Sistema de hormigas-ciclo
            // Reinicializar la hormiga
            for(int i = 0;i < this.antNum;i++){  
                this.ants[i].init(this.distance, this.alpha, this.beta);
            }
      //  }
        // Imprime el mejor resultado
        this.printOptimal();
        */
    }

    public void printOptimal() {
         System.out.println("The optimal length is: " + this.bestLength);
         System.out.println("The optimal tour is: ");
         for (int i = 0; i < this.bestTour.length; i++) {
             System.out.println(this.bestTour[i]);
         }
    }
    public void buildGraphManually(Graph<Integer, DefaultEdge> graph){
    	//Creo los vertices
    	for (int k = 0; k < this.cityNum; k++) {
    	   //Crear una clase que identifique al vertice		
    		graph.addVertex(k);
			
		}
    	
    	//Creo las aristas
    	for (int i = 0; i < this.distance.length; i++) {
			for (int j = 0; j < this.distance.length; j++) {
				if(this.distance[i][j]!=9999) {
					graph.addEdge(i,j);
					graph.addEdge(j, i);
					graph.setEdgeWeight(i, j, this.distance[i][j]);
					graph.setEdgeWeight(j, i, this.distance[j][i]);//el peso debe ser el mismo
				}
				
			}
		}
    }
    
    
    public void calculateKshortestPath(int origen, int destino) {
 	
        
        Graph<Integer,DefaultEdge> topologia = new SimpleDirectedWeightedGraph<>(DefaultEdge.class);
        
        
        //Construyo el grafo a la hora de tomar la decision con todos los datos actualizados (distancias, cargas de los enlaces...)
		
        buildGraphManually(topologia);//El estado de la topologia será el mismo para la hormiga 1 y la 20
        
        
	    YenKShortestPath<Integer, DefaultEdge> shortestpath= new YenKShortestPath<>(topologia);
        
		List<GraphPath<Integer,DefaultEdge>> rutas = shortestpath.getPaths(origen, destino, 3); //Hay algun enlace sin destino
		
		
		
		//Para cada solucion recorremos los nodos
		
		int i=0;
		while (i<3) {
			GraphPath<Integer,DefaultEdge> graph =  rutas.get(i);//Ruta que corresponde a la hormiga en funcion de su identificador(para que las rutas sean distintas)
			
			List<Integer>vertex =graph.getVertexList();
			System.out.print("La solucion "+i+" recorre los siguientes nodos:");
			for (Integer j : vertex) {
				System.out.print(" j ");
			}
			System.out.println();
			
			
		i++;	
		}
	
    }
    
    public Ant[] getAnts() {
        return ants;
    }

    public void setAnts(Ant[] ants) {
        this.ants = ants;
    }

    public int getAntNum() {
        return antNum;
    }

    public void setAntNum(int antNum) {
        this.antNum = antNum;
    }

    public int getCityNum() {
        return cityNum;
    }

    public void setCityNum(int cityNum) {
        this.cityNum = cityNum;
    }

    public int getMAX_GEN() {
        return MAX_GEN;
    }

    public void setMAX_GEN(int mAX_GEN) {
        MAX_GEN = mAX_GEN;
    }

    public double[][] getPheromone() {
        return pheromone;
    }

    public void setPheromone(double[][] pheromone) {
        this.pheromone = pheromone;
    }

    public int[][] getDistance() {
        return distance;
    }

    public void setDistance(int[][] distance) {
        this.distance = distance;
    }

    public int getBestLength() {
        return bestLength;
    }

    public void setBestLength(int bestLength) {
        this.bestLength = bestLength;
    }

    public int[] getBestTour() {
        return bestTour;
    }

    public void setBestTour(int[] bestTour) {
        this.bestTour = bestTour;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getBeta() {
        return beta;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    public double getRho() {
        return rho;
    }

    public void setRho(double rho) {
        this.rho = rho;
    }

    @Override
    public String toString() {
        return "ACO [ants=" + Arrays.toString(ants) + ", antNum=" + antNum + ", cityNum=" + cityNum + ", MAX_GEN="
                + MAX_GEN + ", pheromone=" + Arrays.toString(pheromone) + ", distance=" + Arrays.toString(distance)
                + ", bestLength=" + bestLength + ", bestTour=" + Arrays.toString(bestTour) + ", alpha=" + alpha
                + ", beta=" + beta + ", rho=" + rho + "]";
    }

}