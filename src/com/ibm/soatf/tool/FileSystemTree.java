package com.ibm.soatf.tool;

import java.util.ArrayList;
import java.util.List;

public class FileSystemTree<T> {
    
    private Node<T> root;

    public FileSystemTree(T rootData) {
        root = new Node<T>();
        root.data = rootData;
        root.children = new ArrayList<Node<T>>();
    }

    public static class Node<T> {
        private T data;
        private Node<T> parent;
        private List<Node<T>> children;
    }
    
    public void add(){
        
    }
    public void remove(){
        
    }
    public void traverse(){
        
    }
    
}