package Esqueleto2;


import java.io.BufferedReader;
import java.io.BufferedWriter;
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
    private int antNum; // N�mero de hormigas
    private int cityNum; // N�mero de ciudades
    private int MAX_GEN; // Ejecutar �lgebra (n�mero de veces que se ejecuta el algoritmo)
//    private int origen;
//    private int destino;
    private double[][] pheromone; // Matriz de feromonas
    private int[][] distance; // Matriz de distancias (coste)
    private int[][] load; // carga de trafico que tiene un nodo en un momento dado
    private int[][] traffic; 
    private int [][] capacity;
    private double bestTourLoad; // Carga camino mas optimo
    private double bestTourDistance; // Longitud camino mas �ptimo
    private int[] bestTour; // Mejor camino
    // Tres par�metros
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
    public ACO(int [][] traffic,int[][]adjacency ,int[][]capacity,int antNum, int cityNum, int MAX_GEN, double alpha, double beta, double rho) {
        this.traffic=traffic;
        this.adjacency =adjacency;
        this.capacity=capacity;

    	this.antNum = antNum;
        this.cityNum = cityNum;
        this.MAX_GEN = MAX_GEN;
        this.alpha = alpha;
        this.beta = beta;
        this.rho = rho;
        this.ants = new Ant[this.antNum];
    }

    /*
     * M�todo para inicializar las variable. Lee un fichero con las ciudades a partir del cual calcula la matriz de distancias.
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
             * Calcular la matriz de distancias. Para problemas espec�ficos, el m�todo de c�lculo de distancias es diferente. Aqu� se utiliza att48 como caso.
             * Tiene 48 ciudades, el m�todo de c�lculo de distancia es la distancia pseudoeuclidiana y el valor �ptimo es 10628
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
            
            //---------------------------------------------------------PROVISIONAL------------------------
           //Para hacer las pruebas mas faciles voy hacer que de momento la matriz de distancias sea la de adyacentes
           // this.distance=this.adjacency;
            
            //---------------------------------------------------------------------------------------------
            
            
            //Las aristas que no est�n conectadas las ponemos a 9999
            finalizeCostMatrix();
            //Completamos la matriz de carga con el porcentaje de trafico y capacidad de cada enlace
            //completarMatrizCarga();
            
     
            
            // Inicializar la matriz de feromonas
            this.pheromone=new double[this.cityNum][this.cityNum];
            for(int i = 0;i < this.cityNum;i++) {
                for(int j = 0;j < this.cityNum;j++) {
                    this.pheromone[i][j] = 0.1d; //valor por defecto de las feromonas
                }
            }
            // Inicializa la longitud de la ruta �ptima
            this.bestTourLoad=Integer.MAX_VALUE;
            // Inicializar la ruta �ptima
            this.bestTour=new int[this.cityNum];  
            // Coloca las hormigas al azar  
            for(int i = 0;i < this.antNum;i++){  //Inicializamos el vector de hormigas= creamos las hormigas
                this.ants[i]=new Ant(this.cityNum);  
                this.ants[i].init(this.distance,this.load, this.alpha, this.beta);  
            }  
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private void completarMatrizCarga() {
//    	for (int i = 0; i < this.adjacency.length; i++) {
//			for (int j = 0; j < this.adjacency.length; j++) {
//				if (this.adjacency[i][j]==1) {//No tenemos arista entre estos dos nodos
//					this.load[i][j]=(this.traffic[i][j]*100)/this.capacity[i][j];
//				}
//			}
//		}
//		
//	}

	private void finalizeCostMatrix() {
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
        // Volatilizaci�n de feromonas  
        for(int i = 0;i < this.cityNum;i++)  
            for(int j = 0;j < this.cityNum;j++)  
                this.pheromone[i][j] = this.pheromone[i][j] * (1 - this.rho);//RHO es el valor de volatilizacion de las feromonas 
        // Actualizaci�n de feromonas (alta complejidad)
        for(int i = 0;i < this.cityNum;i++) {
            for(int j = 0;j < this.cityNum;j++) {
                for(int k = 0;k < this.antNum;k++) {
                    this.pheromone[i][j] += this.ants[k].getDelta()[i][j];//Utilizamos una formula para actualizar feromonas que probablemente haya que cambiar
                }
            }
        }
    }

    /*
     * M�todo que implementa el movimiento de las hormigas
     */
    public void solve() throws IOException {
        
    	BufferedWriter  fEnrutamiento = new BufferedWriter (new FileWriter("enrutamiento.txt"));
    	BufferedWriter  fCarga = new BufferedWriter (new FileWriter("carga.txt"));
		
         int it=0;
		for (int f = 0; f < this.traffic.length; f++) {// Recorremos la matriz de trafico
			
			for (int c = 0; c < this.traffic.length; c++) {
				System.out.println("Iteracion: "+it+" | Origen(Fila): "+f+" | Destino (Columna): "+c);
				
				// Inicializa la longitud de la ruta �ptima
	            this.bestTourLoad=Integer.MAX_VALUE;
	            // Inicializar la ruta �ptima
	            this.bestTour=new int[this.cityNum];  
				completeFirstCity(f);
				
			   fEnrutamiento = new BufferedWriter (new FileWriter("enrutamiento.txt",true));
				if (f!=c) {//De este modo garantizamos las n*n-1 iteraciones (ahorramos la diagonal a 0)
					boolean callejonsinsalida=false;
					
					this.buildBestSolution(f,c,callejonsinsalida);
					

					// Cuando tenemos el mejor camino, entonces actualizamos matriz de carga
					if(!callejonsinsalida) {
					this.printOptimal(c);	
					this.updateLoadMatrix(f,c,fEnrutamiento);
					}
					fEnrutamiento.close();
					it++;
			}
			}
		} // Cierran la matriz de trafico

		//escribe en un fichero la carga de cada enlace
		this.writeLoadFile(fCarga);
		
         fCarga.close();
    }

   

	private void buildBestSolution(int f, int c ,boolean callejonsinsalida) {
    	
		for (int g = 0; g < this.MAX_GEN; g++) {

			for (int i = 0; i < this.antNum; i++) {

				callejonsinsalida = false;
				while (this.ants[i].getCurrentCity() != c) {
					// System.out.println("Construyendo solucion");

					if (this.ants[i].selectNextCity(this.pheromone) == 1) {
						callejonsinsalida = true;
						break;
					} // Construye solucion

				}
				if (!callejonsinsalida) {
					this.ants[i].getTabu().add(this.ants[i].getFirstCity());

//  if(this.ants[i].getTabu().size() < 49) {
//      System.out.println(this.ants[i].toString());
//  }
					// Calcula la longitud del camino obtenido por la hormiga
					this.ants[i].setTourLoad(this.ants[i].calculateTourLoad());
					this.ants[i].setTourDistance(this.ants[i].calculateTourDistance());

					System.out.print("Generacion " + g + " |  Hormiga: " + i + " | Coste: "
							+ this.ants[i].getTourLoad()+" |");
					 this.ants[i].showTabu();
					System.out.println();

					if (this.ants[i].getTourLoad() == this.bestTourLoad) {// En caso de empate es la
																			// distancia la que resuelve

						if (this.ants[i].calculateTourDistance() < this.bestTourDistance) {
							assignBestTour(g, this.ants[i]);
						}

					}

					if (this.ants[i].getTourLoad() < this.bestTourLoad) {

						assignBestTour(g, this.ants[i]);
					}
					// Actualiza la matriz de cambio de feromonas
					for (int j = 0; j < this.ants[i].getTabu().size() - 1; j++) {
						this.ants[i].getDelta()[this.ants[i].getTabu().get(j).intValue()][this.ants[i]
								.getTabu().get(j + 1)
								.intValue()] = (double) (1.0 / this.ants[i].getTourLoad());// 1.0 es el
																							// total de
																							// feromonas
						this.ants[i].getDelta()[this.ants[i].getTabu().get(j + 1).intValue()][this.ants[i]
								.getTabu().get(j).intValue()] = (double) (1.0 / this.ants[i].getTourLoad());
					}
					
				}
			}

			// Actualizar feromonas
			this.updatePheromone();// Sistema de hormigas-ciclo
			// Reinicializar la hormiga
			for (int i = 0; i < this.antNum; i++) {
				this.ants[i].reInit(this.distance, this.load, this.alpha, this.beta,f);
			}

		}
		
	}

	private void completeFirstCity(int origen) {
    	int aux=0;
    	for(int i = 0;i < this.antNum;i++){  //Inicializamos el vector de hormigas= creamos las hormigas
      
    		this.ants[i].setFirstCity(origen);
    		aux=this.ants[i].getAllowedCities().indexOf(origen);
    		if (aux!=-1) { //encuentra el elemento a borrar
    			this.ants[i].getAllowedCities().remove(aux); //El origen viene predefinido
                this.ants[i].getTabu().add(this.ants[i].getFirstCity());
        		
                this.ants[i].setCurrentCity(this.ants[i].getFirstCity()); 	
    		}
//    		else {
//    			System.out.println("El origen ya esta fuera de la lista");
//    		}
    			
            
        } 
    	
		
	}

	private void updateLoadMatrix(int origen,int dst, BufferedWriter  fEscritura) throws IOException {
    	int acu = 0;
		fEscritura.write("Origen: "+origen+" Destino: "+dst+": ");
		fEscritura.write(this.bestTour[0]+" -> ");
		acu = traffic[origen][dst];
		
		for (int i = 1; i < this.bestTour.length; i++) {
		
			    fEscritura.write(Integer.toString(this.bestTour[i]));
			   
				
				this.load[this.bestTour[i-1]][this.bestTour[i]] = this.load[this.bestTour[i-1]][this.bestTour[i]] + acu;
				this.load[this.bestTour[i]][this.bestTour[i-1]] = this.load[this.bestTour[i-1]][this.bestTour[i]];
			
				if(bestTour[i]==dst) {
					break;
				}
				fEscritura.write(" -> ");
			
			
				
				
				
		}
		//System.out.println("Carga acumulada: "+load[origen][dst]);
		fEscritura.write("\n");
		
		pintarMatriz(load);
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
    
    private void writeLoadFile(BufferedWriter  fCarga) throws IOException {
    	for (int x=0; x < this.load.length; x++) {
			  for (int y=0; y < this.load.length; y++) {
				  fCarga.write("Origen: "+x+" Destino: "+y+" | Carga: "+this.load[x][y]+"\n");
			  }
			}
		
	}

	private void printOptimal(int destino) {
    	System.out.println("|-----------------------------|");
         System.out.println("| The optimal length is: " + this.bestTourLoad+" |");
         System.out.println("|-----------------------------|");
         System.out.print("| The optimal tour is: ");
         for (int i = 0; i < this.bestTour.length; i++) {
             System.out.print(this.bestTour[i]);
             if (this.bestTour[i] == destino) {
            	 System.out.println(" |");
					break;
				}
             else {
            	 System.out.print(" -> ");
             }
         }
         System.out.println("|-----------------------------|");
    }
    private void buildGraphManually(Graph<Integer, DefaultEdge> graph){
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
    private void assignBestTour(int g,Ant ant) {
    	// Reserva el camino �ptimo
		this.bestTourLoad = ant.getTourLoad();
		this.bestTourDistance=ant.getTourDistance();
		 System.out.println("Generaci�n "+g+" , descubre un camino mejor con coste: "+this.bestTourLoad);
		
		for (int k = 0; k < ant.getTabu().size()-1; k++) {//Por alguna razon siempre tiene una casilla vac�a m�s
			
			this.bestTour[k] = ant.getTabu().get(k).intValue();// Mejor camino hasta el momento
		}
    }
    
    public void calculateKshortestPath(int origen, int destino) {
 	
        
        Graph<Integer,DefaultEdge> topologia = new SimpleDirectedWeightedGraph<>(DefaultEdge.class);
        
        
        //Construyo el grafo a la hora de tomar la decision con todos los datos actualizados (distancias, cargas de los enlaces...)
		
        buildGraphManually(topologia);//El estado de la topologia ser� el mismo para la hormiga 1 y la 20
        
        
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

    public double getBestLength() {
        return bestTourLoad;
    }

    public void setBestLength(int bestLength) {
        this.bestTourLoad = bestLength;
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
                + ", bestLength=" + bestTourLoad + ", bestTour=" + Arrays.toString(bestTour) + ", alpha=" + alpha
                + ", beta=" + beta + ", rho=" + rho + "]";
    }

}