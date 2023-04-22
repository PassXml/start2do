package org.start2do.util.spring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TrieMap<V> {

    private static class TrieNode<V> {

        private Map<Character, TrieNode<V>> children;
        private V value;
        private boolean isEndOfWord;

        public TrieNode() {
            this.children = new HashMap<>();
            this.value = null;
            this.isEndOfWord = false;
        }

        public Map<Character, TrieNode<V>> getChildren() {
            return children;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        public boolean isEndOfWord() {
            return isEndOfWord;
        }

        public void setEndOfWord(boolean isEndOfWord) {
            this.isEndOfWord = isEndOfWord;
        }
    }

    private TrieNode<V> root;
    private Map<String, V> cache;

    public TrieMap() {
        this.root = new TrieNode<>();
        this.cache = new ConcurrentHashMap<>();
    }

    public void put(String key, V value) {
        TrieNode<V> node = root;
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            Map<Character, TrieNode<V>> children = node.getChildren();
            node = children.computeIfAbsent(c, k -> new TrieNode<>());
        }
        node.setValue(value);
        node.setEndOfWord(true);
        cache.clear(); // 清空缓存
    }

    public V get(String key) {
        V result = cache.get(key); // 从缓存中获取结果
        if (result != null) {
            return result;
        }
        TrieNode<V> node = getNode(key);
        result = (node != null && node.isEndOfWord()) ? node.getValue() : null;
        cache.put(key, result); // 将结果存入缓存
        return result;
    }

    public boolean containsKey(String key) {
        TrieNode<V> node = getNode(key);
        return (node != null && node.isEndOfWord());
    }

    public List<V> prefixSearch(String prefix) {
        TrieNode<V> node = getNode(prefix);
        List<V> values = new ArrayList<>();
        if (node != null) {
            prefixSearchHelper(node, values);
        }
        return values;
    }

    private TrieNode<V> getNode(String key) {
        TrieNode<V> node = root;
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            Map<Character, TrieNode<V>> children = node.getChildren();
            node = children.get(c);
            if (node == null) {
                return null;
            }
        }
        return node;
    }

    private void prefixSearchHelper(TrieNode<V> node, List<V> values) {
        if (node.isEndOfWord()) {
            values.add(node.getValue());
        }
        for (TrieNode<V> child : node.getChildren().values()) {
            prefixSearchHelper(child, values);
        }
    }

    public boolean remove(String key) {
        TrieNode<V> node = root;
        List<TrieNode<V>> path = new ArrayList<>();
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            Map<Character, TrieNode<V>> children = node.getChildren();
            node = children.get(c);
            if (node == null) {
                return false;
            }
            path.add(node);
        }
        if (!node.isEndOfWord()) {
            return false;
        }
        node.setEndOfWord(false);
        node.setValue(null);
        for (int i = path.size() - 1; i >= 0; i--) {
            TrieNode<V> currentNode = path.get(i);
            if (currentNode.isEndOfWord() || !currentNode.getChildren().isEmpty()) {
                break;
            }
            Map<Character, TrieNode<V>> parentChildren = path.get(i - 1).getChildren();
            parentChildren.remove(key.charAt(i - 1));
        }
        cache.remove(key);
        return true;
    }
}



