import java.util.*;
import java.io.*;

class Process {
    int priority, status;
    String process_name;
    Process(String name, int p, int s) {
        this.priority = p;
        this.status = s;
        this.process_name = name;
    }

}

class Ring{

    int processes = 0, priority, status, initiator_index = 0;
    String initiator, name;
    Process coordinator = new Process("coord", -1, 1);
    Scanner sc = new Scanner(System.in);
    ArrayList<Process> ring = new ArrayList<>();
    HashMap<Process, Integer> activeList = new HashMap<>();

    Ring(int p) {
        this.processes = p;
    }

    ArrayList<Process> rotate(ArrayList<Process> array, int index) {
        ArrayList<Process> arr = new ArrayList<>();

        for(int i=0;i<index;i++) {
            arr.add(array.get(i));
        }

        for(int i=0;i<index;i++) {
            array.remove(0);
        }

        array.addAll(arr);

        return array;
    }


    public void formRing() {

        for(int i=1;i<=this.processes;i++) {
            System.out.print("Enter name of process " + i + ": ");
            this.name = sc.next();
            System.out.print("\n");
            System.out.println("Enter priority of " + this.name +": ");
            this.priority = sc.nextInt();
            System.out.print("\n");
            System.out.print("Enter status of " + this.name +" (0 / 1): ");
            this.status = sc.nextInt();
            Process p = new Process(this.name, this.priority, this.status);
            ring.add(p);
            System.out.print("\nProcess Added...\n");
        }

        System.out.println("Enter initiator: ");
        this.initiator = sc.next();

        for(int i=0;i<this.ring.size();i++) {
            if(this.ring.get(i).process_name.equals(this.initiator)) {
                this.initiator_index = i;
                break;
            }
        }

        this.ring = rotate(this.ring, this.initiator_index);

    }

    public void execute() {

        formRing();
        int c = 0;

        Process init = this.ring.get(0);

        Process last_process = this.ring.get(this.processes - 1);

        if(init.status == 0) {
            System.out.println("Invalid case - (Initiator non functional)...");
        }
        else {
            this.activeList.put(init, init.priority);
            boolean flag = false;
            while(true) {
                Process current_process = this.ring.get(c);
                if(flag) {
                    this.activeList.put(current_process, current_process.priority);
                }

                if (c == this.processes - 1) {
                    break;
                }

                for(int i=c+1;i<this.ring.size();i++) {
                    Process next_process = this.ring.get(i);
                    if(next_process.status == 1) {
                        flag = true;
                        c = i;
                        last_process = next_process;
                        System.out.print("Process " + current_process.process_name + " is sending message to " + next_process.process_name + "...\n");
                        break;
                    }
                    else {
                        c += 1;
                        flag = false;
                    }
                }
            }
        }

        if(c == this.processes-1) {
            System.out.print("Process " + last_process.process_name + " is sending message to " + this.initiator + "...\n");

            this.activeList.forEach((k, v) -> {
                System.out.println(k.process_name + ", " + v);
                if(v > this.coordinator.priority) {
                    this.coordinator.process_name = k.process_name;
                    this.coordinator.priority = v;
                }
            });

            System.out.println("New Coordinator is: " + this.coordinator.process_name);
        }
    }
}

public class ringAlgo{
    public static void main(String[] args) {
        Ring object = new Ring(5);
        object.execute();
    }
}