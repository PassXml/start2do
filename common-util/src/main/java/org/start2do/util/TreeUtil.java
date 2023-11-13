package org.start2do.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
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
        Object parentId = parent.getId();
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
        if (Objects.equals(node.getId(), nodeId)) {
            return node;
        }
        for (TreeNode child : node.getChildren()) {
            return (T) findNode(child, nodeId);
        }
        return null;
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

    public interface TreeNode<T> {

        String getId();

        String getParentId();


        void setChildren(List<T> children);

        List<T> getChildren();

        default List<String> getAllChildrenId() {
            return TreeUtil.getAllChildrenId(this);
        }

    }

    public static <T> List<String> getAllChildrenId(TreeNode<T> tTreeNode) {
        List<String> result = new ArrayList<>();
        for (T child : tTreeNode.getChildren()) {
            if (child instanceof TreeNode<?>) {
                String id = ((TreeNode<?>) child).getId();
                result.add(id);
                TreeNode<T> node = (TreeNode<T>) child;
                if ((node).getChildren() != null) {
                    result.addAll(getAllChildrenId(node));
                }
            }
        }
        return result;
    }

}
