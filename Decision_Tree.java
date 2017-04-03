/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.util.*;
import com.googlecode.jctree.ArrayListTree;
import com.googlecode.jctree.NodeNotFoundException;
import com.googlecode.jctree.Tree;

/***
 * 
 * @author hirenp
 * Classification tree implementation
 */

public class Decision_Tree {

    public static Tree<String> tree = new ArrayListTree<>();

    public static void main(String[] args) throws IOException, NodeNotFoundException {
        Scanner s1 = new Scanner(System.in);
        System.out.println("Please enter the path of Input data file : ");
        String name = s1.next();
        Scanner s2 = null;
        File f1 = new File(name);

        try {
            s2 = new Scanner(f1);
        }
        catch (FileNotFoundException ex) {
           System.out.println("File not Found");
           System.exit(0);
        }
        
        BuildTree t1 = new BuildTree(f1);
        
        tree = t1.getTree();
        Scanner s4 = new Scanner(System.in);
        System.out.println("Please enter the path of Test data file : ");
        String test_data = s4.next();        
        float accuracy = t1.processFile(test_data);
        System.out.println("\nThe Bonus Part: \n");
        System.out.println("\nThe Accuracy is : "+accuracy);
    }

}
