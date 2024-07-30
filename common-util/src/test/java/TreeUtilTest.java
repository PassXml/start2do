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
        List<Node> trees = TreeUtil.generateTreesHasMiss(list);
        List<Node> trees2 = TreeUtil.generateTreesNoMiss(list);
        Assertions.assertEquals(trees2.size(), trees.size());
    }

    @Test
    @Tag("findNode2")
    void findNode2() {
        Node node = new Node("1", null, "1", new ArrayList<>());
        Node node1 = new Node("2", "1", "2", new ArrayList<>());
        Node node2 = new Node("3", "1", "3", new ArrayList<>());
        Node node3 = new Node("4", "1", "3", new ArrayList<>());
        Node node4 = new Node("5", "2", "4", new ArrayList<>());
        Node node5 = new Node("6", "2", "5", new ArrayList<>());
        Node node6 = new Node("7", "3", "6", new ArrayList<>());
        Node node7 = new Node("8", "6", "7", new ArrayList<>());
        Node node8 = new Node("9", "3", "8", new ArrayList<>());
        List<Node> list = List.of(node, node1, node2, node3, node4, node5, node6, node7, node8);
        List<Node> nodes = TreeUtil.generateTreesHasMiss(list);
        TreeUtil.printTree(nodes);
        Node node9 = TreeUtil.findNode(nodes, "9");
        Assertions.assertNotNull(node9);
    }


    @Test
    @Tag("findNode")
    void test1() {
        Node node1 = new Node("4", "2", "4", new ArrayList<>());
        Node node2 = new Node("2", "1", "2", new ArrayList<>());
        List<Node> list = List.of(new Node("1", null, "1", new ArrayList<>()), node2,
            new Node("3", "1", "3", new ArrayList<>()), node1, new Node("5", "3", "5", new ArrayList<>()));
        List<Node> nodes = TreeUtil.generateTreesHasMiss(list);
        Node node3 = TreeUtil.findNode(nodes, "2");
        System.out.println(node1.getAllChildrenId());
        System.out.println(node3);
    }

    @Test
    @Tag("Cycle")
    void CycleTest() {
        List<Node> list = List.of(new Node("1", "5", "1", new ArrayList<>()),
            new Node("3", "5", "3", new ArrayList<>()), new Node("5", "3", "5", new ArrayList<>()));
        Assertions.assertTrue(!TreeUtil.hasCycle(list).isEmpty());
    }

    @Test
    @Tag("Cycle False")
    void CycleTestFalse() {
        List<Node> list = List.of(new Node("1", null, "1", new ArrayList<>()),
            new Node("3", "1", "3", new ArrayList<>()), new Node("5", "3", "5", new ArrayList<>()));
        Assertions.assertTrue(TreeUtil.hasCycle(list).isEmpty());
    }

    @Test
    @Tag("test3")
    void test3() {
        List<Node> list = List.of(new Node("1", "5", "1", new ArrayList<>()));
        List<Node> nodes = TreeUtil.generateTreesNoMiss(list);
        System.out.println(nodes);
        Assertions.assertEquals(1, nodes.size());
    }

    @Test
    @Tag("循环引用")
    void test4() {
        List<Node> list = List.of(new Node("1", "1", "1", new ArrayList<>()));
        List<Node> nodes = TreeUtil.hasCycle(list);
        Assertions.assertEquals(1, nodes.size());
    }

    @Test
    @Tag("keep-NodeId-path(remove other)")
    void test2() {
        Node node1 = new Node("4", "2", "4", new ArrayList<>());
        Node node2 = new Node("2", "1", "2", new ArrayList<>());
        List<Node> list = List.of(new Node("1", null, "1", new ArrayList<>()), node2,
            new Node("3", "1", "3", new ArrayList<>()), node1, new Node("5", "3", "5", new ArrayList<>()));
        List<Node> nodes = TreeUtil.generateTreesHasMiss(list);
        System.out.println(nodes);
        List<Node> path = TreeUtil.findPath(nodes.stream().filter(t -> t.getParentId() == null).findFirst().get(), "2");
        List<Node> trees = TreeUtil.generateTreesHasMiss(path);
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
