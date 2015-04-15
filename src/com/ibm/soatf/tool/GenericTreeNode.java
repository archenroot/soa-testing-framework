package com.ibm.soatf.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author zANGETSu
 * @param <T>
 */
public class GenericTreeNode<T> {

    /**
     *
     */
    public T data;

    /**
     *
     */
    public List<GenericTreeNode<T>> children;

    /**
     *
     */
    public GenericTreeNode() {
        super();
        children = new ArrayList<GenericTreeNode<T>>();
    }

    /**
     *
     * @param data
     */
    public GenericTreeNode(T data) {
        this();
        setData(data);
    }

    /**
     *
     * @return
     */
    public List<GenericTreeNode<T>> getChildren() {
        return this.children;
    }

    /**
     *
     * @return
     */
    public int getNumberOfChildren() {
        return getChildren().size();
    }

    /**
     *
     * @return
     */
    public boolean hasChildren() {
        return (getNumberOfChildren() > 0);
    }

    /**
     *
     * @param children
     */
    public void setChildren(List<GenericTreeNode<T>> children) {
        this.children = children;
    }

    /**
     *
     * @param child
     */
    public void addChild(GenericTreeNode<T> child) {
        children.add(child);
    }

    /**
     *
     * @param index
     * @param child
     * @throws IndexOutOfBoundsException
     */
    public void addChildAt(int index, GenericTreeNode<T> child) throws IndexOutOfBoundsException {
        children.add(index, child);
    }

    /**
     *
     */
    public void removeChildren() {
        this.children = new ArrayList<GenericTreeNode<T>>();
    }

    /**
     *
     * @param index
     * @throws IndexOutOfBoundsException
     */
    public void removeChildAt(int index) throws IndexOutOfBoundsException {
        children.remove(index);
    }

    /**
     *
     * @param index
     * @return
     * @throws IndexOutOfBoundsException
     */
    public GenericTreeNode<T> getChildAt(int index) throws IndexOutOfBoundsException {
        return children.get(index);
    }

    /**
     *
     * @return
     */
    public T getData() {
        return this.data;
    }

    /**
     *
     * @param data
     */
    public void setData(T data) {
        this.data = data;
    }

    public String toString() {
        return getData().toString();
    }

    /**
     *
     * @param node
     * @return
     */
    public boolean equals(GenericTreeNode<T> node) {
        return node.getData().equals(getData());
    }

    public int hashCode() {
        return getData().hashCode();
    }

    /**
     *
     * @return
     */
    public String toStringVerbose() {
        String stringRepresentation = getData().toString() + ":[";

        for (GenericTreeNode<T> node : getChildren()) {
            stringRepresentation += node.getData().toString() + ", ";
        }

        //Pattern.DOTALL causes ^ and $ to match. Otherwise it won't. It's retarded.
        Pattern pattern = Pattern.compile(", $", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(stringRepresentation);

        stringRepresentation = matcher.replaceFirst("");
        stringRepresentation += "]";

        return stringRepresentation;
    }
}