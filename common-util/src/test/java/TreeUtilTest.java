import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.start2do.util.TreeUtil;
import org.start2do.util.TreeUtil.TreeNode;

class TreeUtilTest {

    @Test
    @Tag("build")
    void test() {
        List<Node> list = new ArrayList<>(
            List.of(new Node("1", null, "1", new ArrayList<>()), new Node("2", "1", "2", new ArrayList<>()),
                new Node("3", "1", "3", new ArrayList<>()), new Node("4", "2", "4", new ArrayList<>()),
                new Node("5", "3", "5", new ArrayList<>())));
        List<Node> trees = TreeUtil.generateTrees(list);
        System.out.println(trees);
    }

    @Test
    @Tag("findNode")
    void test1() {
        Node node1 = new Node("4", "2", "4", new ArrayList<>());
        Node node2 = new Node("2", "1", "2", new ArrayList<>());
        List<Node> list = List.of(new Node("1", null, "1", new ArrayList<>()), node2,
            new Node("3", "1", "3", new ArrayList<>()), node1, new Node("5", "3", "5", new ArrayList<>()));
        List<Node> nodes = TreeUtil.generateTrees(list);
        Node node3 = TreeUtil.findNode(nodes, "2");
        System.out.println(node1.getAllChildrenId());
        System.out.println(node3);
    }

    @Test
    @Tag("Cycle")
    void CycleTest() {
        List<Node> list = List.of(new Node("1", "5", "1", new ArrayList<>()),
            new Node("3", "1", "3", new ArrayList<>()), new Node("5", "3", "5", new ArrayList<>()));
        Assertions.assertTrue(TreeUtil.hasCycle(list).isEmpty());
    }

    @Test
    @Tag("removeCycle")
    void removeCycleTest() {
        List<Node> list = List.of(new Node("1", null, "1", new ArrayList<>()),
            new Node("3", "5", "3", new ArrayList<>()), new Node("5", "3", "5", new ArrayList<>()));
        List<Node> nodes1 = TreeUtil.hasCycle(list);
        System.out.println(nodes1);
    }

    @Test
    @Tag("Cycle False")
    void CycleTestFalse() {
        List<Node> list = List.of(new Node("1", null, "1", new ArrayList<>()),
            new Node("3", "1", "3", new ArrayList<>()), new Node("5", "3", "5", new ArrayList<>()));
        Assertions.assertFalse(TreeUtil.hasCycle(list).isEmpty());
    }

    @Test
    @Tag("keep-NodeId-path(remove other)")
    void test2() {
        Node node1 = new Node("4", "2", "4", new ArrayList<>());
        Node node2 = new Node("2", "1", "2", new ArrayList<>());
        List<Node> list = List.of(new Node("1", null, "1", new ArrayList<>()), node2,
            new Node("3", "1", "3", new ArrayList<>()), node1, new Node("5", "3", "5", new ArrayList<>()));
        List<Node> nodes = TreeUtil.generateTrees(list);
        System.out.println(nodes);
        List<Node> path = TreeUtil.findPath(nodes.stream().filter(t -> t.getParentId() == null).findFirst().get(), "2");
        List<Node> trees = TreeUtil.generateTrees(path);
        System.out.println(trees);
    }


    @Setter
    @Getter
    @NoArgsConstructor
    @ToString
    public static class Node implements TreeNode<Node> {

        private String id;
        private String parentId;
        private String name;
        private List<Node> children;

        @Override
        public String getTreeNodeId() {
            return this.id;
        }

        public Node(String id, String parentId, String name, List<Node> children) {
            this.id = id;
            this.parentId = parentId;
            this.name = name;
            this.children = children;
        }

    }
}
