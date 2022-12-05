import java.util.*;
import java.io.*;

public class bully {
    static int numberOfProcess;
    static int priorities[] = new int[100];
    static int status[] = new int[100];
    static int cord;
    static Scanner sc = new Scanner(System.in);

    // main() method start
    public static void main(String[] args) throws IOException   // handle IOException
    {
        System.out.println("Enter total number of processes:");

        Scanner sc = new Scanner(System.in);
        numberOfProcess = sc.nextInt();

        int i;

        for(i = 0; i<numberOfProcess; i++)
        {
            System.out.println("Status for process "+(i+1)+":");
            status[i] = sc.nextInt();
            System.out.println("Priority of process "+(i+1)+":");
            priorities[i] = sc.nextInt();
        }

        System.out.println("Enter proces which will initiate election");
        int ele = sc.nextInt();

        if(status[ele-1]==0){
            System.out.println("Invalid Start");
        }
        else{
            electProcess(ele);
            System.out.println("After electing process the final coordinator is "+cord);
        }
    }

    // call electProcess() method



    // create electProcess() method
    static void electProcess(int ele)
    {
        ele = ele - 1;
        cord = ele + 1;

        for(int i = 0; i<numberOfProcess; i++)
        {
            if(priorities[ele]<priorities[i] && status[ele] == 1)
            {
                System.out.println("Election message is sent from "+(ele+1)+" to "+(i+1));
                if(status[i]==1)
                    electProcess(i+1);
            }
        }
    }
}
