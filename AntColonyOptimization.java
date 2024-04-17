//Athavan Jesunesan
//COSC 3P71 - Assignment 3

import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.File;
import java.lang.Math;

public class AntColonyOptimization{
    int n = 0;
    int ultimate = Integer.MAX_VALUE;
    int[] bestPath = new int[n];

    public AntColonyOptimization(){
        
        /* Parameter Settings */
        int numAnts = 50;
        double[] alpha = {0.5, 0, 1, 0.20};
        double[] beta = {0.5, 1, 0, 0.80};
        double p = 0.4;
        int numGen = 5;     //Number of Generations

        /* Algorithm */

        int[][][] ants = new int[numGen][numAnts][n]; //[Generations][number of ants/generation][number of cities]
        int[][] heuristicMatrix = CreateHeuristicMatrix().clone();
        System.out.println("There are "+numAnts+" ants in each generation with a pheromone evaporation rate of "+p);
        System.out.println();
        for(int i = 0; i<4; i++){//All 4 combinations
            System.out.println("Alpha: "+ alpha[i] + " Beta: " + beta[i]);
            double[][] pheromoneMatrix = CreatePheromoneMatrix().clone();
            double[][] probabilityMatrix = CreateProbabilityMatrix(pheromoneMatrix, heuristicMatrix, alpha[i], beta[i]).clone();
            for(int j = 0; j<numGen; j++){// Each Generation
                for(int x = 0; x<numAnts; x++){//Each ant
                    ants[j][x] = ConstructAntSolution(probabilityMatrix).clone();
                }
                pheromoneMatrix = UpdatePheromones((1-p), pheromoneMatrix, ants[j]).clone();
                probabilityMatrix = CreateProbabilityMatrix(pheromoneMatrix, heuristicMatrix, alpha[i], beta[i]).clone();
                System.out.println("Generation "+(j+1));
                System.out.println("Average: "+ AverageTourLength(ants[j], heuristicMatrix));
                System.out.println("Best: "+ BestTourLength(ants[j], heuristicMatrix));
                System.out.println();
            }
            System.out.println("Overall Best: "+ultimate);
            for(int j = 0; j<n; j++){
                System.out.print(bestPath[j]+" ");
            }
            System.out.println();
            System.out.println("----------------------------------------------------------------------------------------------------------------------");
            System.out.println();
            ultimate = Integer.MAX_VALUE;
        }

    }//Constructor

    public int[][] GetData(){
        try{
            File file = new File("data.txt");
            Scanner scanner = new Scanner(file);
            n = Integer.parseInt(scanner.nextLine()); //The number of cities in the TSP
            int[][] data = new int[n][2];//Matrix to store all values
            int temp = 0;
            while(scanner.hasNextLine()){ //sets the x and y values
                String[] line = scanner.nextLine().split(" ");
                temp = Integer.parseInt(line[0]) - 1; //Reduce the city number by one to fit into the array
                data[temp][0] = Integer.parseInt(line[1]);
                data[temp][1] = Integer.parseInt(line[2]);
            }
            return data;
        } catch (FileNotFoundException exception){
            System.out.println("The file was not found.");
            exception.printStackTrace();
        }
        return null; //The case where the file was not found
    }

    public int[][] CreateHeuristicMatrix(){
        int[][] data = GetData();
        int[][] heuristicMatrix = new int[n][n];
        int x1, y1;
        int x2, y2;
        double d = 0; //Euclidean Distance

        for(int i = 0; i<n; i++){
            x1 = data[i][0];
            y1 = data[i][1];
            for(int j = i; j<n; j++){
                if(j==i){
                    heuristicMatrix[i][j] = 0;
                } else {
                    x2 = data[j][0];
                    y2 = data[j][1];
                    d = Math.sqrt((Math.pow(x2-x1, 2))+(Math.pow(y2-y1, 2)));
                    heuristicMatrix[i][j] = (int)Math.round(d);
                    heuristicMatrix[j][i] = (int)Math.round(d);
                }
            }
        }
        return heuristicMatrix;
    }

    public double[][] CreatePheromoneMatrix(){
        double[][] pheromoneMatrix = new double[n][n];
        double rand = 0;

        for(int i = 0; i<n; i++){
            for(int j = i; j<n; j++){
                if(j==i){
                    pheromoneMatrix[i][j] = 0;
                } else {
                    rand = Math.random()*(1);
                    pheromoneMatrix[i][j] = rand;
                    pheromoneMatrix[j][i] = rand;
                }
            }
        }
        return pheromoneMatrix;
    }

    public double[][] CreateProbabilityMatrix(double[][] pheromoneMatrix, int[][] heuristicMatrix, double alpha, double beta){
        double[][] probabilityMatrix = new double[n][n];
        double value = 0;

        for(int i = 0; i<n; i++){
            for(int j = i; j<n; j++){
                if(j==i){
                    probabilityMatrix[i][j] = 0;
                } else {
                    value = (Math.pow(pheromoneMatrix[i][j], alpha))*(Math.pow((heuristicMatrix[i][j]), -beta));
                    probabilityMatrix[i][j] = value;
                    probabilityMatrix[j][i] = value;
                }
            }
        }
        return probabilityMatrix;
    }

    public int[] ConstructAntSolution(double[][] probabilityMatrix){
        int[] ant = new int[n];
        ant[0] = (int)(Math.random()*(n-1))+1; //Choosing the first city randomly
        int counter = 1;//keeps track of how many cities have been added to ant's path
        double sum = 0; 
        double rand = 0;
        int pointer = 0;
        double[] row = new double[n];
        for(int i = 0; i<n-1; i++){
            row = probabilityMatrix[ant[i]].clone();
            for(int j = 0; j<counter; j++){
                row[ant[j]] = -1; //Symbolizes invalid
            }
            for(int j = 0; j<n; j++){//get the sum_prob
                if(row[j]!=-1){ //This will only execute n-counter times
                    sum += row[j];
                }
            }
            for(int j = 0; j<n; j++){//Divide all by sum_prob
                if(row[j]!=-1){
                    row[j] = row[j]/sum;
                }
            }
            sum = 0;//now acting as cumulative sum
            rand = Math.random()*(1);

            if(rand==0)pointer++;
            while(rand>sum){
                if(row[pointer] != -1){
                    sum += row[pointer];
                }
                pointer++;
            } 
            ant[i+1] = pointer-1;

            /* Adjust variables for next iteration */
            pointer=0;
            sum = 0; //will act as sum_prob again for the first half
            counter++;
        }
        return ant;
    }

    public double[][] GetEdges(int[][] ants){
        double[][] edgeMatrix = new double[n][n];
        int a,b;
        for(int i = 0; i<ants.length; i++){
            for(int j = 1; j<n; j++){
                a = ants[i][j-1];
                b = ants[i][j];
                edgeMatrix[a][b]++;
                edgeMatrix[b][a]++;
            }
        }
        for(int i = 0; i<n; i++){
            for(int j = i; j<n; j++){
                edgeMatrix[i][j] /= n;
                edgeMatrix[j][i] /= n;
            }
        }
        return edgeMatrix;
    }


    public double[][] UpdatePheromones(double p, double[][] pheromoneMatrix, int[][] ants){
        double[][] edgeMatrix = GetEdges(ants).clone();
        double value = 0;        

        for(int i = 0; i<n; i++){
            for(int j = i; j<n; j++){
                value = (pheromoneMatrix[i][j]*p) + edgeMatrix[i][j];
                pheromoneMatrix[i][j] = value;
                pheromoneMatrix[j][i] = value;
            }
        }
        return pheromoneMatrix;
    }

    public int BestTourLength(int[][] ants, int[][] heuristicMatrix){//Finds best tour length of a generation
        int[] list = new int[ants.length];
        int travel = 0;
        int a,b;
        int best = Integer.MAX_VALUE;
        
        for(int i = 0; i<ants.length; i++){//Places all into a list
            for(int j = 1; j<n; j++){
                a = ants[i][j-1];
                b = ants[i][j];
                travel += heuristicMatrix[a][b];
            }
            if(travel<ultimate){
                ultimate = travel;
                bestPath = ants[i].clone();
            }
            list[i] = travel;
            travel = 0;
        }

        for(int i = 0; i<ants.length; i++){ //finds the best
            if(best>list[i]){
                best=list[i];
            }
        }
        return best;
    }

    public double AverageTourLength(int[][] ants, int[][] heuristicMatrix){//Returns the Average tour length of a generation
        int travel = 0;
        int a,b;

        for(int i = 0; i<ants.length; i++){
            for(int j = 1; j<n; j++){
                a = ants[i][j-1];
                b = ants[i][j];
                travel += heuristicMatrix[a][b];
            }
        }
        return travel/ants.length;
    }


    public static void main(String[] args) {
        AntColonyOptimization a = new AntColonyOptimization();
    }
}