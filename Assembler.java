import java.util.*;
import java.io.*;

class AssemblerScan{
    int location_counter = 0;
    HashMap<String, Integer> sym_tab = new HashMap<>();
    HashMap<Integer, ArrayList<String>> lit_tab = new HashMap<>();
    ArrayList<Integer> pool_tab = new ArrayList<>();
    HashMap<String, List<String>> mot = new HashMap<>();
    ArrayList<ArrayList<String>> intermediate_code = new ArrayList<>();
    ArrayList<ArrayList<String>> machine_code = new ArrayList<>();
    HashMap<Integer, String> forward_reference = new HashMap<>();
    int sym_pointer = 0, lit_pointer = 0, pool_pointer = -1;

    AssemblerScan() {
        this.mot.put("STOP", Arrays.asList("IS", "00"));
        this.mot.put("ADD", Arrays.asList("IS", "01"));
        this.mot.put("SUB", Arrays.asList("IS", "02"));
        this.mot.put("MULT", Arrays.asList("IS", "03"));
        this.mot.put("MOVER", Arrays.asList("IS", "04"));
        this.mot.put("MOVEM", Arrays.asList("IS", "05"));
        this.mot.put("COMP", Arrays.asList("IS", "06"));
        this.mot.put("BC", Arrays.asList("IS", "07"));
        this.mot.put("DIV", Arrays.asList("IS", "08"));
        this.mot.put("READ", Arrays.asList("IS", "09"));
        this.mot.put("PRINT", Arrays.asList("IS", "10"));
        this.mot.put("R1", Arrays.asList("RG", "01"));
        this.mot.put("R2", Arrays.asList("RG", "02"));
        this.mot.put("R3", Arrays.asList("RG", "03"));
        this.mot.put("R4", Arrays.asList("RG", "04"));
        this.mot.put("START", Arrays.asList("AD", "01"));
        this.mot.put("END", Arrays.asList("AD", "02"));
        this.mot.put("ORIGIN", Arrays.asList("AD", "03"));
        this.mot.put("EQU", Arrays.asList("AD", "04"));
        this.mot.put("LTORG", Arrays.asList("AD", "05"));
        this.mot.put("DC", Arrays.asList("DL", "01"));
        this.mot.put("DS", Arrays.asList("DL", "02"));
    }

    public int getSymIndex(String label_name) {
        int ind = 0;
        for(Map.Entry<String, Integer> s: this.sym_tab.entrySet()) {
            if(s.getKey().equals(label_name)) {
                break;
            }
            ind += 1;
        }

        return ind;
    }

    public void assemblerPass1() throws FileNotFoundException {
        FileReader fr = new FileReader("src/assembly_program");
        Scanner sc = new Scanner(fr);
        int pool_size = 0;
        int position = 0;
        while(sc.hasNextLine()) {
            String l = sc.nextLine().replaceAll("^\\s","");
            String[] line = l.replaceAll(",","").split(" ");
            int inc = 1;

            for(int i=0;i< line.length;i++) {
                ArrayList<String> ins = new ArrayList<>();
                boolean isDir = false;
                position += 1;
                if(!this.mot.containsKey(line[i])) {
                    if(i == 0) {
                        ins.add("S");
                        if(!this.sym_tab.containsKey(line[i])) {
                            this.sym_tab.put(line[i], this.location_counter);
                            ins.add(Integer.toString(sym_pointer));
                            sym_pointer += 1;
                        }
                        else {
                            int c = 0;
                            for (Map.Entry<String, Integer> set :this.sym_tab.entrySet()) {
                                if(set.getKey().equals(line[i])) {
                                    break;
                                }
                                c += 1;
                            }
                            ins.add(Integer.toString(c));
                        }
                    }
                    else {
                        if(line[i].charAt(0) == '=') {
                            ins.add("L");
                            ins.add(Integer.toString(lit_pointer));
                            lit_pointer += 1;
                            ArrayList<String> val = new ArrayList<>();
                            val.add(line[i]);
                            val.add("0");
                            this.lit_tab.put(lit_pointer, val);
                            pool_size += 1;
                        }
                        else {
                            boolean isNum = true;
                            for(int j=0;j<line[i].length();j++) {
                                if(line[i].charAt(j) < '0' || line[i].charAt(j) > '9') {
                                    isNum = false;
                                    break;
                                }
                            }
                            if(isNum) {
                                ins.add("C");
                                ins.add(line[i]);
                            }
                            else {
                                if(this.sym_tab.containsKey(line[i])) {
                                    int sym_ind = getSymIndex(line[i]);
                                    ins.add("S");
                                    ins.add(Integer.toString(sym_ind));
                                }
                                else {
                                    forward_reference.put(position, line[i]);
                                }
                            }
                        }
                    }
                }
                else {
                    if(line[i].equals("START")) {
                        inc = Integer.parseInt(line[i+1]);
                        ins.add(this.mot.get("START").get(0));
                        ins.add(this.mot.get("START").get(1));
                        isDir = true;
                    }
                    else if(line[i].equals("LTORG") || line[i].equals("END")) {
                        pool_pointer = lit_pointer - pool_size + 1;
                        int p = this.location_counter;
                        for(int j=pool_pointer;j<=this.lit_tab.size();j++) {
                            ArrayList<String> temp = new ArrayList<>();
                            temp.add(this.lit_tab.get(j).get(0));
                            temp.add(Integer.toString(p));
                            this.lit_tab.replace(j, temp);
                            p += 1;
                        }
                        inc = pool_size;
                        this.pool_tab.add(pool_pointer);
                        if(line[i].equals("LTORG")) {
                            ins.add(this.mot.get("LTORG").get(0));
                            ins.add(this.mot.get("LTORG").get(1));
                        }
                        else {
                            ins.add(this.mot.get("END").get(0));
                            ins.add(this.mot.get("END").get(1));
                        }
                        isDir = true;
                        pool_size = 0;
                    }
                    else if(line[i].equals("ORIGIN")) {
                        String query = line[i+1].replace('+',' ');
                        String[] arr = query.split(" ");
                        int address = this.sym_tab.get(arr[0]);
                        inc = address + Integer.parseInt(arr[1]) - this.location_counter;
                        ins.add(this.mot.get("ORIGIN").get(0));
                        ins.add(this.mot.get("ORIGIN").get(1));
                        isDir = true;
                        position -= 1;
                        i += 2;
                    }
                    else if(line[i].equals("DS")) {
                        int space = Integer.parseInt(line[i+1]);
                        inc = space;
                        ins.add(this.mot.get("DS").get(0));
                        ins.add(this.mot.get("DS").get(1));
                    }
                    else if(line[i].equals("EQU")) {
                        inc = 0;
                        String sym1 = line[i-1];
                        String sym2 = line[i+1];
                        int adr = this.sym_tab.get(sym2);
                        this.sym_tab.replace(sym1, adr);
                        ins.add(this.mot.get("EQU").get(0));
                        ins.add(this.mot.get("EQU").get(1));
                        isDir = true;
                    }
                    else {
                        ins.add(this.mot.get(line[i]).get(0));
                        ins.add(this.mot.get(line[i]).get(1));
                    }
                }


                this.intermediate_code.add(ins);
            }

            this.location_counter += inc;
            System.out.println(this.location_counter);
        }

        if(this.forward_reference.size() > 0) {
            int ind = 0;
            for(Map.Entry<Integer, String> s :this.forward_reference.entrySet()) {
                int sym_ind = 0;
                sym_ind = getSymIndex(s.getValue());
                ArrayList<String> temp = new ArrayList<>();
                temp.add("S");
                temp.add(Integer.toString(sym_ind));
                this.intermediate_code.set(s.getKey()-1, temp);
            }
        }

        System.out.println(forward_reference);
        System.out.println(this.intermediate_code);
        System.out.println(this.sym_tab);
        System.out.println(this.lit_tab);
        System.out.println(this.pool_tab);
    }

    void writeToFile() throws IOException {
        FileWriter fw = new FileWriter("src/assembly_inter_code");
        for(int i=0;i<this.intermediate_code.size();i++) {
            String word = "(";
            word += this.intermediate_code.get(i).get(0) + ", " + this.intermediate_code.get(i).get(1) + ") ";
            fw.write(word);
        }

        fw.close();
    }
//
//    void assemblerPass2() {
//
//    }
}


public class Assembler {
    public static void main(String[] args) throws IOException {
        AssemblerScan object = new AssemblerScan();
        object.assemblerPass1();
//        object.writeToFile();
    }
}
