/**
 * 
 */
package in.ac.iitk.cse.mti.aritra.amrs.datamodels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author aritra
 * 
 */
public class Trie {
	private TrieNode root;
	private final int maxCost;
	private int nodeCount;
	private int wordCount;

	public Trie() {
		root = new TrieNode();
		maxCost = 3;
		nodeCount = 1;
		wordCount = 0;
	}

	public void insert(String word, String data) {
		TrieNode current = root;
		for (char letter : word.toCharArray()) {
			if (current.getChild(letter) == null) {
				current.setChild(letter, new TrieNode());
				nodeCount++;
			}
			current = current.getChild(letter);
		}
		current.isWord(true);
		current.setData(data);
		wordCount++;
	}
	
	public int getWordCount() {
		return wordCount;
	}
	
	public int getNodeCount() {
		return nodeCount;
	}

	public void print() {
		printRecursive(root, "");
	}

	private void printRecursive(TrieNode node, String formWord) {
		if (node.isWord()) {
			System.out.println(formWord);
		}
		for (Character c : node.getChildren().keySet()) {
			printRecursive(node.getChild(c), formWord + c);
		}
	}

	public String search(ArrayList<Character> word) {
		int iWordLength = word.size();
		int[] currentRow = new int[iWordLength + 1];
		for (int i = 0; i < currentRow.length; i++) {
			currentRow[i] = i;
		}

		Map<String, Integer> results = new HashMap<String, Integer>();
		for (Character letter : root.getChildren().keySet()) {
			traverseTrie(root.getChild(letter), letter, word, currentRow,
					results);
		}

		String data = null;
		int minimumDistance = iWordLength;
		for (Entry<String, Integer> entry : results.entrySet()) {
			if (entry.getValue() < minimumDistance) {
				data = entry.getKey();
				minimumDistance = entry.getValue();
			}
		}
		return data;
	}

	private void traverseTrie(TrieNode node, char letter,
			ArrayList<Character> word, int[] previousRow,
			Map<String, Integer> results) {
		int size = previousRow.length;
		int[] currentRow = new int[size];
		currentRow[0] = previousRow[0] + 1;

		int insertCost, deleteCost, replaceCost;

		for (int i = 1; i < size; i++) {
			insertCost = currentRow[i - 1] + 1;
			deleteCost = previousRow[i] + 1;

			if (word.get(i - 1) == letter) {
				replaceCost = previousRow[i - 1];
			} else {
				replaceCost = previousRow[i - 1] + 1;
			}

			currentRow[i] = minimum(insertCost, deleteCost, replaceCost);
		}

		if (currentRow[size - 1] <= maxCost && node.isWord()) {
			results.put(node.getData(), currentRow[size - 1]);
		}

		if (minElement(currentRow) <= maxCost) {
			for (Character c : node.getChildren().keySet()) {
				traverseTrie(node.getChildren().get(c), c, word, currentRow,
						results);
			}
		}
	}

	private int minimum(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}

	private int minElement(int[] array) {
		int minimum = array != null ? array[0] : 0;
		for (int element : array) {
			minimum = Math.min(minimum, element);
		}
		return minimum;
	}
}
