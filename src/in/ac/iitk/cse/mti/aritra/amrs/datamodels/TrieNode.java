/**
 * 
 */
package in.ac.iitk.cse.mti.aritra.amrs.datamodels;

import java.util.HashMap;
import java.util.Map;

/**
 * @author aritra
 * 
 */
public class TrieNode {
    private boolean isWord;
    private String data;
    private Map<Character, TrieNode> children;
    
    public TrieNode() {
        isWord = false;
        data = null;
        children = new HashMap<Character, TrieNode>();
    }
    
    public boolean isWord() {
        return isWord;
    }
    
    public void isWord(boolean flag) {
        isWord = flag;
    }
    
    public String getData() {
        return data;
    }
    
    public void setData(String data) {
        this.data = data;
    }
    
    public TrieNode getChild(char letter) {
        return children.get(letter);
    }
    
    public void setChild(char letter, TrieNode node) {
        children.put(letter, node);
    }
    
    public Map<Character, TrieNode> getChildren() {
        return children;
    }
}
