import java.util.*;
import java.io.*;

class MacroScan{
    HashMap<String, ArrayList<Integer>> mnt = new HashMap<>();
    HashMap<String, ArrayList<ArrayList<String>>> mdt = new HashMap<>();
    HashMap<String, ArrayList<ArrayList<String>>> kpdtab = new HashMap<>();
    HashMap<String, ArrayList<String>> ala = new HashMap<>();
    List<List<String>> callingCode = new ArrayList<>();
    int macro_count = 0;
    boolean flag = false;
    boolean isCall = false;
    public void macroPass1() throws FileNotFoundException {
        FileReader fr = new FileReader("src/macro_program");
        Scanner sc = new Scanner(fr);
        int pp = 0, kp = 0;
        ArrayList<String> p_params = new ArrayList<>();
        ArrayList<ArrayList<String>> k_params = new ArrayList<>();
        String name = "";
        ArrayList<ArrayList<String>> function_code = new ArrayList<>();
        List<String> call = new ArrayList<>();
        while(sc.hasNextLine()) {
            ArrayList<String> func_body = new ArrayList<>();
            String l = sc.nextLine().replaceAll(",","");
            String[] line = l.replaceAll("^\\s","").split(" ");

            if(isCall) {
                call = Arrays.asList(line);
                this.callingCode.add(call);
            }
            else {
                func_body.addAll(Arrays.asList(line));
                function_code.add(func_body);
                if(line[0].equals("MEND")) {
                    flag = false;
                    ArrayList<Integer> details = new ArrayList<>(Arrays.asList(pp, kp, macro_count));
                    this.mnt.put(name, details);
                    this.mdt.put(name,function_code);
                    this.kpdtab.put(name, k_params);
                    this.ala.put(name, p_params);
                    k_params = new ArrayList<>();
                    p_params = new ArrayList<>();
                    function_code = new ArrayList<>();
                    pp = 0;
                    kp = 0;
                    name = "";
                }

                if(line[0].equals("MACRO")) {
                    flag = true;
                    macro_count += 1;
                    String func = sc.nextLine().replaceAll(",","");
                    String[] func_def = func.replaceAll("^\\s","").split(" ");
                    func_body = new ArrayList<>();
                    func_body.addAll(Arrays.asList(func_def));
                    function_code.add(func_body);
                    name = func_def[0];
                    for(int i=1;i<func_def.length;i++) {
                        if(func_def[i].charAt(0) == '&') {
                            if(func_def[i].length() > 2 && func_def[i].charAt(2) == '=') {
                                kp += 1;
                                String[] default_param = func_def[i].split("=");
                                if(default_param.length == 2) {
                                    ArrayList<String> keyword_param = new ArrayList<>(Arrays.asList(
                                            default_param[0], default_param[1]));
                                    k_params.add(keyword_param);
                                    p_params.add(default_param[0]);
                                }
                                else {
                                    ArrayList<String> keyword_param = new ArrayList<>(List.of(func_def[i]));
                                    k_params.add(keyword_param);
                                    p_params.add(default_param[0]);
                                }
                            }
                            if(func_def[i].length() == 2) {
                                pp += 1;
                                p_params.add(func_def[i]);
                            }
                        }
                    }
                }
            }

            if(line[0].equals("START")) {
                isCall = true;
            }
        }

        this.mdt.forEach((k, v) -> {
            ArrayList<String> params = this.ala.get(k);
            for(int i=0;i<v.size();i++) {
                for(int j=0;j<v.get(i).size();j++) {
                    if(v.get(i).get(j).charAt(0) == '&') {
                        for(int k1=0;k1<params.size();k1++) {
                            if(v.get(i).get(j).equals(params.get(k1))) {
                                String query = "(P,";
                                query += Integer.toString(k1+1) + ")";
                                v.get(i).set(j, query);
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    public void macroPass2() throws IOException {
        macroPass1();

        System.out.println(this.callingCode);
        System.out.println(this.mnt);
        System.out.println(this.kpdtab);
        for(int x=0;x<this.callingCode.size();x++) {
            boolean func = false;
            for(int j=0;j<this.callingCode.get(x).size();j++) {
                String ins = this.callingCode.get(x).get(j);
                if(this.mnt.containsKey(ins)) {
                    func = true;
                    break;
                }
            }

            if(func) {
                HashMap<Integer, String> actual_param = new HashMap<>();
                String[] s = String.join(" ", this.callingCode.get(x)).split(" ");
                String func_name = s[0];
                ArrayList<String> par = this.ala.get(func_name);
                for(int i=0;i<s.length;i++) {
                    String[] temp;
                    if(s[i].charAt(0) == '&') {
                        if(s[i].length() >= 4) {
                            temp = s[i].split("=");
                            String val = temp[1];
                            for(int j=0;j<par.size();j++) {
                                if(par.get(j).equals(temp[0])) {
                                    actual_param.put(j+1, val);
                                    par.remove(j);
                                    break;
                                }
                            }
                        }
                    }
                }

                if(par.size() > 0) {
                    ArrayList<ArrayList<String>> k_p = this.kpdtab.get(func_name);
                    for(int i=0;i<par.size();i++) {
                        for(int j=0;j< k_p.size();j++) {
                            if(par.get(i).equals(k_p.get(j).get(0))) {
                                actual_param.put(j+1,k_p.get(j).get(1));
                                break;
                            }
                        }
                    }
                }

                actual_param.forEach((k, v) -> {
                    System.out.println(k + " " + v);
                });

                FileWriter fw = new FileWriter("src/macroOutput");

                ArrayList<ArrayList<String>> code = new ArrayList<>();
                code = this.mdt.get(func_name);

                for(int i=0;i<code.size();i++) {
                    for(int j=0;j<code.get(i).size();j++) {
                        String arg = code.get(i).get(j);
                        if(arg.charAt(0) == '(' && arg.charAt(1) == 'P') {
                            String t = "";
                            t += arg.charAt(3);
                            int key = Integer.parseInt(t);
                            code.get(i).set(j, actual_param.get(key));
                        }
                    }
                }

                this.mdt.replace(func_name, code);

                this.mdt.forEach((k, v) -> {
                    for(int i=0;i<v.size();i++) {
                        for(int j=0;j<v.get(i).size();j++) {
                            try {
                                fw.write(v.get(i).get(j) + " ");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        try {
                            fw.write("\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                fw.write("START\n");
                for(int i=0;i<this.callingCode.size();i++) {
                    for(int j=0;j<this.callingCode.get(i).size();j++) {
                        fw.write(this.callingCode.get(i).get(j) + " ");
                    }
                    fw.write("\n");
                }

                fw.close();
            }

        }
    }
}

public class Macro_Processor {
    public static void main(String[] args) throws IOException {
        MacroScan macro = new MacroScan();
        macro.macroPass2();
    }
}
