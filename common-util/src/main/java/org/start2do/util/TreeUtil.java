package org.start2do.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TreeUtil {

    public <T extends TreeNode<?>> void printTree(List<T> roots) {
        for (T root : roots) {
            printTree(root, 0);
        }
    }

    private <T extends TreeNode<?>> void printTree(T node, int level) {
        if (node == null) {
            return;
        }

        for (int i = 0; i < level; i++) {
            System.out.print("  ");
        }
        System.out.println(node.getTreeNodeId());

        for (T child : (List<T>) node.getChildren()) {
            printTree(child, level + 1);
        }
    }

    /**
     * 根据所有树节点列表，生成含有所有树形结构的列表;会存在丢失的情况.
     *
     * @param nodes 树形节点列表
     * @param <T>   节点类型
     * @return 树形结构列表
     */
    public <T extends TreeNode<?>> List<T> generateTreesHasMiss(List<T> nodes) {
        List<T> roots = new ArrayList<>();
        List<T> original = new ArrayList<>(nodes);
        for (Iterator<T> ite = original.iterator(); ite.hasNext(); ) {
            T node = ite.next();
            if (node.getParentId() == null || Objects.equals(node.getParentId(), "")) {
                roots.add(node);
                // 从所有节点列表中删除该节点，以免后续重复遍历该节点
                ite.remove();
            }
        }
        roots.forEach(root -> {
            setChildren(root, original);
        });
        if (roots.isEmpty()) {
            nodes.stream().findFirst().ifPresent(t -> {
                setChildren(t, original);
            });
        }
        return roots;
    }

    /**
     * 另外一种实现方式不允许丢失
     */
    public <T extends TreeNode<?>> List<T> generateTreesNoMiss(List<T> nodes) {
        List<T> roots = new ArrayList<>();
        Map<String, T> nodeMap = nodes.stream().collect(Collectors.toMap(t -> t.getTreeNodeId(), t -> t));
        for (T node : nodes) {
            if (node.getParentId() == null || Objects.equals(node.getParentId(), "")) {
                roots.add(node);
            } else {
                T parent = nodeMap.get(node.getParentId());
                if (parent != null) {
                    List<T> children = (List<T>) parent.getChildren();
                    children.add(node);
                } else {
                    roots.add(node);
                }
            }
        }
        return roots;
    }

    /**
     * 从所有节点列表中查找并设置parent的所有子节点
     *
     * @param parent 父节点
     * @param nodes  所有树节点列表
     */

    public <T extends TreeNode> void setChildren(T parent, List<T> nodes) {
        List<T> children = new ArrayList<>();
        Object parentId = parent.getTreeNodeId();
        for (Iterator<T> ite = nodes.iterator(); ite.hasNext(); ) {
            T node = ite.next();
            if (Objects.equals(node.getParentId(), parentId)) {
                children.add(node);
                // 从所有节点列表中删除该节点，以免后续重复遍历该节点
//                ite.remove();
            }
        }
        // 如果孩子为空，则直接返回,否则继续递归设置孩子的孩子
        if (children == null || children.isEmpty()) {
            return;
        }
        parent.setChildren(children);
        children.forEach(child -> {
            // 递归设置子节点
            setChildren(child, nodes);
        });
    }

    //
//    public <T extends TreeNode<? extends TreeNode>> T findNode(T node, String nodeId) {
//        if (Objects.equals(node.getTreeNodeId(), nodeId)) {
//            return node;
//        }
//        for (TreeNode child : node.getChildren()) {
//            return (T) findNode(child, nodeId);
//        }
//        return null;
//    }
//
//    public <T extends TreeNode<? extends TreeNode>> T findNode(List<T> node, String nodeId) {
//        for (T t : node) {
//            T node1 = findNode(t, nodeId);
//            if (node1 != null) {
//                return (T) node1;
//            }
//        }
//        return null;
//    }
    public <T extends TreeNode<? extends TreeNode>> T findNode(T node, String nodeId) {
        if (Objects.equals(node.getTreeNodeId(), nodeId)) {
            return node;
        }
        for (TreeNode child : node.getChildren()) {
            T foundNode = findNode((T) child, nodeId);
            if (foundNode != null) {
                return foundNode;
            }
        }
        return null;
    }

    public <T extends TreeNode<? extends TreeNode>> T findNode(List<T> node, String nodeId) {
        for (T t : node) {
            T foundNode = findNode(t, nodeId);
            if (foundNode != null) {
                return foundNode;
            }
        }
        return null;
    }


    public interface TreeNode<T> extends Cloneable {

        String getTreeNodeId();

        String getParentId();

        void setParentId(String id);

        void setTreeNodeId(String id);

        void setChildren(List<T> children);

        List<T> getChildren();

        @JsonIgnore
        default List<String> getAllChildrenId() {
            return TreeUtil.getAllChildrenId(this);
        }
    }

    public static <T> List<String> getAllChildrenId(TreeNode<T> tTreeNode) {
        List<String> result = new ArrayList<>();
        for (T child : tTreeNode.getChildren()) {
            if (child instanceof TreeNode<?>) {
                String id = ((TreeNode<?>) child).getTreeNodeId();
                result.add(id);
                TreeNode<T> node = (TreeNode<T>) child;
                if ((node).getChildren() != null) {
                    result.addAll(getAllChildrenId(node));
                }
            }
        }
        return result;
    }


    public static <T extends TreeNode<?>> List<T> hasCycle(List<T> nodes) {
        Set<String> visited = new HashSet<>();
        Set<String> stack = new HashSet<>();
        List<T> result = new ArrayList<>();
        for (T node : nodes) {
            if (hasCycleUtil(node, visited, stack, nodes)) {
                result.add(node);
            }
        }
        return result;
    }

    private static <T extends TreeNode<?>> boolean hasCycleUtil(T node, Set<String> visited, Set<String> stack,
        List<T> nodes) {
        String nodeId = node.getTreeNodeId();
        if (stack.contains(nodeId)) {
            return true;
        }
        if (visited.contains(nodeId)) {
            return false;
        }

        visited.add(nodeId);
        stack.add(nodeId);
        for (T child : getChildren(nodeId, nodes)) {
            if (hasCycleUtil(child, visited, stack, nodes)) {
                return true;
            }
        }

        stack.remove(nodeId);
        return false;
    }

    public static <T extends TreeNode<?>> List<T> getChildren(String parentId, List<T> nodes) {
        List<T> children = new ArrayList<>();
        for (T node : nodes) {
            if (Objects.equals(node.getParentId(), parentId)) {
                children.add(node);
            }
        }
        return children;
    }

    public static <T extends TreeNode<T>> List<T> getAllChildren(List<T> nodes) {
        List<T> children = new ArrayList<>();
        for (T node : nodes) {
            children.add(node);
            List<T> nodeChildren = node.getChildren();
            if (nodeChildren != null && !nodeChildren.isEmpty()) {
                children.addAll(getAllChildren(nodeChildren));
            }
        }
        return children;
    }

    //-----------------------------------------------
    public static <T extends TreeUtil.TreeNode<T>> List<T> mergeTreesPreservingChildren(List<List<T>> treeLists) {
        Map<String, T> mergedNodesMap = new HashMap<>();

        // First pass: Collect all nodes
        for (List<T> treeList : treeLists) {
            for (T root : treeList) {
                collectNodes(root, mergedNodesMap);
            }
        }

        // Second pass: Merge children
        for (List<T> treeList : treeLists) {
            for (T root : treeList) {
                mergeChildren(root, mergedNodesMap);
            }
        }

        // Rebuild the tree structure
        List<T> mergedRoots = new ArrayList<>();
        for (T node : mergedNodesMap.values()) {
            String parentId = node.getParentId();
            if (parentId == null || parentId.isEmpty() || !mergedNodesMap.containsKey(parentId)) {
                mergedRoots.add(node);
            }
        }

        return mergedRoots;
    }

    private static <T extends TreeNode<T>> void collectNodes(T node, Map<String, T> mergedNodesMap) {
        String nodeId = node.getTreeNodeId();
        if (!mergedNodesMap.containsKey(nodeId)) {
            T newNode = createNewNode(node);
            mergedNodesMap.put(nodeId, newNode);
        }

        for (T child : node.getChildren()) {
            collectNodes(child, mergedNodesMap);
        }
    }

    private static <T extends TreeNode<T>> void mergeChildren(T node, Map<String, T> mergedNodesMap) {
        T mergedNode = mergedNodesMap.get(node.getTreeNodeId());

        for (T child : node.getChildren()) {
            T mergedChild = mergedNodesMap.get(child.getTreeNodeId());
            if (!mergedNode.getChildren().contains(mergedChild)) {
                mergedNode.getChildren().add(mergedChild);
            }
            mergeChildren(child, mergedNodesMap);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends TreeNode<T>> T createNewNode(T originalNode) {
        try {
            // This assumes that T has a no-arg constructor. Adjust if necessary.
            T newNode = (T) originalNode.getClass().getDeclaredConstructor().newInstance();

            // Copy basic TreeNode properties
            newNode.setTreeNodeId(originalNode.getTreeNodeId());
            newNode.setParentId(originalNode.getParentId());
            newNode.setChildren(new ArrayList<>());

            // Here you might want to copy other properties specific to your implementation
            // For example:
            // newNode.setName(originalNode.getName());
            // newNode.setValue(originalNode.getValue());

            return newNode;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create new node", e);
        }
    }

    ////////////////////////////

    /**
     * 获取从根节点到指定节点的路径，并包含该节点的下方所有子节点。添加深拷贝
     *
     * @param nodes  所有节点列表
     * @param nodeId 目标节点ID
     * @return 从根节点(包含)到目标节点的路径，并包含该节点的下方所有子节点
     */
    public static <T extends TreeNode<T>> List<T> getNodePathAndChildrenDeepCopy(List<T> nodes, String nodeId) {
        Map<String, T> nodeMap = new HashMap<>();
        for (T node : nodes) {
            nodeMap.put(node.getTreeNodeId(), node);
            for (T child : getAllChildren(node.getChildren())) {
                nodeMap.put(child.getTreeNodeId(), child);
            }
        }

        // Find the node with the given nodeId
        T targetNode = nodeMap.get(nodeId);

        if (targetNode == null) {
            return Collections.emptyList(); // If node is not found, return empty list
        }

        // Collect nodes from the root to the target node
        List<T> pathNodes = new ArrayList<>();
        T currentNode = targetNode;

        while (currentNode != null) {
            pathNodes.add(currentNode);
            currentNode = nodeMap.get(currentNode.getParentId());
        }

        Collections.reverse(pathNodes); // Reverse to get the path from root to the target node

        // Collect the children of the target node
        List<T> result = new ArrayList<>(pathNodes);
        result.addAll(getAllChildren(targetNode.getChildren()));

        // Perform deep copy of the result
        List<T> deepCopyResult = new ArrayList<>();
        for (T node : result) {
            deepCopyResult.add(createNewNode(node));
        }

        // Rebuild the tree structure for the deep copy
        Map<String, T> deepCopyMap = new HashMap<>();
        for (T node : deepCopyResult) {
            deepCopyMap.put(node.getTreeNodeId(), node);
        }

        for (T node : deepCopyResult) {
            String parentId = node.getParentId();
            if (parentId != null && !parentId.isEmpty()) {
                T parentNode = deepCopyMap.get(parentId);
                if (parentNode != null) {
                    parentNode.getChildren().add(node);
                }
            }
        }

        // Filter out the root nodes from the deep copy result
        List<T> finalResult = new ArrayList<>();
        for (T node : deepCopyResult) {
            if (node.getParentId() == null || node.getParentId().isEmpty() || !deepCopyMap.containsKey(
                node.getParentId())) {
                finalResult.add(node);
            }
        }

        return finalResult;
    }

}
