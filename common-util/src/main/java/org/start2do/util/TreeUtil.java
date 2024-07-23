package org.start2do.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TreeUtil {

    /**
     * 根据所有树节点列表，生成含有所有树形结构的列表
     *
     * @param nodes 树形节点列表
     * @param <T>   节点类型
     * @return 树形结构列表
     */
    public <T extends TreeNode<?>> List<T> generateTrees(List<T> nodes) {
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

    public <T extends TreeNode<? extends TreeNode>> T findNode(T node, String nodeId) {
        if (Objects.equals(node.getTreeNodeId(), nodeId)) {
            return node;
        }
        for (TreeNode child : node.getChildren()) {
            return (T) findNode(child, nodeId);
        }
        return null;
    }


    public static <T extends TreeNode<?>> List<T> findPath(TreeNode<T> root, String targetNodeId) {
        List<T> path = new ArrayList<>();
        findPathRecursive((T) root, targetNodeId, path);
        return path;
    }

    private static <T extends TreeNode<?>> boolean findPathRecursive(T node, String targetNodeId, List<T> path) {
        if (node == null) {
            return false;
        }

        path.add(node);

        if (Objects.equals(node.getTreeNodeId(), targetNodeId)) {
            return true;
        }

        for (Object child : node.getChildren()) {
            if (findPathRecursive((T) child, targetNodeId, path)) {
                return true;
            }
        }
        path.remove(path.size() - 1);
        return false;
    }


    public interface TreeNode<T> {

        String getTreeNodeId();

        String getParentId();

        void setChildren(List<T> children);

        List<T> getChildren();

        @JsonIgnore
        default List<String> getAllChildrenId() {
            return TreeUtil.getAllChildrenId(this);
        }

    }

    public static <T> List<String> getAllChildrenId(TreeUtil.TreeNode<T> tTreeNode) {
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

    public <T extends TreeNode<? extends TreeNode>> T findNode(List<T> node, String nodeId) {
        for (T t : node) {
            T node1 = findNode(t, nodeId);
            if (node1 != null) {
                return (T) node1;
            }
        }
        return null;
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
}
