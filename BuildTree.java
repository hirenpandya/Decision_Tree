/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.googlecode.jctree.ArrayListTree;
import com.googlecode.jctree.NodeNotFoundException;
import com.googlecode.jctree.Tree;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.Math.log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author hirenp
 */
public class BuildTree {

    public int rows,cols,location,w;
    public double EntropyS;
    public String temp1,temp2,target_attribute,fileinput;

    public Tree<String> tree = new ArrayListTree<>();
    
    public File f1;
    String workingDir;
    
    public BuildTree(File f1){
        this.f1 = f1;
        this.rows=0;
        this.cols=0;
        this.location=0;
        this.w=0;
        this.EntropyS=0;
        this.temp1=null;
        this.temp2=null;
        this.target_attribute=null;
        this.fileinput=""; 
        this.workingDir = System.getProperty("user.dir");
    }
    
    // The basic tree information
    public Tree<String> getTree() throws NodeNotFoundException, IOException {
        rows = getRow(f1);                      
        cols = getCol(f1);                     

        String[][] data = new String[rows][cols]; 
        String[][] database = database(data,f1,rows,cols);      

        String[] attributes = attributes(f1);                  
        String[] target_attribute_list = TargetAttr(database);    

        System.out.println("Please select Target attribute :");
        for(int i=0;i<target_attribute_list.length;i++){
            System.out.println(i+" - "+target_attribute_list[i]);
        }
        
        Scanner s1 = new Scanner(System.in);
        
        int choice = s1.nextInt();
        for(int i=0;i<attributes.length;i++){
            if(target_attribute_list[choice].contentEquals(attributes[i]))
            location = i;
        }

        System.out.println("The Index of Target Attribute is "+ location);
        target_attribute = target_attribute_list[choice];
        System.out.println("The Target Attribute is : "+target_attribute_list[choice]);

        // Calculating the Entropy
        double entropy = entropy(database ,target_attribute_list[choice] ,location );
        System.out.println("The Entropy is : "+ entropy);

        // Finding the root node based on Info gain
        String rootnode = rootnode(database,target_attribute,location,attributes);     
        System.out.println("The rootnode is : "+rootnode);

        // Finding the position of attribute
        int positionOf_root_node = positionOfAttr(database,rootnode);                
        System.out.println("The position of attribute = "+positionOf_root_node);

        // Making the root node and its child in Tree data structure
        make_tree(database, rootnode, f1);   

        String nodenames = tree.children(rootnode).toString();
        String replaceAll = nodenames.toString().replaceAll("\\[|\\]", "");
        replaceAll = replaceAll.replaceAll(" ", "");
        String[] split = replaceAll.split(",");
        String single = "";
        int space = 1;
        String chld = "";
        for(int i =0;i<split.length;i++){     
            chld = split[i];
            System.out.println("if  "+rootnode+ " is "+split[i]);
            fileinput += "if  "+rootnode+ " is "+split[i]+"\n";          
            ID3_algo(database , split[i],positionOf_root_node,f1, single, space, chld);
        }
        
        File f = new File(this.workingDir+"/Rules");
        if(f.exists()){
            f.delete();
        }
        boolean createNewFile = f.createNewFile();
        try (BufferedWriter output = new BufferedWriter(new FileWriter(f , true))) {
            output.write(fileinput);
        }
        System.out.println("\n\nOutput is in Rules file");
  
        return tree;
    }    

    public void ID3_algo(String[][] database, String arc, int targetindex, File f1, String single, int space, String ch) throws NodeNotFoundException {

        int counter=0;
        for(int i =0;i<database.length;i++){
            if(database[i][targetindex].equals(arc)){
                counter++;   
            }
        }
        counter = counter + 1;
        int num_column = 0;
        String[][] subData = new String[(counter)][database[0].length]; // creating subset database
        
        if(database.length == rows ){

            for(int j=0;j<database[0].length;j++){                
                subData[0][j] =  database[0][j];
                num_column= j;

            }
            
            int k=1; 
            for(int i =0;i<database.length;i++){
                if(database[i][targetindex].equals(arc) && k <=(counter-1) ){
                    for(int j=0;j<cols;j++){
                        subData[k][j] =  database[i][j];
                        num_column= j;
                    }
                    k++;
                }
            }
        }
        else{          
            for(int j=0;j<database[0].length;j++){
                subData[0][j] =  database[0][j];
                num_column= j;

            }
             
            int k=1; 
            for(int i =0;i<database.length;i++){
                if(database[i][targetindex].equals(arc) && k <= (counter-1) ){
                    for(int j=0;j<database[0].length;j++){
                        subData[k][j] =  database[i][j];
                        num_column= j;
                    }
                    k++;
                }
            }
        }

        // To check if current node has children or not
        String morenodes = morenodes(subData); 

        num_column = num_column + 1;
        
        // If there are children of current node, it will go for recursion
        if(morenodes.equals("yes")){
            String[][] subDatabase = subDatabase(subData,targetindex,subData.length,num_column);
            // If ID3 algo doesn't find pure set, it will consider most frequently occurred target value as final target attribute
            if(single.contains("single ")){
                String space_str ="";
                for(int q=0; q<space; q++){
                    space_str = space_str + "\t";
                }                
                String t[] = single.split("\\s+");
                System.out.println(space_str+"then "+target_attribute+" is "+ t[1]);
                fileinput +=space_str+"then "+target_attribute+" is "+ t[1]+"\n";  

                tree.add(ch,t[1]);
                
            } else {
                String gain = gain_sub(subDatabase);
                String[] str = gain.split("####");
                gain = str[0];

                if(gain==null){

                } else {
                    tree.add(arc,gain);
                    int positionOfAttr = positionOfAttr(subDatabase, gain);
                    maketree2(subDatabase,gain,f1,positionOfAttr) ;
                    String nodenames = tree.children(gain).toString();

                    String replaceAll = nodenames.toString().replaceAll("\\[|\\]", "");
                    replaceAll = replaceAll.replaceAll("\\s+", "");
                    String[] split = replaceAll.split(",");

                    if(!tree.leaves().equals(temp1) && !tree.leaves().equals(temp2) ){
                        for(int i =0;i<split.length;i++){
                            ch = split[i];
                            String space_str ="";
                            for(int q=0; q<space; q++){
                                space_str = space_str + "\t";
                            }

                            System.out.println(space_str+"if "+gain+" is " + split[i]);
                            fileinput +=space_str+"if  "+gain+ " is "+split[i]+"\n";                            
                            ID3_algo(subDatabase , split[i],positionOfAttr,f1, str[1], (space+1), ch); 
                        }
                    }
                }
            }
        } else {
            String duplicate = new String();
            duplicate = morenodes;

            if(tree.contains(duplicate)){
                while(tree.contains(duplicate)){
                    duplicate = duplicate + " ";             
                }                
                tree.add(ch, duplicate);  
            } else{
                tree.add(ch, duplicate);           
            }

            String space_str ="";
            for(int q=0; q<space; q++){
                space_str = space_str + "\t";
            }            
            System.out.println(space_str+"then "+target_attribute+" is "+ morenodes);
            fileinput +=space_str+"then "+target_attribute+" is "+ morenodes+"\n";      
        }
    }

    // Creating tree for rest of nodes
    public void maketree2(String[][] subDatabase, String rootnode, File f1, int attr_pos) throws NodeNotFoundException {
        
        tree.add(rootnode);
        int attrpos=attr_pos;
        LinkedHashSet a1 = new LinkedHashSet();

        for(int i=1;i<subDatabase.length;i++){
            a1.add(subDatabase[i][attrpos]);
        }

        String replaceAll = a1.toString().replaceAll("\\[|\\]", "");
        replaceAll = replaceAll.replaceAll("\\s+", "");
        String[] split = replaceAll.split(",");
        for(int i=0;i<split.length;i++){
            tree.add(rootnode,split[i]);
        }
    }

    // Calculating gain
    public String gain_sub(String[][] subData) {
        int l=0;
        Map<String, Double> hm = new HashMap<String, Double>();
        String[] attr= new String[subData[0].length];  
        for(int j=0;j<subData[0].length;j++){
            if(subData[0][j]!=null ){
                    attr[j] = subData[0][j];
            }

            if( attr[j]!=null && attr[j].equals(target_attribute) ){
                l=j;
            }
        }
        String t = "";
        String garrt = "";
        for(int ind = 0;ind<subData[0].length;ind++){
            if(ind != l){
                garrt=subData[0][ind];
                LinkedHashSet a1 = new LinkedHashSet();
                ArrayList<String> b = new ArrayList<String>(); 
                for(int i=1;i<subData.length;i++){
                    a1.add(subData[i][ind]);
                    b.add(subData[i][ind]);
                } 

                String replaceAll = a1.toString().replaceAll("\\[|\\]", "");
                replaceAll = replaceAll.replaceAll("\\s+", "");
                String[] split = replaceAll.split(",");
                double pos=0,neg=0,xy=0,value=0;
                int[] c = new int[split.length];
                for(int k=0;k<split.length;k++){ 
                    String x = split[k];
                    if(x.equals("null")){
 
                    } else {
                        c[k] = rootcount(b,x);  
                    }
                }

                for(int i=0;i<split.length;i++){ 
                   pos=0;neg=0;
                   for(int j=0;j<subData.length;j++){ 

                       if(subData[j][ind]!=null && subData[j][ind].equals(split[i]) && subData[j][l].equals(temp1) ){
                           pos++;
                       }
                       if(subData[j][ind]!=null && subData[j][ind].equals(split[i]) && subData[j][l].equals(temp2) ){
                           neg++;
                       }
                   }

                    if(pos !=0 && neg !=0){
                        double first = (double)pos/(double)(pos+neg);
                        double first_1 = log(first)/log(2);
                        double second = (double) neg /(double)(pos+neg);
                        double second_1 = log(second)/log(2);    
                        xy =  -(first*first) - (second*second_1);
                    }
                    else  if(pos ==0 && neg !=0){
                        double first = (double)pos/(double)(pos+neg);
                        double first_1 = log(first)/log(2);
                        double second = (double) neg /(double)(pos+neg);
                        double second_1 = log(second)/log(2);
                        xy =  -(0) - (second*second_1);
                    }
                    else  if(pos !=0 && neg ==0){
                        double first = (double)pos/(double)(pos+neg);
                        double first_1 = log(first)/log(2);
                        double second = (double) neg /(double)(pos+neg);
                        double yy = log(second)/log(2);
                        xy =  -(first*first_1) - (0);
                    }

                    value -= ( (   (double)c[i] /(double) (subData.length-1)) * xy );

                    if(pos>neg){
                        t = temp1;
                    } else{
                        t = temp2;
                    }
                    
                }
                hm.put(garrt, (EntropyS+value));
            }
        }   

        String a="";
        Double maxMap=(Collections.max(hm.values()));
        int hash_size = hm.size();
        for (Map.Entry<String, Double> entry : hm.entrySet()) {  
            if (entry.getValue()==maxMap) {
            //if ((entry.getValue()==maxMap) && (entry.getKey()!=null)) {
                a  = entry.getKey();   
                if(hash_size==1){
                    a = a + "####single "+t;
                } else  {
                    a = a + "####multiple";
                }
            }
        }   
        return a;
    }

    // Finding sub entries
    public String[][] subDatabase(String[][] subData, int targetindex, int rsize, int csize) {
        int counter=0;
        //System.out.println(rsize+" "+(csize-1));
        String[][] subData2 = new String[rsize][csize-1];
        
        for(int i =0;i<subData2.length;i++){  
            int k=0;  
            for(int j=0;j<subData[0].length;j++){	            	
                if(j != targetindex){
                    subData2[i][k] = subData[i][j];
                    k++;
                }
            }
        }
        
        return subData2;
    }

    // Finding whether current node has children or not
    public String morenodes(String[][] subData) {
        int c_size=0;
        int same1=0,same2=0;
        for(int i =0;i<subData.length;i++){
            for(c_size=0;c_size<subData[0].length;c_size++){
                if(subData[i][c_size]==null ) {
                }
                else if(subData[i][c_size].equals(temp1) ){
                    same1++;
                }
                else if(subData[i][c_size].equals(temp2)){
                    same2++;
                }  
            }
        }
        
        if(same1 !=0 && same2 !=0){
            return "yes";
        }

        else if(same1 == 0 ){
            return temp2;
        }
        else{
            return temp1;
        }
    }

    
    public void make_tree(String[][] database, String rootnode, File f1) throws FileNotFoundException, NodeNotFoundException {
        tree.add(rootnode);
        int attrpos=0;
        LinkedHashSet a1 = new LinkedHashSet();
        Scanner s11 = new Scanner(f1);
        String line = s11.nextLine();
        String[] parts =  line.split("\\s+");
        for(int i=0;i<parts.length;i++){
            if (parts[i].equals(rootnode)){
                attrpos = i;
            }
        }     

        for(int i=1;i<database.length;i++){
            a1.add(database[i][attrpos]);
        }
        String replaceAll = a1.toString().replaceAll("\\[|\\]", "");
        String replaceAll1 = replaceAll.replaceAll("\\s+", "");
        String[] split = replaceAll1.split(",");
        for(int i=0;i<split.length;i++){
            tree.add(rootnode,split[i]);            
        }
   
    }

    // Finding the position of Root node
    public int positionOfAttr(String[][] database, String rootnode) {
        int counter=0;
        for(int i =0;i<database[0].length;i++){
 
            if(database[0][i]==null){
                  
            } else if(database[0][i].equals(rootnode)){
                counter=i;   
            }
        }
        return counter;
    }

    // Finding the Root node
    public String rootnode(String[][] database, String target_attribute, int location, String[] attributes) {
        String rootgain="",rootnode="";
        Map<String, Double> hm = new HashMap<String, Double>();

        for(int i=0;i<attributes.length;i++){
            if(i!=location){
                rootgain = gain(database,attributes[i],i,location);
                //System.out.println("rootgain "+" "+i + rootgain);
            }
            if(i<attributes.length){                                         
                String[] sp = rootgain.split("\\s+");
                hm.put(sp[0], Double.parseDouble(sp[1]));
            }
        }    	   	

        Double maxMap=(Collections.max(hm.values()));  
        for (Map.Entry<String, Double> entry : hm.entrySet()) {  
            if (entry.getValue()==maxMap) {
                rootnode  = entry.getKey();     
            }
        }

        return rootnode;

    }

    // Finding the Info gain
    public String gain(String[][] database, String attribute, int ind, int targ_index) {
        double pos=0,neg=0,xy=0,value=0;
        ArrayList<String> a = new ArrayList<String>(); 
        LinkedHashSet l = new LinkedHashSet();
        for(int i=1;i<rows;i++){
            //System.out.println("i am here"+database[i][ind]);
            l.add(database[i][ind]);
            a.add(database[i][ind]);
        }
        String replaceAll = l.toString().replaceAll("\\[|\\]", "");
        replaceAll = replaceAll.replaceAll(" ", "");
        String[] unique = replaceAll.split(",");

        int[] c = new int[unique.length];
        for(int k=0;k<unique.length;k++){  
            String x = unique[k];
            c[k] = rootcount(a,x);

        }

        for(int i=0;i<unique.length;i++){ 
            pos=0;neg=0;
            for(int j=0;j<database.length;j++){ 
                if( database[j][ind].equals(unique[i]) && database[j][targ_index].equals(temp1) ){
                    pos++;
                }
                else if( database[j][ind].equals(unique[i]) && database[j][targ_index].equals(temp2) ){
                    neg++;
                }
            }

            if(pos !=0 && neg !=0){
                double first = (double)pos/(double)(pos+neg);
                double first_1 = log(first)/log(2);
                double second = (double) neg /(double)(pos+neg);
                double second_1 = log(second)/log(2);	                
                xy =  -(first*first_1) - (second*second_1);	               
            }
            else  if(pos ==0 && neg !=0){
                double first = (double)pos/(double)(pos+neg);
                double first_1 = log(first)/log(2);
                double second = (double) neg /(double)(pos+neg);
                double second_1 = log(second)/log(2);
                xy =  -(0) - (second*second_1);
            }
            else  if(pos !=0 && neg ==0){
                double first = (double)pos/(double)(pos+neg);
                double first_1 = log(first)/log(2);
                double second = (double) neg /(double)(pos+neg);
                double second_1 = log(second)/log(2);
                xy =  -(first*first_1) - (0);
            }    
            value -= ( (   (double)c[i] /(double) (database.length-1)) * xy );
        }

        String  attr_entropy = attribute+" "+(EntropyS+value);   

        return attr_entropy;
     }

    // Finding the Root Index
    public int rootcount(ArrayList<String> itemList, String x) {
        int count = 0;

        for (int i=0;i<itemList.size();i++) {    
            if ( itemList.get(i).equals(x)) {
                count++;
            }
        }

        return count;
    }

    // Finding Entropy
    public double entropy(String[][] database, String attribute, int location2) {
        String temp=null;
        int tar_size = database.length-1;
        String target[] = new String[database.length];

        for(int i=1;i<database.length;i++){
                target[i]= database[i][location2];
                if(temp1==null){
                    temp1=target[i];
                }
                if(!temp1.equals(target[i])){
                    temp2=target[i];
                }
        }

        List<String> resultList = Arrays.asList(target);

        int count = Collections.frequency(resultList, temp1);
        int count2 = Collections.frequency(resultList, temp2);

        if(count<count2){
            int t=count;
            count=count2;
            count2=t; 
            String t1=temp1;
            temp1=temp2;
            temp2=t1; 
        }

        double first = (double)count/(double)(database.length-1);
        double first_1 = log(first)/log(2);
        double second = (double)count2/(double)(database.length-1);
        double second_1 = log(second)/log(2);
        double x =  (-first*first_1) - (second*second_1);
        EntropyS = x;
        return x;	
    }

    // Findind possible target attributes
    public String[] TargetAttr(String[][] data) {
        LinkedHashSet a = new LinkedHashSet();
        LinkedHashSet b = new LinkedHashSet();
      
        for(int i=0;i<cols;i++){
            a.clear(); 
            for(int j=0;j<rows;j++){
                a.add(data[j][i]);
            }
            if(a.size()==3){
                b.add(data[0][i]);
            }

        }

        String replaceAll = b.toString().replaceAll("\\[|\\]", "");
        String replaceAll1 = replaceAll.replaceAll(" ", "");
        String[] split = replaceAll1.split(",");

        return split;
    }

    // Fining all attributes
    public String[] attributes(File f1) throws FileNotFoundException {
        Scanner s1 = new Scanner(f1);
        String line = s1.nextLine();
        String words[] = line.split("\\s+");

        return words;
    }

    // Getting all entries
    public String[][] database(String[][] data, File f1, int rows2, int cols2) throws FileNotFoundException, IOException {
        FileReader input_file = new FileReader(f1.getAbsolutePath());
        BufferedReader br_input_file = new BufferedReader(input_file);
        String line_input_file;
        int lncnt = 0;
        while((line_input_file = br_input_file.readLine())!=null){
            if(lncnt<rows2){
                String[] parts =  line_input_file.split("\\s+");
                for(int j=0;j<parts.length;j++){
                    data[lncnt][j]=parts[j].trim();
                }
                
                lncnt = lncnt + 1;
            } 

        } 
        return data;	
    }

    // getting all columns
    public int getCol(File f1) throws FileNotFoundException {
        Scanner s1 = new Scanner(f1);
        int i =0;
        if (f1.exists()){
            if(s1.hasNextLine()){
                String line = s1.nextLine();
                String[] parts =  line.split("\\s+");
                i= parts.length;
            }
        }

        return i;
    }

    // getting all rows
    public int getRow(File f1) throws FileNotFoundException, IOException {
        FileReader input_file = new FileReader(f1.getAbsolutePath());
        BufferedReader br_input_file = new BufferedReader(input_file);
        String line_input_file;
        int lncnt = 0;
        while((line_input_file = br_input_file.readLine())!=null){
            lncnt = lncnt + 1;
        } 
        return lncnt;
    }

    // for predicting output based on generated ID3 decision tree
    public float processFile(String filename) throws FileNotFoundException, IOException, NodeNotFoundException {
        FileReader input_file = new FileReader(filename);
        BufferedReader br_input_file = new BufferedReader(input_file);
        String line_input_file;
        HashMap<Integer, String> map1 = new HashMap<>();

        float lncnt = 0;
        float predict = 0;
        System.out.println("\n\nTarget Attribute\tCorrect Label\tPredicted Label");
        while((line_input_file = br_input_file.readLine())!=null){
            HashMap<String, String> map2 = new HashMap<>();
            
            if(lncnt==0){
                String[] headers = line_input_file.split("\\s+");
                for(int i=0; i<headers.length; i++){
                    map1.put(i, headers[i]);
                }
            } else {
                String[] entry = line_input_file.split("\\s+");
                for(int i=0; i<entry.length; i++){
                    map2.put(map1.get(i), entry[i]);
                }                
                int j = checkRecord(map2, this.tree, (int) lncnt);
                if(j==1){
                   predict++;
                }               
            }
            
            lncnt = lncnt + 1;
        }
        lncnt = lncnt - 1;
        float accuracy = (predict/lncnt)*100;
        
        return accuracy;
    }

    public int checkRecord(HashMap<String, String> map2, Tree<String> tree,int lncnt) throws NodeNotFoundException {
        String root = tree.root();
        int flag = 0;
        String str = "";
        if(root!=null){
            str = traverse(root,map2,lncnt);
            if(map2.containsKey(this.target_attribute)){
                if(str.contains(map2.get(this.target_attribute)) || str.toLowerCase().contains(map2.get(this.target_attribute))){                
                    flag = 1;
                }
                System.out.println(this.target_attribute+ "\t"+map2.get(this.target_attribute)+"\t"+str + "\n");

            } else {
                if(str.contains(map2.get("class")) || str.toLowerCase().contains(map2.get("class"))){                
                    flag = 1;
                }       
                System.out.println("class\t"+map2.get("class")+"\t"+str + "\n");

            }

        } else {
            flag = 0;
        }        

        return flag;
    }

    // For Traversing generated decision tree
    public String traverse(String node, HashMap<String, String> map2, int lncnt) throws NodeNotFoundException {
        String leaf = "";
        
        if(tree.children(node).isEmpty()){
            return node;
        } else{
            Collection<String> children = tree.children(node);
            Object[] child = children.toArray();
            for(int j=0; j<child.length; j++){
                if(map2.containsKey(node) && child[j].equals(map2.get(node))){
                    leaf = (String) child[j];
                } else if(!map2.containsKey(node)){
                    leaf = (String) child[j];
                }
            }
            
            leaf = traverse(leaf,map2, lncnt);
        }
        
        return leaf;
        
    }
    
    
}
