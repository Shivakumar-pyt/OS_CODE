import java.util.*;
import java.io.*;


class Producer extends Thread{
    public ArrayList<String> resource = new ArrayList<>();
    public int size;
    public String res;
    public Scanner sc = new Scanner(System.in);
    Producer(int s) {
        this.size = s;
    }

    public void run() {
        System.out.println("Starting Production...");
        for(int i=1;i<=this.size;i++) {
            System.out.print("Enter resource name: ");
            this.res = sc.next();
            this.resource.add(this.res);
            System.out.print("\n");
        }
        System.out.println("Production Over...");
    }

}

class Consumer extends Thread{
    ArrayList<String> res = new ArrayList<>();

    Consumer(ArrayList<String> resource) {
        this.res = resource;
    }

    public void run() {
        System.out.println("Starting Consumption...");
        String rs = res.get(0);
        res.remove(0);
        System.out.println("Consumption Over");
        System.out.println("----------Consumed Resources----------");
        System.out.println("\t" + rs);
    }
}

class Mutex{
    int consumers, size, c = 0;
    ArrayList<String> shared_resource = new ArrayList<>();
    ArrayList<Consumer> critical_section = new ArrayList<>();
    Scanner sc = new Scanner(System.in);
    Mutex(int consumers, int s) {
        this.consumers = consumers;
        this.size = s;
    }

    ArrayList<String> produce() throws InterruptedException {
        Producer prod = new Producer(this.size);
        prod.start();
        prod.join();
        System.out.print("\n");
        return prod.resource;
    }

    public void execute() throws InterruptedException {
        Producer p = new Producer(this.size);
        p.start();
        p.join();

        this.shared_resource = p.resource;

        for(int i=0;i<this.consumers;i++) {
            Consumer c = new Consumer(this.shared_resource);
            this.critical_section.add(c);
        }

        while(consumers > 0) {
            if(this.shared_resource.size() == 0) {
                char choice;
                System.out.print("Restart Production? (y / n): ");
                choice = sc.next().charAt(0);
                if(choice == 'y') {
                    this.shared_resource = produce();
                }
            }

            this.critical_section.get(0).res = this.shared_resource;
            this.critical_section.get(0).start();
            this.critical_section.get(0).join();
            this.shared_resource = this.critical_section.get(0).res;
            c += 1;
            this.consumers -= 1;
            System.out.println("Consumer " + c + " over...");
            this.critical_section.remove(0);
        }
    }
}


class Semaphore{
    int s, consumers, size;
    ArrayList<String> shared_resource = new ArrayList<>();
    ArrayList<Consumer> critical_section = new ArrayList<>();
    ArrayList<Consumer> suspended_list = new ArrayList<>();
    Scanner sc = new Scanner(System.in);
    int c = 1;

    Semaphore(int semaphore, int consumers, int size) {
        this.s = semaphore;
        this.consumers = consumers;
        this.size = size;
    }

    boolean waitState() {
        if(this.s > 0) {
            this.s -= 1;
            return true;
        }

        return false;
    }

    void signalState() {
        if(this.s < this.size) {
            this.s += 1;
        }
    }

    ArrayList<String> produce() throws InterruptedException {
        Producer prod = new Producer(this.size);
        prod.start();
        prod.join();
        System.out.println("\n");
        return prod.resource;
    }

    public void execute() throws InterruptedException {
        Producer p = new Producer(this.size);
        p.start();
        p.join();
        this.shared_resource = p.resource;
        for(int i=0;i<this.consumers;i++) {
            Consumer c = new Consumer(this.shared_resource);
            if(waitState()) {
                this.critical_section.add(c);
            }
            else {
                this.suspended_list.add(c);
            }
        }

        if(this.consumers <= this.s) {
            for(int i=0;i<this.critical_section.size();i++) {
                this.critical_section.get(i).start();
                this.critical_section.get(i).join();
                System.out.println("Consumer " + i + 1 + " over...");
            }
        }
        else {
            while(this.consumers > 0) {
                if(this.shared_resource.size() == 0) {
                    char choice;
                    System.out.println("Restart Production? (y / n): ");
                    choice = sc.next().charAt(0);
                    if(choice == 'y') {
                        this.shared_resource = produce();
                    }
                    else {
                        break;
                    }
                }

               critical_section.get(0).res = this.shared_resource;
               critical_section.get(0).start();
               critical_section.get(0).join();
               System.out.println("Consumer " + c + " over...");
               this.shared_resource = critical_section.get(0).res;
               this.consumers -= 1;
               this.critical_section.remove(0);
               System.out.println("Critical Size: " + this.critical_section.size());
               signalState();
               c += 1;
               if(waitState()) {
                   System.out.println("Suspended Size: " + this.suspended_list.size());
                   if(this.suspended_list.size() > 0) {
                       this.critical_section.add(this.suspended_list.get(0));
                       this.suspended_list.remove(0);
                   }
               }
            }
        }

    }

}


public class mutex_semaphore {
    public static void main(String[] args) throws InterruptedException {
        Scanner sc = new Scanner(System.in);
        char choice, ch;
        int consumers, size, semaphore;
        System.out.println("-------------PROCESS SYNCHRONIZATION--------");
        System.out.print("Mutex (0) or Semaphore (1)? (0/1): ");
        choice = sc.next().charAt(0);
        System.out.print("\n\n");
        do{
            switch(choice) {
                case '0':
                    System.out.print("Enter no. of consumers: ");
                    consumers = sc.nextInt();
                    System.out.print("\n");
                    System.out.print("Enter size of shared resource: ");
                    size = sc.nextInt();
                    System.out.print("\n\n");
                    Mutex object = new Mutex(consumers,size);
                    object.execute();
                    break;

                case '1':
                    System.out.print("\nEnter no. of consumers: ");
                    consumers = sc.nextInt();
                    System.out.print("\n");
                    System.out.print("Enter semaphore: ");
                    semaphore = sc.nextInt();
                    System.out.print("\n");
                    System.out.print("Enter size of shared resource: ");
                    size = sc.nextInt();
                    Semaphore obj = new Semaphore(semaphore, consumers, size);
                    obj.execute();
                    break;
            }

            System.out.print("\nAgain? (y / n): ");
            ch = sc.next().charAt(0);
            if(ch == 'y') {
                System.out.print("\nMutex (0) or Semaphore (1)? (0/1): ");
                choice = sc.next().charAt(0);
            }
        }while(ch != 'n');
    }
}
