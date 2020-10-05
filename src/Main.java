import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;

class EntropyTable{
    String value;

    double Pi;
    double Ni;

    double I_PiNi;
}

class Node{
    String attributeName;
    ArrayList<Edge> edges;
}

class Edge{
    String edgeName;
    Node nextNode;
}

public class Main {

    public static String dataset[][];
    public static int datasetRow;
    public static int datasetCol;
    public static String pValue, nValue;
    public static int P,N;

    public static void initializeDataset(String filePath)
    {
        try {
            File file = new File(filePath);
            Scanner sc = new Scanner(file);

            datasetRow = Integer.valueOf(sc.next());
            datasetCol = Integer.valueOf(sc.next());
            pValue = sc.next().split("=")[1];
            nValue = sc.next().split("=")[1];

            dataset = new String[datasetRow][datasetCol];

            for(int i=0; i<datasetRow; i++)
                for(int j=0; j<datasetCol; j++)
                    dataset[i][j] = sc.next();

        } catch (FileNotFoundException e) {
            System.out.println("Failed to read file! error: " + e.getMessage());
        }
    }

    public static double log2(double n)
    {
        return (Math.log(n)/Math.log(2));
    }

    public static double calculateEntropy(double p, double n)
    {
        if(p==0 || n==0)
            return 0;
        else if(p==n)
            return 1;
        else
        {
            double entropy = ((-p/(p+n))*log2(p/(p+n))) - ((n/(p+n))*log2(n/(p+n)));
            return entropy;
        }
    }

    public static double entropyofClassAttribute(String table[][], int row)
    {
        int p = 0;
        int n = 0;

        for(int i=1; i<row; i++)
        {
            if(table[i][datasetCol-1].equals(pValue))
                p++;
            else
                n++;
        }

        P = p;
        N = n;

        return calculateEntropy(p,n);
    }

    public static String[] getUniqueValues(int columnNumber, String table[][], int row)
    {
        HashSet<String> hashSet = new HashSet<>();

        for(int i=1; i<row; i++)
            hashSet.add(table[i][columnNumber]);

        String hashValues[] = new String[hashSet.size()];
        int j = 0;

        for(Iterator<String> it = hashSet.iterator(); it.hasNext();)
            hashValues[j++] = String.valueOf(it.next());

        return hashValues;
    }

    public static double entropyofFeatureAttribute(int columnNumber, String table[][], int row)
    {
        ArrayList<EntropyTable> entropyTables = new ArrayList<>();

        String uniqueValus[] = getUniqueValues(columnNumber, table, row);

        for(int value=0; value<uniqueValus.length; value++)
        {
            int p = 0;
            int n = 0;

            for(int i=1; i<row; i++)
            {
                for(int j=0; j<datasetCol; j++)
                {
                    if(table[i][j].equals(uniqueValus[value]) && table[i][datasetCol-1].equals(pValue))
                        p++;
                    else if(table[i][j].equals(uniqueValus[value]) && table[i][datasetCol-1].equals(nValue))
                        n++;
                }
            }

            EntropyTable entropyTable = new EntropyTable();
            entropyTable.Pi = p;
            entropyTable.Ni = n;
            entropyTable.I_PiNi = calculateEntropy(p,n);
            entropyTable.value = uniqueValus[value];

            entropyTables.add(entropyTable);
        }

        double entropy = 0.0;

        for(int i=0; i<entropyTables.size(); i++)
        {
            entropy = entropy +  (((entropyTables.get(i).Pi + entropyTables.get(i).Ni)/(P+N)) * entropyTables.get(i).I_PiNi);

        }


        return entropy;
    }

    public static double calculateGain(String table[][], int columnNumber, int row)
    {
        return entropyofClassAttribute(table, row) - entropyofFeatureAttribute(columnNumber, table, row );
    }

    public static Node getRoot(String table[][],int row)
    {
        Node node = new Node();

        double maxGain = 0;
        int maxGainColumnNumber = 0;
        String maxGainAttributeName = table[0][0];

        for(int i=0; i<datasetCol-1; i++)
        {
            double gain = calculateGain(table, i, row);

            if(gain>=maxGain)
            {
                maxGain = gain;
                maxGainColumnNumber = i;
                maxGainAttributeName = table[0][i];
            }
        }

        String uniqueValus[] = getUniqueValues(maxGainColumnNumber, table, row);
        ArrayList<Edge> edges = new ArrayList<>();

        for(int i=0; i<uniqueValus.length; i++)
        {
            Edge edge = new Edge();
            edge.edgeName = uniqueValus[i];
            edge.nextNode = null;

            edges.add(edge);
        }

        node.attributeName = maxGainAttributeName;
        node.edges = edges;

        return node;
    }

    public static void decisionTree(Node node, String[][] table, int row)
    {
        if(node.edges == null)
            return;

        ArrayList<String> listofSplitedNode = listofSplitedNode(node, table, row);

        if(listofSplitedNode == null)
            return;

        for(int i=0; i<listofSplitedNode.size(); i++)
        {
            String newTable[][] = splitAndGetNewTable(table, node, row, listofSplitedNode.get(i));

            Node nextNode = getRoot(newTable, newTable.length);

            for(int j=0; j<node.edges.size(); j++)
            {
                Edge nodeEdge = node.edges.get(j);

                if(nodeEdge.edgeName.equals(listofSplitedNode.get(i)))
                {
                    node.edges.get(j).nextNode = nextNode;
                    decisionTree(nextNode, newTable, newTable.length);
                }
            }

        }

    }

    public static String[][] splitAndGetNewTable(String[][] table, Node node, int row, String attributeName)
    {
        int colNumber = getColumnNumberFromAttributeName(node.attributeName, table, row);

        String[][] newTable;

        int newTablerow = 0;

        for(int i=1; i<row; i++)
        {
            if(table[i][colNumber].equals(attributeName))
                newTablerow++;
        }

        newTable = new String[newTablerow+1][datasetCol];

        System.arraycopy(table[0], 0, newTable[0], 0, table[0].length);

        int j=1;

        for(int i=1; i<row; i++)
        {
            if(table[i][colNumber].equals(attributeName))
                System.arraycopy(table[i], 0, newTable[j++], 0, table[i].length);
        }

        return newTable;
    }

    public static int getColumnNumberFromAttributeName(String attributeName, String[][] table, int row)
    {

        for(int i=0; i<datasetCol; i++)
        {
            if(table[0][i].equals(attributeName))
                return i;
        }

        return -1;
    }
    
    public static ArrayList<String> listofSplitedNode(Node node, String[][] table, int row)
    {
        ArrayList<String> listofSplitedNode = new ArrayList<>();

        int attributeColNumber = getColumnNumberFromAttributeName(node.attributeName, table, row);

        String[] uniqueValue = getUniqueValues(attributeColNumber, table, row);


        for(int value=0; value<uniqueValue.length; value++)
        {
            String classAttribute = null;

            for(int i=1; i<row; i++)
            {
                if(table[i][attributeColNumber].equals(uniqueValue[value]))
                {
                    classAttribute = table[i][datasetCol-1];
                    break;
                }
            }

            int featureValue = 0;
            int classValue = 0;

            for(int i=1; i<row; i++)
            {
                if(table[i][attributeColNumber].equals(uniqueValue[value]))
                    featureValue++;

                if(table[i][attributeColNumber].equals(uniqueValue[value]) && table[i][datasetCol-1].equals(classAttribute))
                    classValue++;
            }

            if(featureValue!=classValue)
                listofSplitedNode.add(uniqueValue[value]);

            else if(featureValue == classValue)
            {
                Node nextNode = new Node();
                nextNode.edges = null;

                nextNode.attributeName = classAttribute;

                for(int i=0; i<node.edges.size(); i++)
                {
                    if(node.edges.get(i).edgeName.equals(uniqueValue[value]))
                    {
                        node.edges.get(i).nextNode = nextNode;
                    }
                }

            }

        }

        if(listofSplitedNode.size() > 0)
            return listofSplitedNode;

        return null;
    }

    public static void printChar(int count, char ch)
    {
        for(int i=0; i<count; i++)
            System.out.print(ch);
    }

    public static void print(Node node, int spaceCount)
    {
        spaceCount++;

        if(node == null)
        {
            spaceCount--;
            return;
        }

        printChar(spaceCount, '-');
        spaceCount++;
        System.out.println(node.attributeName);

        if(node.edges!=null)
        {
            for(int i=0; i<node.edges.size(); i++)
            {
                printChar(spaceCount, '-');

                System.out.println(node.edges.get(i).edgeName);
                print(node.edges.get(i).nextNode, spaceCount);
            }
        }

    }



    public static void main(String[] args) {

        initializeDataset("src/res/Dataset.txt");

        Node root = getRoot(dataset, datasetRow);
        decisionTree(root, dataset, datasetRow);

        print(root, -1);

    }

}
