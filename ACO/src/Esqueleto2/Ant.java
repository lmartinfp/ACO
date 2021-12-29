package Esqueleto2;
import java.util.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Supplier;
import java.lang.Object;
import org.jgrapht.*;
import org.jgrapht.alg.shortestpath.YenKShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.builder.GraphBuilder;
import org.jgrapht.io.*;




/**
 * Hormigas
 * Cloneable es una clase clonada, usando el método clone () de Object
 *
 */
public class Ant implements Cloneable{

 

	/**
     * Vector, como ArrayList, también se implementa a través de arreglos, la diferencia es que admite la sincronización de subprocesos,
     * Es decir, solo un hilo puede escribir Vector a la vez para evitar la inconsistencia causada por varios hilos que escriben al mismo tiempo.
     * Pero lograr la sincronización requiere un alto costo, por lo que acceder a ella es más lento que acceder a ArrayList.
     */
    private ArrayList<Integer> allowedCities;// Ciudades permitidas

    private ArrayList<Integer> tabu;// Lista tabú, registra las ciudades visitadas

    private int [][] distance;// Matriz de distancias que va a ser para nosotros el coste
    
    private int [][] carga;// Matriz de distancias

    private double[][] delta; // Matriz de cambio de feromonas

    private double alpha;

    private double beta;
    
    private int umbral; //numero de hormigas maximo que soporta un enlace actua como la carga del enlace

    private int cityNum;// Número de ciudades

    private int tourLength;// La longitud del camino

    private int firstCity; // Ciudad de inicio

    private int currentCity; //Ciudad actual

    public Ant(int cityNum) {
        this.cityNum = cityNum;
        this.tourLength = 0;
    }

    public Ant() {
        this.cityNum = 30;
        this.tourLength = 0;
    }

    /**
     * Inicializa las hormigas y selecciona al azar la primera ciudad para las hormigas
     * @param distance
     * @param a
     * @param b
     */
    public void init(int[][] distance,double a,double b) {
        this.alpha = a;
        this.beta = b;
        this.distance = distance;
        this.allowedCities = new ArrayList<Integer>();
        this.tabu = new ArrayList<Integer>();
        this.delta = new double[cityNum][cityNum];
        for(int i = 0;i < this.cityNum;i++) {
            Integer city = new Integer(i);
            this.allowedCities.add(city);
            for (int j = 0;j < this.cityNum;j++) {
                this.delta[i][j] = 0.d;
            }
        }

        Random random = new Random(System.currentTimeMillis());
        this.firstCity = random.nextInt(this.cityNum);//La primera ciudad se elige de forma aleatoria

        for (Integer city:this.allowedCities) {
            if(city.intValue() == this.firstCity) {
                this.allowedCities.remove(city);
                this.tabu.add(city);
                break;
            }
        }

        this.currentCity = this.firstCity;
    }

    /**
     * Elige la siguiente ciudad
     * @param <E>
     * @param  matriz de feromonas
     * 
     * Aplicar k shortestpath me devuelve los caminos y probar uno a uno para ver cual interesa  
     * 
     * 
     */
    public void selectNextCity(double[][] pheromone,int origen,int destino,Graph<Integer,DefaultEdge> topologia,int idhormiga) {
        double[] p = new double[this.cityNum];// Probabilidad de transición
        double sum = 0.d;// Denominador de probabilidad de transición

        
        
		YenKShortestPath shortestpath= new YenKShortestPath<>(topologia);
        
		List<GraphPath<Integer,DefaultEdge>> rutas = shortestpath.getPaths(origen, destino, idhormiga);
		
		GraphPath<Integer,DefaultEdge> graph =  rutas.get(idhormiga);//Ruta que corresponde a la hormiga en funcion de su identificador(para que las rutas sean distintas)
		
		List<Integer>vertex =graph.getVertexList();
	
        if (this.allowedCities.contains(vertex.get(this.currentCity+1))) {// Permite ir de todas a todas las ciudades
       
        	for (Integer city: this.allowedCities) {  
        		if(distance[this.currentCity][city]!=9999) {//Calculo del sumatorio para ciudades adyacentes(modificacion)
             sum += Math.pow(pheromone[this.currentCity][city.intValue()],
                    this.alpha)*Math.pow(1.d/this.distance[this.currentCity][city.intValue()], this.beta)*Math.pow(1.d/this.carga[this.currentCity][city.intValue()], this.beta);
        		}
        		}
        	
        	
        	
         //Cambiar por formula de wuham formula 1 y quitamos los retardos. Me quedo con el siguiente nodo de k shortest path
        //Tendremos que cargar de feromonas el camino con menor MLU (menor saturacion de carga)
        
        }
	
        
//      double s = 0.d;
        for (int i = 0; i < this.cityNum; i++) {
            boolean flag = false;
            for (Integer city : this.allowedCities) {
                if(i == city.intValue() && distance[this.currentCity][city]!=9999) {// comprobar que es adyacente de la ciudad acutal(modificacion)
                     p[i] = (double) ((Math.pow(pheromone[this.currentCity][i], 
                             this.alpha)*Math.pow(1.d/this.distance[this.currentCity][i], this.beta)*Math.pow(1.d/this.carga[this.currentCity][i], this.beta))/sum);
                     flag = true;
                     break;
                }
            }
            if(!flag)
                p[i] = 0.d;//Las probabilidades que no se rellenan se ponen a 0
//          s += p[i];
        }
//      if(Double.isNaN(s)) {
//          for(int i =0;i < this.cityNum;i++) {
//              System.out.println(p[i]);
//          }
//      }

        /**
          * Si la ciudad con la probabilidad más alta se selecciona directamente como la próxima ciudad cada vez, el algoritmo convergerá prematuramente.
          * Al final de la búsqueda, solo puede obtener soluciones subóptimas, y el uso de la ruleta puede mejorar las capacidades de búsqueda global del algoritmo sin perder la búsqueda local.
          * Así que aquí elige la ruleta para elegir la siguiente ciudad. Consulte "Inteligencia computacional" Tsinghua University Press
         */
        // La ruleta elige la siguiente ciudad
         double sumSelect = 0.d;
         int selectCity = -1;
         Random random = new Random(System.currentTimeMillis());
         double selectP = random.nextDouble();
         while(selectP == 0.f) {
             selectP = random.nextDouble();
         }
         for(int i = 0;i < this.cityNum;i++) {//TODO solo podria seleccionar las adyacentes
             sumSelect += p[i];
             if(sumSelect >= selectP) { //La primera ciudad cuya probabilidad de selección supere al random
                 selectCity = i;
                 
                 //-----EN EL MOMENTO DE SELECCIONAR LA CIUDAD A LA QUE MOVERSE LLENAMOS LA MATRIZ DE TRAFICO? SERIA ESTE UN CASO REAL?---
                 
                 
                 //this.distance[this.currentCity][selectCity]++;//?? El estado de la topologia será el mismo para la hormiga 1 y la 20
                 
                // Elimina la ciudad seleccionada de las ciudades que permiten la selección
                 this.allowedCities.remove(Integer.valueOf(selectCity));
                // Agrega una ciudad seleccionada a la tabla tabú
                 this.tabu.add(Integer.valueOf(selectCity));
                // Cambia la ciudad actual a la ciudad seleccionada
                 this.currentCity = selectCity;
                 break;
             }
         }
    }
    

    

    /**
     * Calcular la longitud del camino
     * @return
     */
    public int calculateTourLength() {
        int length = 0;
//      if(this.tabu.size() == 1) {
//          return 0;
//      }
        for(int i = 0;i < this.tabu.size()-1;i++) {
            length += this.distance[this.tabu.get(i).intValue()][this.tabu.get(i+1).intValue()];
        }
        return length;
    }

    public ArrayList<Integer> getAllowedCities() {
        return allowedCities;
    }

    public void setAllowedCities(ArrayList<Integer> allowedCities) {
        this.allowedCities = allowedCities;
    }

    public ArrayList<Integer> getTabu() {
        return tabu;
    }

    public void setTabu(ArrayList<Integer> tabu) {
        this.tabu = tabu;
    }

    public int[][] getDistance() {
        return distance;
    }

    public void setDistance(int[][] distance) {
        this.distance = distance;
    }

    public double[][] getDelta() {
        return delta;
    }

    public void setDelta(double[][] delta) {
        this.delta = delta;
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

    public int getCityNum() {
        return cityNum;
    }

    public void setCityNum(int cityNum) {
        this.cityNum = cityNum;
    }

    public int getTourLength() {
        return tourLength;
    }

    public void setTourLength(int tourLength) {
        this.tourLength = tourLength;
    }

    public int getFirstCity() {
        return firstCity;
    }

    public void setFirstCity(int firstCity) {
        this.firstCity = firstCity;
    }

    public int getCurrentCity() {
        return currentCity;
    }

    public void setCurrentCity(int currentCity) {
        this.currentCity = currentCity;
    }

    @Override
    public String toString() {
        return "Ant [allowedCities=" + allowedCities + ", tabu=" + tabu + ", distance=" + Arrays.toString(distance)
                + ", delta=" + Arrays.toString(delta) + ", alpha=" + alpha + ", beta=" + beta + ", cityNum=" + cityNum
                + ", tourLength=" + tourLength + ", firstCity=" + firstCity + ", currentCity=" + currentCity + "]";
    }

}